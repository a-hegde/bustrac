package com.ibangalore.bustrac;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ibangalore.bustrac.UXAssets.ArrivalsLViewAdapter;
import com.ibangalore.bustrac.UXAssets.ArrivalsRowItem;
import com.ibangalore.bustrac.data.TrackerContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Created by ahegde on 6/13/15.
 */
public class NextBusFragment extends Fragment {
    private static final String LOG_TAG=NextBusFragment.class.getSimpleName();
    public static final String KEY_STATION_CODE = "station_code";
    Vector<ContentValues> mContentValuesVector;
    ArrivalsLViewAdapter mArrivalsAdapter;
    public String stationCode = "90401";
    GoogleMap mMap;
    LatLng mStationLoc = new LatLng(39.95, -75.17);
    LatLng mMapCenter = new LatLng(39.95, -75.17);
    boolean mMapIsTop = false;
    private static final float DEFAULT_ZOOM = 13;

    //Create a Mapping from individual markers to corresponding station ids
    private Map<Marker, Integer> markerMap = new HashMap<Marker, Integer>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(LOG_TAG, "func onCreateView starts");
        Bundle  arguments = getArguments();
        if (arguments != null){
            if(arguments.containsKey(NextBusFragment.KEY_STATION_CODE)){
                this.stationCode = String.valueOf(arguments.getInt(NextBusFragment.KEY_STATION_CODE));
                Log.d(LOG_TAG, "onCreateView, stationCode set to "+this.stationCode );

            }

        }

        ArrayList<ArrivalsRowItem> arrayArrivals = new ArrayList<ArrivalsRowItem>();
        mArrivalsAdapter = new ArrivalsLViewAdapter(getActivity(), arrayArrivals);

        View rootView = inflater.inflate(R.layout.next_bus, null, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_bus_arrivals);
        listView.setAdapter(mArrivalsAdapter);

        TextView stationNameTV = (TextView) rootView.findViewById(R.id.station_name_TV);
        ImageView circleMaskImgV = (ImageView) rootView.findViewById(R.id.img_circle_mask);
        circleMaskImgV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout mapFrame = (FrameLayout) getActivity().findViewById(R.id.back_map);
                mapFrame.bringToFront();
                mMapIsTop = true;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMapCenter, DEFAULT_ZOOM));
//                mapFrame.invalidate();
            }
        });

        new DownloadArrivals().execute();
        return rootView;

