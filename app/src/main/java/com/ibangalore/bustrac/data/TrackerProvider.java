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
    static final int ROUTE_MASTER = 300;
    static final int STATION_MASTER = 400;
    static final int STATION = 500;

    private static final String LOG_TAG = TrackerProvider.class.getSimpleName();

    private static final SQLiteQueryBuilder sBusLocationsQueryBuilder;
    private static final SQLiteQueryBuilder sRoutesQueryBuilder;
    private static final SQLiteQueryBuilder sStationsQueryBuilder;

    static {
        sBusLocationsQueryBuilder = new SQLiteQueryBuilder();
        sBusLocationsQueryBuilder.setTables(TrackerContract.LocationEntry.TABLE_NAME);
        sRoutesQueryBuilder = new SQLiteQueryBuilder();
        sRoutesQueryBuilder.setTables(TrackerContract.RoutesMaster.TABLE_NAME);
        sStationsQueryBuilder = new SQLiteQueryBuilder();
        sStationsQueryBuilder.setTables(TrackerContract.StationsMaster.TABLE_NAME);
    }

    private static final String sBusLocationsSelection =
            TrackerContract.LocationEntry.TABLE_NAME + "."+
                    TrackerContract.LocationEntry.COLUMN_ROUTE_NUM +"=?";

    private static final String sStationNameSelection =
            TrackerContract.StationsMaster.TABLE_NAME + "."+
                    TrackerContract.StationsMaster.COLUMN_STATION_ID +"=?";



    @Override
    public boolean onCreate() {
        mOpenHelper = new TrackerDBHelper(getContext());
        return true;
    }

    static UriMatcher buildUriMatcher(){

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(TrackerContract.CONTENT_AUTHORITY, TrackerContract.PATH_LOCATION, ALL_BUS_LOCATIONS);
        uriMatcher.addURI(TrackerContract.CONTENT_AUTHORITY, TrackerContract.PATH_LOCATION + "/*", BUS_LOCATIONS);
        uriMatcher.addURI(TrackerContract.CONTENT_AUTHORITY, TrackerContract.PATH_ROUTES, ROUTE_MASTER);
        uriMatcher.addURI(TrackerContract.CONTENT_AUTHORITY, TrackerContract.PATH_STATIONS, STATION_MASTER);
        uriMatcher.addURI(TrackerContract.CONTENT_AUTHORITY, TrackerContract.PATH_STATIONS + "/*", STATION);
        return uriMatcher;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        Log.d(LOG_TAG, "Query Func started with uri="+uri);
        Log.d(LOG_TAG, "Uri Matcher with stn id returns ="+
                sUriMatcher.match(Uri.parse("content://com.ibangalore.bustrac.provider/stations/90401")));
        Log.d(LOG_TAG, "Uri Matcher with stn name  returns ="+
                sUriMatcher.match(Uri.parse("content://com.ibangalore.bustrac.provider/stations/Wayne")));


        switch(sUriMatcher.match(uri)) {
            case ALL_BUS_LOCATIONS:
                //Execution falls through to below code
            case BUS_LOCATIONS:{
                retCursor = getBusLocations(uri, projection, sortOrder);
                break;
            }
            case ROUTE_MASTER:{
                retCursor = getRouteMaster(uri, projection, sortOrder);
                break;
            }
            case STATION:{
                retCursor = getStationName(uri);
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

    private Cursor getRouteMaster(Uri uri, String[] projection,
                                   String sortOrder){

        return sRoutesQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

    }

    private Cursor getStationMaster(Uri uri, String[] projection,
                                  String sortOrder){

        return sStationsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

    }

    private Cursor getStationName(Uri uri){
        String stationId = TrackerContract.StationsMaster.getStationIdFromUri(uri);
        String[] selectionArgs = new String[] {stationId};
        String selection = sStationNameSelection;
        String[] projection = new String[]{TrackerContract.StationsMaster.COLUMN_STATION_NAME};

        return sStationsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

    }


    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(LOG_TAG, " insert function started with uri = "+uri);
        Log.d(LOG_TAG, "Uri Matcher with stn id returns ="+
                sUriMatcher.match(uri));

        final SQLiteDatabase tracDB = mOpenHelper.getWritableDatabase();
        Uri retUri = null;

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
            case ROUTE_MASTER:{
                long id = tracDB.insert(TrackerContract.RoutesMaster.TABLE_NAME, null, values);
                if (id > 0){
                    String routeNum = values.getAsString(TrackerContract.RoutesMaster.COLUMN_ROUTE_NUM);
                    retUri = TrackerContract.RoutesMaster
                            .buildRoutesUri(routeNum);
                }
                else throw new SQLException("Failed to insert rows for "+ uri);
                break;
            }
            case STATION_MASTER:{
                long id = tracDB.insert(TrackerContract.StationsMaster.TABLE_NAME, null, values);
                if (id > 0){
                    String stationName = values.getAsString(TrackerContract.StationsMaster.COLUMN_STATION_NAME);
                    retUri = TrackerContract.StationsMaster
                            .buildStationsUriFromName(stationName);
                }
                else Log.d(LOG_TAG, "insert failed for "+ values);
                break;
            }

            default:
                Log.d(LOG_TAG, sUriMatcher.match(uri) +" did not match "+ALL_BUS_LOCATIONS+", "+BUS_LOCATIONS+" or "+ STATION_MASTER);
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, " delete function started");
        final SQLiteDatabase tracDB = mOpenHelper.getWritableDatabase();
        Uri retUri;

        switch(sUriMatcher.match(uri)) {
            case ALL_BUS_LOCATIONS:
                //Execution falls through to below code
            case BUS_LOCATIONS:{
                //To Do - Put code here for delete
                break;
            }
            case ROUTE_MASTER:{
                return tracDB.delete(TrackerContract.RoutesMaster.TABLE_NAME, null, null);
            }
            case STATION_MASTER :{
                return tracDB.delete(TrackerContract.StationsMaster.TABLE_NAME, null, null);
            }

            default:
                Log.d(LOG_TAG, sUriMatcher.match(uri) +" did not match "+ALL_BUS_LOCATIONS
                        +", "+BUS_LOCATIONS+" or "+ROUTE_MASTER);
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
