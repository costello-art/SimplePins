package com.sk.simplepins;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sk.simplepins.data.Pin;
import com.sk.simplepins.data.source.PinsDataSource;
import com.sk.simplepins.data.source.local.PinsLocalDataSource;
import com.sk.simplepins.utils.PermissionUtils;

import java.util.List;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener,
        PinsDataSource.OnAllPinsLoadedCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener, PinsDataSource.OnPinSavedCallback, PinsDataSource.OnAllPinsRemovedCallback {

    private GoogleMap mMap;
    private PinsDataSource mDataSource;
    private String mFacebookId;


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private GoogleApiClient mGoogleApiClient;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mFacebookId = Profile.getCurrentProfile().getId();
        mDataSource = PinsLocalDataSource.getInstance(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void saveMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.err_location_permission_revoked, Toast.LENGTH_LONG).show();
            return;
        }

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (lastLocation != null) {
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            MarkerOptions marker = new MarkerOptions().position(latLng).draggable(true);

            mDataSource.savePin(new Pin(marker, mFacebookId), this);
            Toast.makeText(this, R.string.current_location_saved, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        switch (id) {
            case R.id.nav_store_location:
                saveMyLocation();
                break;

            case R.id.nav_delete_all:
                askToRemoveAllPins();
                break;

            case R.id.nav_logout:
                mProgressBar.setVisibility(View.VISIBLE);
                logoutAndStartLoginActivity();
                break;
        }

        return true;
    }

    private void askToRemoveAllPins() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_confirm_remove_all)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllPins();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.show();
    }

    private void deleteAllPins() {
        mDataSource.deleteAllPins(mFacebookId, this);
    }

    private void enableMyLocation() {
        checkForNetworkLocation();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mMap.setMyLocationEnabled(true);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);

        enableMyLocation();
        mDataSource.getPins(mFacebookId, this);
    }

    @Override
    public void onMapClick(final LatLng latLng) {
        mDataSource.savePin(new Pin("", latLng.latitude, latLng.longitude, mFacebookId), this);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        mDataSource.deletePin((long) marker.getTag(), new PinsDataSource.OnPinRemoveCallback() {
            @Override
            public void onPinRemoveSuccess() {
                marker.remove();
            }

            @Override
            public void OnPinRemoveError() {
                Toast.makeText(MainActivity.this, R.string.err_unable_to_remove_marker, Toast.LENGTH_SHORT).show();
            }
        });

        return true;
    }

    @Override
    public void onPinsLoaded(List<Pin> pins) {
        for (Pin pin : pins) {
            addMarkerAndSetTag(pin);
        }
    }

    @Override
    public void onDataNotAvailable() {
        //do nothing
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog.newInstance().show(getSupportFragmentManager(), "dialog");
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) { }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onMarkerDragStart(Marker marker) { }

    @Override
    public void onMarkerDrag(Marker marker) { }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mDataSource.updatePin(new Pin(marker, mFacebookId), new PinsDataSource.OnPinUpdatedCallback() {
            @Override
            public void onPinUpdated() {
                Toast.makeText(MainActivity.this, R.string.marker_updated, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPinUpdateError() {
                Toast.makeText(MainActivity.this, R.string.err_unable_to_update_marker, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPinSaved(Pin pin) {
        addMarkerAndSetTag(pin);
    }

    private void addMarkerAndSetTag(Pin pin) {
        LatLng latLng = new LatLng(pin.getLat(), pin.getLng());
        MarkerOptions options = new MarkerOptions().position(latLng).draggable(true);
        Marker marker = mMap.addMarker(options);
        marker.setTag(pin.getId());
    }

    @Override
    public void onPinSaveError() {
        Toast.makeText(this, R.string.err_unable_to_save_marker, Toast.LENGTH_SHORT).show();
    }

    private void checkForNetworkLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean networkLocationEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!networkLocationEnabled) {
            //show dialog to allow user to enable location settings
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.dialog_enable_location_title);
            dialog.setMessage(R.string.dialog_enable_location_descr);

            dialog.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                }
            });

            dialog.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    //nothing to do
                }
            });

            dialog.show();
        }

    }

    @Override
    public void onAllPinsRemoved() {
        mMap.clear();
        Toast.makeText(this, R.string.toast_all_pins_removed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemoveAllPinsError() {
        Toast.makeText(this, R.string.toast_unable_to_remove_all_pins, Toast.LENGTH_SHORT).show();
    }
}