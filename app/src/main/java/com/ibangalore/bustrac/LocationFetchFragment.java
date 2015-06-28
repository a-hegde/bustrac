package com.ibangalore.bustrac;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ibangalore.bustrac.data.TrackerContract;
import com.ibangalore.bustrac.kml.RouteDataSet;
import com.ibangalore.bustrac.kml.TransitSAXHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Class to fetch the current bus location by fetching from APIs exposed by Transit Agency
 */
public class LocationFetchFragment extends Fragment{

    OnBusItemSelectedListener mCallback;
    public final String LOG_TAG = LocationFetchFragment.class.getSimpleName();
    ArrayAdapter<String> mBusLocAdapter;
    Vector<ContentValues> mContentValuesVector;
    RouteDataSet mRouteDataSet;
    String mBusRoute = "23";

    /* The calling activity must implement this interface */
    public interface OnBusItemSelectedListener {
        //Called by LocationFetchFragment when an item from the list view is selected
        public void onBusItemSelected(Vector<ContentValues> busPositionsCVV, RouteDataSet routeDataSet);
    }


    //Constructor
    public LocationFetchFragment() {
        Log.d(LOG_TAG, "constructor");
        Bundle bundle = getArguments();

        if (bundle != null && bundle.containsKey(MapsActivity.KEY_BUS_ROUTE)){
            mBusRoute = bundle.getString(MapsActivity.KEY_BUS_ROUTE);
            Log.d(LOG_TAG, " in constructor, mBusRoute set to "+mBusRoute);

        }
        else {
            Log.d(LOG_TAG, "Constructor - Didn't get route in bundle, falling back to default");
            mBusRoute = "23";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        Bundle  arguments = getArguments();
        if (arguments != null){
            if(arguments.containsKey(MapsActivity.KEY_BUS_ROUTE)){
                mBusRoute = arguments.getString(MapsActivity.KEY_BUS_ROUTE);
                Log.d(LOG_TAG, "onCreateView, mBusRoute set to "+mBusRoute);

            }

        }
        ArrayList<String> busLocArrayList = new ArrayList<>();
        busLocArrayList.add("[39.96/-75.15] to Chestnut Hill, NorthBound (5617)");
        busLocArrayList.add("[39.91/-75.16] to Broad - Oregon, SouthBound (8235)");

        Log.d(LOG_TAG, "onCreateView: initialize Array Adapter");

        mBusLocAdapter = new ArrayAdapter<String>(
                getActivity(),                        // The current context (this activity)
                R.layout.bus_list_item,       // The name of the layout ID
                busLocArrayList);

        View rootView = inflater.inflate(R.layout.bus_location_fragment, null, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_bus_coords);
        listView.setAdapter(mBusLocAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                double lat = mContentValuesVector.get(position).getAsDouble(TrackerContract.LocationEntry.COLUMN_LATITUDE);
                double lng = mContentValuesVector.get(position).getAsDouble(TrackerContract.LocationEntry.COLUMN_LONGITUDE);
                Log.d(LOG_TAG, "Lat = "+lat+" and Long = "+lng+ "at position "+position);
                try {
                    Log.d(LOG_TAG, "Content Values at 3 = " + mContentValuesVector.get(3).toString());
                    Log.d(LOG_TAG, "Content Values at 4 = " + mContentValuesVector.get(4).toString());
                }catch (ArrayIndexOutOfBoundsException e){
                    Log.d(LOG_TAG, "Looks like we have 3 or less buses showing up");
                }
                mCallback.onBusItemSelected(mContentValuesVector, mRouteDataSet);

            }
        });


