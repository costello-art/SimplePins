package com.sk.simplepins.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sk.simplepins.data.Pin;
import com.sk.simplepins.data.source.PinsDataSource;
import com.sk.simplepins.data.source.local.PinsPersistenceContract.PinEntry;

import java.util.ArrayList;
import java.util.List;

import static com.sk.simplepins.utils.PinValidator.validate;

/**
 * Created by Sviat on Nov 09, 2016.
 */

public class PinsLocalDataSource implements PinsDataSource {

    private static final String TAG = PinsLocalDataSource.class.getName();
    private static final Object lock = new Object();
    private static volatile PinsLocalDataSource INSTANCE;

    private PinsDbHelper mDbHelper;

    private String[] mPinProjection = {
            PinEntry._ID,
            PinEntry.COLUMN_NAME_UID,
            PinEntry.COLUMN_NAME_PIN_NAME,
            PinEntry.COLUMN_NAME_LAT,
            PinEntry.COLUMN_NAME_LNG
    };

    private PinsLocalDataSource(Context context) {
        mDbHelper = new PinsDbHelper(context);
    }

    public static PinsLocalDataSource getInstance(Context context) {
        PinsLocalDataSource initialized = INSTANCE;

        if (initialized == null) {
            synchronized (lock) {
                initialized = INSTANCE;
                if (initialized == null) {
                    initialized = new PinsLocalDataSource(context);
                    INSTANCE = initialized;
                }
            }
        }

        return initialized;
    }

    @Override
    public void getPins(String uid, OnAllPinsLoadedCallback callback) {
        List<Pin> pins = new ArrayList<>();

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String whereClause = PinEntry.COLUMN_NAME_UID + " = ?";
        String[] whereArgs = {uid};

        Cursor c = db.query(
                PinEntry.TABLE_NAME,
                mPinProjection,
                whereClause,
                whereArgs,
                null,
                null,
                null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                pins.add(parsePin(c));
            }
        }

        if (c != null) {
            c.close();
        }

        if (pins.isEmpty()) {
            callback.onDataNotAvailable();
        } else {
            callback.onPinsLoaded(pins);
        }
    }

    @Override
    public void savePin(Pin pin, OnPinSavedCallback callback) {
        validate(pin);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PinEntry.COLUMN_NAME_UID, pin.getUid());
        values.put(PinEntry.COLUMN_NAME_PIN_NAME, pin.getName());
        values.put(PinEntry.COLUMN_NAME_LAT, pin.getLat());
        values.put(PinEntry.COLUMN_NAME_LNG, pin.getLng());

        long id = db.insert(PinEntry.TABLE_NAME, null, values);
        db.close();

        if (id != -1) {
            pin.setId(id);
            callback.onPinSaved(pin);
        } else {
            callback.onPinSaveError();
        }
    }

    @Override
    public void deletePin(Pin pin, OnPinRemoveCallback callback) {
        validate(pin);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String clause = PinEntry.COLUMN_NAME_LAT + " = ? AND " + PinEntry.COLUMN_NAME_LNG +
                " = ? AND " + PinEntry.COLUMN_NAME_UID + " = ?";

        String[] args = {String.valueOf(pin.getLat()), String.valueOf(pin.getLng()), pin.getUid()};

        int deletedCount = db.delete(PinEntry.TABLE_NAME, clause, args);
        db.close();

        if (deletedCount != 0) {
            callback.onPinRemoveSuccess();
        } else {
            callback.OnPinRemoveError();
        }
    }

    @Override
    public void deletePin(long id, OnPinRemoveCallback callback) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String clause = PinEntry._ID + " = ? ";

        String[] args = {String.valueOf(id)};

        int deletedCount = db.delete(PinEntry.TABLE_NAME, clause, args);
        db.close();

        if (deletedCount != 0) {
            callback.onPinRemoveSuccess();
        } else {
            callback.OnPinRemoveError();
        }
    }

    @Override
    public void updatePin(Pin pin, OnPinUpdatedCallback callback) {
        validate(pin);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PinEntry.COLUMN_NAME_UID, pin.getUid());
        values.put(PinEntry.COLUMN_NAME_PIN_NAME, pin.getName());
        values.put(PinEntry.COLUMN_NAME_LAT, pin.getLat());
        values.put(PinEntry.COLUMN_NAME_LNG, pin.getLng());

        int rowsAffected = db.update(PinEntry.TABLE_NAME, values, PinEntry._ID + "=" + pin.getId(), null);
        db.close();

        if (rowsAffected > 0) {
            callback.onPinUpdated();
        } else {
            callback.onPinUpdateError();
        }
    }

    @Override
    public void deleteAllPins(String uid, OnAllPinsRemovedCallback callback) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String clause = PinEntry.COLUMN_NAME_UID + " = ? ";

        String[] args = {String.valueOf(uid)};

        int deletedCount = db.delete(PinEntry.TABLE_NAME, clause, args);
        db.close();

        if (deletedCount != 0) {
            callback.onAllPinsRemoved();
        } else {
            callback.onRemoveAllPinsError();
        }
    }

    private Pin parsePin(Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow(PinEntry._ID));
        String name = c.getString(c.getColumnIndexOrThrow(PinEntry.COLUMN_NAME_PIN_NAME));
        double lat = c.getDouble(c.getColumnIndexOrThrow(PinEntry.COLUMN_NAME_LAT));
        double lng = c.getDouble(c.getColumnIndexOrThrow(PinEntry.COLUMN_NAME_LNG));
        String uid = c.getString(c.getColumnIndexOrThrow(PinEntry.COLUMN_NAME_UID));

        return  new Pin(name, lat, lng, uid, id);
    }
}