//        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private class DownloadArrivals extends AsyncTask<Void, Void, ArrayList<ArrivalsRowItem>>
            implements OnMapReadyCallback {

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        private final String LOG_TAG = DownloadArrivals.class.getSimpleName();
        double mMask2ScreenWidthRatio;
        double mMask2ScreenHeightRatio;

        @Override
        protected void onPreExecute() {
            Log.d(LOG_TAG, "func onPreExecute starts - show hourglass spin");
            progressDialog.show();

        }

        protected ArrayList<ArrivalsRowItem> doInBackground(Void... urls) {
            Log.d(LOG_TAG, "func doInBackground starts - about to call fetchArrivals");

            ArrayList<ArrivalsRowItem> arrivalsArrayList = fetchArrivals();
            if (arrivalsArrayList == null) {
                arrivalsArrayList = new ArrayList<ArrivalsRowItem>();
                arrivalsArrayList.add(new ArrivalsRowItem("Not able to provide you data now", null, null, null));
                arrivalsArrayList.add(new ArrivalsRowItem("Come back later and try your luck", null, null, null));
            }

            return arrivalsArrayList;
        }

        private ArrayList<ArrivalsRowItem> fetchArrivals() {
            Log.d(LOG_TAG, "func fetchArrivals starts");
            ArrayList<ArrivalsRowItem> arrivalsArrayList = new ArrayList<ArrivalsRowItem>();
            ArrayList<ArrivalsRowItem> errorArrayList = new ArrayList<ArrivalsRowItem>();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuffer buffer = null;

            URL url = null;
            String baseArrivalsUri = "http://www3.septa.org/hackathon/Arrivals";

            String stationCode = NextBusFragment.this.stationCode;
            Log.d(LOG_TAG, "Station Code = " + NextBusFragment.this.stationCode);

            String arrivalsJsonStr = null;

            try {
                // Add station code at the end of the Url
                // There is one more, optional parameter at the end for the number of results. Default is 5.
                Uri uri = Uri.parse(baseArrivalsUri).buildUpon().appendPath(stationCode).build();
                url = new URL(uri.toString());
                Log.d(LOG_TAG, "Fetching URL " + url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                errorArrayList.add(new ArrivalsRowItem(null, "MalformedURLException", null, null));
                return errorArrayList;
            }
            try {
                // Create the request to site, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    //Nothing to do
                    Log.d(LOG_TAG, "Got a null inputStream, we are done here");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                // you could condense the above block to online like this:
                //reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                buffer = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    // Adding a newline  won't affect JSON parsing
                    // But if you print out the buffer for debugging, it makes it easier to read.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    Log.d(LOG_TAG, "Got an empty buffer, we are done here");
                    return null;
                }

            } catch (IOException e) {
                e.printStackTrace();
                errorArrayList.add(new ArrivalsRowItem(null, "IOException", null, null));
                return errorArrayList;
            }

            arrivalsJsonStr = buffer.toString();
//            errorArrayList.add(locationJsonStr);
//            return errorArrayList;

            //Now we have the JSON data, parse it to get the sub-components

            try {
                JSONObject arrivalsJson = new JSONObject(arrivalsJsonStr);
                errorArrayList.add(new ArrivalsRowItem(null, "fetchArrivals: converted string to JsonObj. Now extract keys for JSONobj: "
                        , null, null));
                // Get the first key, which is long string with the station name and time stamp
                Iterator<String> myIter = arrivalsJson.keys();
                String stationNameEtc="";
                if(myIter.hasNext())
                    stationNameEtc = myIter.next();

                // The first key of the JSon is of the format "Glenolden Departures: June 23, 2015, 10:50 am"
                // So extract the station name, which is everything till the string Departures
                String stationName = stationNameEtc.substring(0, stationNameEtc.indexOf("Departures") - 2);

                JSONArray outerArray = arrivalsJson.getJSONArray(stationNameEtc);
                JSONObject arrivalsNorthObj = outerArray.getJSONObject(0);
                JSONArray arrivalsNorthArray = arrivalsNorthObj.getJSONArray("Northbound");



                JSONObject arrivalsSouthObj = outerArray.getJSONObject(1);
                JSONArray arrivalsSouthArray = arrivalsSouthObj.getJSONArray("Southbound");

                errorArrayList.add(new ArrivalsRowItem(null, "Got North and South: "+arrivalsNorthArray.toString()+'\n'
                + arrivalsSouthArray.toString(), null, null));

                mContentValuesVector = new Vector<ContentValues>();

                ContentValues arrivalsValues = new ContentValues();

                for (int i = 0; i < arrivalsNorthArray.length(); ++i) {
                    JSONObject busObject = arrivalsNorthArray.getJSONObject(i);
                    errorArrayList.add(new ArrivalsRowItem(null, "fetchBusLocation: Got individual bus " + i + " = " + busObject.toString()
                            , null, null));

                    //Separating out the individual elements and storing them in a ContentValue Vector
                    arrivalsValues.put("Route_Num",
                            busObject.getString("train_id"));
                    arrivalsValues.put("Destination",
                            busObject.getString("destination"));
                    arrivalsValues.put("Direction",
                            busObject.getString("direction"));
                    arrivalsValues.put("ETA",
                            busObject.getString("depart_time"));

                    mContentValuesVector.add(arrivalsValues);
                    arrivalsValues = new ContentValues();

                    // Time is of this format - Jun 14 2015 01:35:00:000PM
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy hh:mm:ss:SSSa");
                    sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                    int etaMins=0;
                    String twoTimes="blanco";
                    try{
                        Date eta = sdf.parse(busObject.getString("depart_time"));
                        Date currentTime = new Date();
                        errorArrayList.add(new ArrivalsRowItem(null, "Current time is == "+currentTime.toString(), null, null));
                        twoTimes = "Both Timings: [" +eta.toString()+" - "+currentTime.toString()+"]" ;
                        errorArrayList.add(new ArrivalsRowItem(null, twoTimes, null, null));
                        etaMins = (int)((eta.getTime()/60000) - (currentTime.getTime()/60000));

                    }catch (ParseException e){
                        errorArrayList.add(new ArrivalsRowItem(null, "We had a datetime format problem: "+e.toString()
                                , null, null));
                    }

                    //Creating a composite string for display purposes and adding it to an ArrayList
                    //that will be used to update the ArrayAdapter linked to the listView
                    String oneLine = busObject.getString("train_id") + " | "
                            + busObject.getString("destination")
                            + " via " + busObject.getString("direction")
                            + " | " + String.valueOf(etaMins) +" mins"
                            + " [" +busObject.getString("depart_time").substring(12,20)+ "]" ;
                    arrivalsArrayList.add(new ArrivalsRowItem(busObject.getString("train_id"),
                            busObject.getString("destination"),
                            busObject.getString("direction"),
                            String.valueOf(etaMins)
                    ));
/*debug - remove later*/    errorArrayList.add(new ArrivalsRowItem(null, oneLine, null, null));
                }

            } catch (JSONException e) {
                e.printStackTrace();
                errorArrayList.add(new ArrivalsRowItem(null, "JSONException - looks like approach didn't work"
                        , null, null));
                return errorArrayList;
            }
            return arrivalsArrayList;


        } //End method fetchBusLocations

        @Override
        protected void onPostExecute(ArrayList<ArrivalsRowItem> arrivalsArrayList) {
            Log.d(LOG_TAG, "Reached onPostExecute");
            progressDialog.dismiss();
            String stationName=null;

            //Get the station name from the station id to put at top of screen.
            Uri uri = TrackerContract.StationsMaster.buildStationsUriFromID(stationCode);
            Log.d(LOG_TAG,"Uri we constructed is "+uri);
            Cursor c = getActivity().getContentResolver().query(
                    uri,
                    null,
                    null,
                    null,
                    null
            );
            while (c.moveToNext()){
                stationName = c.getString(c.getColumnIndex(TrackerContract.StationsMaster.COLUMN_STATION_NAME));
            }

            TextView stationNameTV = (TextView) getActivity().findViewById(R.id.station_name_TV);
            stationNameTV.setText(stationName);

            // Populate the arrivals Array List we fetched in the background execution
            if (arrivalsArrayList == null)
                return;
            for (ArrivalsRowItem bus : arrivalsArrayList) {
                Log.d(LOG_TAG, "> " + bus);
            }

            mArrivalsAdapter.clear();
            mArrivalsAdapter.addAll(arrivalsArrayList);
            mArrivalsAdapter.notifyDataSetChanged();

            // Create background map and position it to center on bus stop in circle
            MapFragment backMapFragment = MapFragment.newInstance();

            backMapFragment.getMapAsync(this);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            //Replace whatever is in this container with the new (maps) fragment.
            transaction.replace(R.id.back_map, backMapFragment);
            transaction.addToBackStack(null);
            transaction.commit();


        } //End method postExecute


        /************************
         * Callback function invoked once the map is ready to render.
         * This is the point to specify where the maps is to be centered and what zoom.
         * Also set lat long for the markers that will show up.
         *************************/
        @Override
        public void onMapReady(final GoogleMap map){
                map.addMarker(new MarkerOptions()
                        .position(mStationLoc)
                        .title(stationCode));
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mStationLoc, DEFAULT_ZOOM));


            // Code to reposition the map so the station marker shows in the circular window.
            int circleMaskWidth = getActivity().findViewById(R.id.img_circle_mask).getMeasuredWidth();
            int circleMaskHeight = getActivity().findViewById(R.id.img_circle_mask).getMeasuredHeight();

            int stationNameWidth = getActivity().findViewById(R.id.station_name_TV).getMeasuredWidth();
            int busArvlsHeight = getActivity().findViewById(R.id.listview_bus_arrivals).getMeasuredHeight();

            Log.d(LOG_TAG,"circleMaskWidth, circleMaskHeight, stationNameWidth, busArvlsHeight: "
                    +circleMaskWidth+": "+circleMaskHeight+": "+stationNameWidth+": "+busArvlsHeight);

            mMask2ScreenWidthRatio = 1.00*circleMaskWidth/(circleMaskWidth+stationNameWidth);
            mMask2ScreenHeightRatio = 1.00*circleMaskHeight/(circleMaskHeight+busArvlsHeight);
            Log.d(LOG_TAG, "Screen ratios: Width Ratio=" + mMask2ScreenWidthRatio + " & Height Ratio=" + mMask2ScreenHeightRatio);

