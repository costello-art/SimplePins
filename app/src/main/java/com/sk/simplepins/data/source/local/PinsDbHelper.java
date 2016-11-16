package com.sk.simplepins.data.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sk.simplepins.data.source.local.PinsPersistenceContract.PinEntry;

/**
 * Created by Sviat on Nov 06, 2016.
 */

public class PinsDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SimplePins";
    private static final int DATABASE_VERSION = 4;

    private static final String COMMA_SEP = ",";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String TEXT_TYPE = " TEXT";

    private static final String CREATE_TABLE_PINS =
            "CREATE TABLE " + PinEntry.TABLE_NAME + " (" +
                    PinEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
                    PinEntry.COLUMN_NAME_UID + INTEGER_TYPE + COMMA_SEP +
                    PinEntry.COLUMN_NAME_PIN_NAME + TEXT_TYPE + COMMA_SEP +
                    PinEntry.COLUMN_NAME_LAT + REAL_TYPE + COMMA_SEP +
                    PinEntry.COLUMN_NAME_LNG + REAL_TYPE + ")";

    private static final String DROP_TABLE_PINS = "DROP TABLE IF EXISTS " + PinEntry.TABLE_NAME;

    public PinsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PINS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_PINS);
        db.execSQL(CREATE_TABLE_PINS);
    }
}