package com.sk.simplepins.data.source.local;

import android.provider.BaseColumns;

/**
 * Created by Sviat on Nov 09, 2016.
 */
public final class PinsPersistenceContract {

    private PinsPersistenceContract() {
    }

    public static abstract class PinEntry implements BaseColumns {
            static final String TABLE_NAME = "pins";
            static final String COLUMN_NAME_UID = "user_id";
            static final String COLUMN_NAME_PIN_NAME = "name";
            static final String COLUMN_NAME_LAT = "lat";
            static final String COLUMN_NAME_LNG = "lng";
        }
}