package com.sk.simplepins.data;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Sviat on Nov 06, 2016.
 */

public class Pin {
    private long id;
    private String name;
    private double lat;
    private double lng;
    private String uid;

    public Pin(Marker m, String uid) {
        this(m.getTitle(), m.getPosition().latitude, m.getPosition().longitude, uid);
        this.id = (long) m.getTag();
    }

    public Pin(MarkerOptions m, String uid) {
        this(m.getTitle(), m.getPosition().latitude, m.getPosition().longitude, uid);
    }

    public Pin(String name, double lat, double lng, String uid, long id) {
        this(name, lat, lng, uid);
        this.id = id;
    }

    public Pin(String name, double lat, double lng, String uid) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.uid = uid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}