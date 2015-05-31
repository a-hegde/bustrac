package com.ibangalore.bustrac.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by ahegde on 5/13/15.
 */
public class TrackerDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "bustrac.db";
    private static final int DATABASE_VERSION = 2;
    private static final String LOG_TAG = TrackerDBHelper.class.getSimpleName();

    public TrackerDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(LOG_TAG, "Constructor");
    }
    @Override
    public void onCreate(SQLiteDatabase tracDB) {

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + TrackerContract.LocationEntry.TABLE_NAME
                + " ( " + TrackerContract.LocationEntry._ID + " INTEGER PRIMARY KEY, "
                + TrackerContract.LocationEntry.COLUMN_ROUTE_NUM + " INTEGER NOT NULL, "
                + TrackerContract.LocationEntry.COLUMN_DESTINATION + " TEXT, "
                + TrackerContract.LocationEntry.COLUMN_DIRECTION + " TEXT, "
                + TrackerContract.LocationEntry.COLUMN_LATITUDE + " REAL NOT NULL, "
                + TrackerContract.LocationEntry.COLUMN_LONGITUDE + " REAL NOT NULL, "
                + TrackerContract.LocationEntry.COLUMN_VEHICLE_ID + " INTEGER ); " ;

        final String SQL_CREATE_ROUTE_TABLE = "CREATE TABLE " + TrackerContract.RoutesMaster.TABLE_NAME
                + " ( " + TrackerContract.LocationEntry._ID + " INTEGER PRIMARY KEY, "
                + TrackerContract.RoutesMaster.COLUMN_ROUTE_NUM + " INTEGER NOT NULL, "
                + TrackerContract.RoutesMaster.COLUMN_ROUTE_DESC + " TEXT, "
                + TrackerContract.RoutesMaster.COLUMN_DIRECTION + " TEXT ); " ;



        Log.d(LOG_TAG, "About to drop table location and routes");
        try{
            tracDB.execSQL("drop table "+ TrackerContract.LocationEntry.TABLE_NAME+" ;");
            tracDB.execSQL("drop table "+ TrackerContract.RoutesMaster.TABLE_NAME+" ;");
        }catch (SQLiteException e){
            Log.d(LOG_TAG, "No existing table, carry on. Error - " + e.toString());
        }

        Log.d(LOG_TAG, "About to create table location");
        tracDB.execSQL(SQL_CREATE_LOCATION_TABLE);
        tracDB.execSQL(SQL_CREATE_ROUTE_TABLE);

        Log.d(LOG_TAG, "Done with table creation");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.onCreate(db);
    }
}
