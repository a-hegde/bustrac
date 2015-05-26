package com.ibangalore.bustrac.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by ahegde on 5/16/15.
 */
public class TrackerProvider extends ContentProvider {
    private TrackerDBHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    static final int BUS_LOCATIONS = 100;
    static final int ALL_BUS_LOCATIONS = 200;

    private static final String LOG_TAG = TrackerProvider.class.getSimpleName();

    private static final SQLiteQueryBuilder sBusLocationsQueryBuilder;

    static {
        sBusLocationsQueryBuilder = new SQLiteQueryBuilder();
        sBusLocationsQueryBuilder.setTables(TrackerContract.LocationEntry.TABLE_NAME);
    }

    private static final String sBusLocationsSelection =
            TrackerContract.LocationEntry.TABLE_NAME + "."+
                    TrackerContract.LocationEntry.COLUMN_ROUTE_NUM +"=?";

    @Override
    public boolean onCreate() {
        mOpenHelper = new TrackerDBHelper(getContext());
        return true;
    }

    static UriMatcher buildUriMatcher(){

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(TrackerContract.CONTENT_AUTHORITY, TrackerContract.PATH_LOCATION, ALL_BUS_LOCATIONS);
        uriMatcher.addURI(TrackerContract.CONTENT_AUTHORITY, TrackerContract.PATH_LOCATION + "/*", BUS_LOCATIONS);

        return uriMatcher;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch(sUriMatcher.match(uri)) {
            case ALL_BUS_LOCATIONS:
                //Execution falls through to below code
            case BUS_LOCATIONS:{
                retCursor = getBusLocations(uri, projection, sortOrder);
                break;
            }
            default:
                Log.d(LOG_TAG, sUriMatcher.match(uri) +" did not match "+ALL_BUS_LOCATIONS+" or "+BUS_LOCATIONS);
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor getBusLocations(Uri uri, String[] projection,
                                   String sortOrder){
        String routeNum = TrackerContract.LocationEntry.getRouteNumFromUri(uri);
        String[] selectionArgs = new String[] {routeNum};
        String selection = sBusLocationsSelection;

        return sBusLocationsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

    }


    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(LOG_TAG, " insert function started");
        final SQLiteDatabase tracDB = mOpenHelper.getWritableDatabase();
        Uri retUri;

        switch(sUriMatcher.match(uri)) {
            case ALL_BUS_LOCATIONS:
                //Execution falls through to below code
            case BUS_LOCATIONS:{
                long id = tracDB.insert(TrackerContract.LocationEntry.TABLE_NAME, null, values);
                if (id > 0){
                    String routeNum = values.getAsString(TrackerContract.LocationEntry.COLUMN_ROUTE_NUM);
                    retUri = TrackerContract.LocationEntry
                            .buildLocationUri(routeNum);
                }
                else throw new SQLException("Failed to insert rows for "+ uri);
                break;
            }
            default:
                Log.d(LOG_TAG, sUriMatcher.match(uri) +" did not match "+ALL_BUS_LOCATIONS+" or "+BUS_LOCATIONS);
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
