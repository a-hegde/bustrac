package com.ibangalore.bustrac.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ahegde on 5/15/15.
 */
public class TrackerContract {

    public static final String CONTENT_AUTHORITY = "com.ibangalore.bustrac.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+ CONTENT_AUTHORITY);

    public static final String PATH_LOCATION = "location";
    public static final String PATH_ROUTES = "routes";


    public static final class LocationEntry implements BaseColumns{
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_VEHICLE_ID = "vehicle_id";
        public static final String COLUMN_DIRECTION = "direction";
        public static final String COLUMN_DESTINATION = "destination";
        public static final String COLUMN_ROUTE_NUM = "route_num";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_LOCATION;


        public static final Uri buildLocationUri(String routeNum){
            return CONTENT_URI.buildUpon().appendPath(routeNum).build();
        }

        public static String getRouteNumFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

     }

    public static final class RoutesMaster implements BaseColumns{
        public static final String TABLE_NAME = "routes_master";

        //route number need not be a number - it is often alphanumeric
        public static final String COLUMN_ROUTE_NUM = "route_num";

        public static final String COLUMN_ROUTE_DESC = "route_name";
        public static final String COLUMN_DIRECTION = "direction";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_ROUTES).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_LOCATION;


        public static final Uri buildRoutesUri(String routeNum){
            return CONTENT_URI.buildUpon().appendPath(routeNum).build();
        }


    }



}