/*
            LatLng mapLeft = map.getProjection().getVisibleRegion().farLeft;
            LatLng mapRight = map.getProjection().getVisibleRegion().nearRight;
            LatLngBounds mapBounds = map.getProjection().getVisibleRegion().latLngBounds;
            Log.d(LOG_TAG, "farLeft=" + mapLeft + " & nearRight=" + mapRight);
            Log.d(LOG_TAG, "Map Bounds=" + mapBounds);
*/

            View mapView = getFragmentManager().findFragmentById(R.id.back_map).getView();
            mMap = map;
            if (mapView.getViewTreeObserver().isAlive()){
                mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        LatLngBounds mapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                        Log.d(LOG_TAG, "onGlobalLayoutListener Map Bounds=" + mapBounds);
                        if (true == mMapIsTop){
                            // Get markers for all stations in visible area of map.
                            String [] selectArgs = new String[] {
                                    String.valueOf(mapBounds.southwest.latitude), String.valueOf(mapBounds.northeast.latitude),
                                    String.valueOf(mapBounds.southwest.longitude), String.valueOf(mapBounds.northeast.longitude)
                            };
                            Cursor c = getActivity().getContentResolver().query(TrackerContract.StationsMaster.CONTENT_URI,
                                    null,
                                    "latitude between ? and ? and longitude between ? and ?",
                                    selectArgs,
                                    null);
                            while (c.moveToNext()){
                                LatLng stationPoint = new LatLng(c.getDouble(c.getColumnIndex(TrackerContract.StationsMaster.COLUMN_LATITUDE)),
                                c.getDouble(c.getColumnIndex(TrackerContract.StationsMaster.COLUMN_LONGITUDE)));
                                Integer stationId = c.getInt(c.getColumnIndex(TrackerContract.StationsMaster.COLUMN_STATION_ID));
                                String stationName = c.getString(c.getColumnIndex(TrackerContract.StationsMaster.COLUMN_STATION_NAME));

                                Log.d(LOG_TAG, "In query cursor, got station> " + stationName + " point: " + stationPoint
                                        + " id:"+stationId);
                                Marker m = mMap.addMarker(new MarkerOptions()
                                        .position(stationPoint)
                                        .title(c.getString(c.getColumnIndex(TrackerContract.StationsMaster.COLUMN_STATION_NAME))));
                                //add mapping of markers to station ids to marker Map for use in InfoWindow Click Listener
                                markerMap.put(m, stationId);
                            }
                            return;
                        }
                        else{
                            // Map is underneath the ListView. Reposition so that marker on current station shows up
                            // in the circular window next to station name. Some messy calculation required to
                            // figure out where map should be repositioned is done in getCornerLocation().
                            // Call the procedure and reposition map.

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getCornerLocation(mapBounds), DEFAULT_ZOOM));
                        }

                    }
                });
            }

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    int stationId = markerMap.get(marker);
                    NextBusFragment nextBusFragment = new NextBusFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(NextBusFragment.KEY_STATION_CODE, stationId);
                    nextBusFragment.setArguments(bundle);

                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.body, nextBusFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();

                }
            });

        } //end function onMapReady

        // Map is underneath the ListView. Reposition so that marker on current station shows up
        // in the circular window next to station name. Some messy calculation required to
        // figure out where map should be repositioned is done here.
        private LatLng getCornerLocation(LatLngBounds mapBounds){
            double mapWidth = Math.abs(mapBounds.northeast.longitude - mapBounds.southwest.longitude);
            double mapHeight = Math.abs(mapBounds.northeast.latitude - mapBounds.southwest.latitude);
//            Log.d(LOG_TAG, "onGlobalLayoutListener Map width & height=" + mapWidth + "/" + mapHeight);

            mMapCenter = mMap.getCameraPosition().target;
            double mapCenterLat = mMapCenter.latitude;
            double mapCenterLng = mMapCenter.longitude;

            // TO move to top left screen corner, subtract from latitude, add to longitude
            double cornerLat = mapCenterLat -  (mapHeight/2) + (mMask2ScreenHeightRatio*mapHeight/2);
            double cornerLng = mapCenterLng + (mapWidth/2) - (mMask2ScreenWidthRatio * mapWidth / 2);
            Log.d(LOG_TAG, "Map Corner=" + cornerLat + "/" + cornerLng);
            return new LatLng(cornerLat, cornerLng);
        }


    } //end private class DownloadArrivals

}// end class NextBusFragment