        Button fetchButton = (Button) rootView.findViewById(R.id.fetch_button);
        fetchButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLocations(v);
            }
        });

        /****Temporarily populate a few rows into route master****/
        populateMasterTables();

        return rootView;
    }

    public void refreshLocations(View view){
        new DownloadBusLocation().execute();
    }


    @Override
    public void onResume(){
        super.onResume();
        Log.d(LOG_TAG, "onResume");

    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            mCallback = (OnBusItemSelectedListener) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString()
            + " must implement OnBusItemSelectedListener");
        }
    }

    private void populateMasterTables(){
        Uri routesUri = TrackerContract.RoutesMaster.CONTENT_URI;
        Uri stationsUri = TrackerContract.StationsMaster.CONTENT_URI;

        // delete all rows from route master table
        int delRows = getActivity().getContentResolver().delete(routesUri, null, null);
        Log.d(LOG_TAG, "Deleted rows = "+delRows);

        //now create ContentValues of rows to insert.
        Log.d(LOG_TAG, "Inserting into routes master ");
        ContentValues[] routeMasterCV = new ContentValues[6];

        routeMasterCV[0]  = new ContentValues();
        routeMasterCV[1]  = new ContentValues();
        routeMasterCV[2]  = new ContentValues();
        routeMasterCV[3]  = new ContentValues();
        routeMasterCV[4]  = new ContentValues();
        routeMasterCV[5]  = new ContentValues();

        routeMasterCV[0].put(TrackerContract.RoutesMaster.COLUMN_ROUTE_NUM, "1");
        routeMasterCV[1].put(TrackerContract.RoutesMaster.COLUMN_ROUTE_NUM, "1");
        routeMasterCV[2].put(TrackerContract.RoutesMaster.COLUMN_ROUTE_NUM, "2");
        routeMasterCV[3].put(TrackerContract.RoutesMaster.COLUMN_ROUTE_NUM, "2");
        routeMasterCV[4].put(TrackerContract.RoutesMaster.COLUMN_ROUTE_NUM, "4");
        routeMasterCV[5].put(TrackerContract.RoutesMaster.COLUMN_ROUTE_NUM, "30");

        routeMasterCV[0].put(TrackerContract.RoutesMaster.COLUMN_ROUTE_DESC, "Parx Casino to 54th-City");
        routeMasterCV[1].put(TrackerContract.RoutesMaster.COLUMN_ROUTE_DESC, "Parx Casino to 54th-City");
        routeMasterCV[2].put(TrackerContract.RoutesMaster.COLUMN_ROUTE_DESC, "20th-Johnston to Pulaski-Hunting Park");
        routeMasterCV[3].put(TrackerContract.RoutesMaster.COLUMN_ROUTE_DESC, "20th-Johnston to Pulaski-Hunting Park");
        routeMasterCV[4].put(TrackerContract.RoutesMaster.COLUMN_ROUTE_DESC, "Fern Rock Transportation Center to Broad-Pattison");
        routeMasterCV[5].put(TrackerContract.RoutesMaster.COLUMN_ROUTE_DESC, "Amtrak 30th Street Station to 69th Street Transportation Center");

        routeMasterCV[0].put(TrackerContract.RoutesMaster.COLUMN_DIRECTION, "To Parx Casino");
        routeMasterCV[1].put(TrackerContract.RoutesMaster.COLUMN_DIRECTION, "To 54th-City");
        routeMasterCV[2].put(TrackerContract.RoutesMaster.COLUMN_DIRECTION, "To 20th-Johnston");
        routeMasterCV[3].put(TrackerContract.RoutesMaster.COLUMN_DIRECTION, "To Pulaski-Hunting Park");
        routeMasterCV[4].put(TrackerContract.RoutesMaster.COLUMN_DIRECTION, "To Broad-Pattison");
        routeMasterCV[5].put(TrackerContract.RoutesMaster.COLUMN_DIRECTION, "To Amtrak 30th Street Station");

        for (ContentValues routeCV:routeMasterCV){
            Uri insertUri = getActivity().getContentResolver().insert(routesUri, routeCV);
            Log.d(LOG_TAG, "Inserted with Uri = "+insertUri);
        }

        // Populate the station master from a CSV file

        // delete all rows from route master table
        int delStationRows = getActivity().getContentResolver().delete(stationsUri, null, null);
        Log.d(LOG_TAG, "Deleted rows = "+delStationRows);

        // Create ContentValues to hold rows for insert into Station Master.
        ArrayList<ContentValues> stationMasterCV = new ArrayList<ContentValues>();

        // First read csv file from Assets folder, the parse the columns and insert into ContentValues array

        AssetManager manager = getActivity().getAssets();
        InputStream inStream = null;
        try{
            inStream = manager.open("station_id_name.csv");
            BufferedReader buffer = new BufferedReader(new InputStreamReader(inStream));
            String line = "";
            while ((line = buffer.readLine())!= null){
                String[] columns = line.split(",");
                ContentValues cv = new ContentValues();
                cv.put(TrackerContract.StationsMaster.COLUMN_STATION_ID, columns[0]);
                cv.put(TrackerContract.StationsMaster.COLUMN_STATION_NAME, columns[1]);
                // put cv into Content Value array defined earlier
                stationMasterCV.add(cv);
            }
        } catch (IOException e){
            e.printStackTrace();
            Log.d(LOG_TAG, "Problem reading CSV file with station info");
        }

        //Now insert content values into stations table using content provider framework
        Log.d(LOG_TAG, "Inserting into station master ");
        for (ContentValues stationCV:stationMasterCV){
            Uri insertUri = getActivity().getContentResolver().insert(stationsUri, stationCV);
            Log.d(LOG_TAG, "Inserted with Uri = "+insertUri);
        }



    }


    private class DownloadBusLocation extends AsyncTask<Void, Void, ArrayList<String>>{

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        private final String LOG_TAG = DownloadBusLocation.class.getSimpleName();

        @Override
        protected void onPreExecute(){
            progressDialog.show();

        }


        @Override
        protected ArrayList<String> doInBackground(Void... urls){
            ArrayList<String> busLocArrayList = fetchBusLocations();
            if(busLocArrayList == null){
                busLocArrayList = new ArrayList<String>();
                busLocArrayList.add("Not able to provide you data now");
                busLocArrayList.add("Come back later and try your luck");
            }
/*
            else{
                Uri locationUri = TrackerContract.LocationEntry.CONTENT_URI;
                AsyncQueryHandler handler =
                        new AsyncQueryHandler(getActivity().getContentResolver()) {};

                //insert rows in table location for each location returned by API call
                for (ContentValues bus:mContentValuesVector){
                    handler.startInsert(0, null, locationUri, bus);
                }
            }
*/
            fetchRouteMap();
            //busLocArrayList.addAll(fetchRouteMap());  //ToDo - discard this line and go back to getting RouteDataSet

            return busLocArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> busLocArrayList){
            Log.d(LOG_TAG, "Reached onPostExecute");
            progressDialog.dismiss();
            Toast.makeText(getActivity(), "onPostExecute", Toast.LENGTH_LONG);
            if (busLocArrayList == null)
                return;
            for (String loc:busLocArrayList){
                Log.d(LOG_TAG, "> "+loc);
            }

            mBusLocAdapter.clear();
            mBusLocAdapter.addAll(busLocArrayList);
            mBusLocAdapter.notifyDataSetChanged();

            //insert rows in table location for each location returned by API call
            Log.d(LOG_TAG, "About to call Content resolver with Insert");
            Uri locationUri = TrackerContract.LocationEntry.CONTENT_URI;
            for (ContentValues bus:mContentValuesVector){
                getActivity().getContentResolver().insert(locationUri, bus);
            }
        }

        private ArrayList<String> fetchBusLocations(){
            ArrayList<String> busLocArrayList = new ArrayList<String>();
            ArrayList<String> errorArrayList = new ArrayList<String>();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuffer buffer = null;

            URL url = null;
            String baseRouteUri = "http://www3.septa.org/transitview/bus_route_data";
            String baseKMLUri = "http://www3.septa.org/transitview/kml";
            String routeNum = mBusRoute;
            Log.d(LOG_TAG, "mBusRoute = "+mBusRoute);

            String locationJsonStr = null;

            try{

                Uri uri = Uri.parse(baseRouteUri).buildUpon().appendPath(routeNum).build();
                url = new URL(uri.toString());
                Log.d(LOG_TAG, "Fetching URL "+ url);
                /* debug  url = new URL("http://www3.septa.org/transitview/bus_route_data/23");*/
                Uri uriKML = Uri.parse(baseKMLUri).buildUpon().appendPath(routeNum).build();


            }catch (MalformedURLException e){
                e.printStackTrace();
                errorArrayList.add("MalformedURLException");
                return errorArrayList;
            }
            try{
                // Create the request to site, and open the connection
                urlConnection =  (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null){
                    //Nothing to do
                    Log.d(LOG_TAG, "Got a null inputStream, we are done here");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                // you could condense the above block to online like this:
                //reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                buffer = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null){
                    // Adding a newline  won't affect JSON parsing
                    // But if you print out the buffer for debugging, it makes it easier to read.
                    buffer.append(line + "\n");
                }


                if (buffer.length() == 0){
                    Log.d(LOG_TAG, "Got an empty buffer, we are done here");
                    return null;
                }

            }catch (IOException e){
                e.printStackTrace();
                errorArrayList.add("IOException");
                return errorArrayList;
            }

            locationJsonStr = buffer.toString();
//            errorArrayList.add(locationJsonStr);
//            return errorArrayList;

            //Now we have the JSON data, parse it to get the sub-components

            try{
                JSONObject locationJson = new JSONObject(locationJsonStr);
                errorArrayList.add("fetchBusLocation: converted string to JsonObj. Now extract buses Array");
                JSONArray busesArray = locationJson.getJSONArray("bus");
                errorArrayList.add("fetchBusLocation: Extracted buses Array = "+busesArray.toString());
                mContentValuesVector = new Vector<ContentValues>();

                ContentValues busValues = new ContentValues();

                for (int i = 0; i < busesArray.length(); ++i){
                    JSONObject busObject = busesArray.getJSONObject(i);
                    errorArrayList.add("fetchBusLocation: Got individual bus "+i+" = "+busObject.toString());

                    //Separating out the individual elements and storing them in a ContentValue Vector
                    busValues.put(TrackerContract.LocationEntry.COLUMN_LATITUDE,
                            busObject.getString("lat"));
                    busValues.put(TrackerContract.LocationEntry.COLUMN_LONGITUDE,
                            busObject.getString("lng"));
                    busValues.put(TrackerContract.LocationEntry.COLUMN_VEHICLE_ID,
                            busObject.getString("VehicleID"));
                    busValues.put(TrackerContract.LocationEntry.COLUMN_DESTINATION,
                            busObject.getString("Direction"));
                    busValues.put(TrackerContract.LocationEntry.COLUMN_DESTINATION,
                            busObject.getString("destination"));
                    busValues.put(TrackerContract.LocationEntry.COLUMN_ROUTE_NUM, mBusRoute); //this needs to be parameterized

                    mContentValuesVector.add(busValues);
                    busValues = new ContentValues();


                    //Creating a composite string for display purposes and adding it to an ArrayList
                    //that will be used to update the ArrayAdapter linked to the listView
                    String oneLine = "[" + busObject.getString("lat") + "/"
                            + busObject.getString("lng") + "]"
                            +" to "+busObject.getString("destination")
                            +", " + busObject.getString("Direction")
                            +" (" + busObject.getString("VehicleID") + ")";
                    busLocArrayList.add(oneLine);
                }

            }catch(JSONException e){
                e.printStackTrace();
                errorArrayList.add("JSONException");
                return errorArrayList;
            }
            return busLocArrayList;


        } //End method fetchBusLocations

        /**************
         * Fetch the route information in KML format
         * parse using SAX (Simple API for XML) Parser
         *
         * ************/

        private  void fetchRouteMap() {
            mRouteDataSet = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuffer buffer = null;
            ArrayList<String> errorArrayList = new ArrayList<String>();

            Log.d(LOG_TAG, "Func fetchRouteMap");

            URL url = null;
            String baseKMLUri = "http://www3.septa.org/transitview/kml";
            String routeNum = mBusRoute;

            try {
                Uri uri = Uri.parse(baseKMLUri).buildUpon().appendPath(routeNum).build();
                url = new URL(uri.toString());
                Log.d(LOG_TAG, "Fetching URL " + url);
            } catch (MalformedURLException e) {
                Log.d(LOG_TAG, "Error in URL Fetch: "+e);
                e.printStackTrace();
            }

            try{
                Log.d(LOG_TAG,"Starting the whole SAXFactory stuff");
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                XMLReader xmlReader = parser.getXMLReader();
                TransitSAXHandler saxHandler = new TransitSAXHandler();
                xmlReader.setContentHandler(saxHandler);
                xmlReader.parse(new InputSource(url.openStream()));
                mRouteDataSet = saxHandler.getParsedData();

                Log.d(LOG_TAG,"Finished parsing KML file. Result = " +'\n'+mRouteDataSet.toString());
                errorArrayList.add("Finished parsing KML file. Result = " +'\n'+mRouteDataSet.toString());

            } catch (Exception e){
                Log.d(LOG_TAG, "SAX related exception: " + e);
            }

//            return errorArrayList;
        } //End method fetchRouteMap


        } //End private class DownloadBusLocation
}
