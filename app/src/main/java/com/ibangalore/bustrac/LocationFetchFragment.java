package com.ibangalore.bustrac;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Vector;

/**
 * Class to fetch the current bus location by fetching from APIs exposed by Transit Agency
 */
public class LocationFetchFragment extends Fragment{

    OnBusItemSelectedListener mCallback;
    public final String LOG_TAG = LocationFetchFragment.class.getSimpleName();
    ArrayAdapter<String> mBusLocAdapter;
    Vector<ContentValues> mContentValuesVector;

    /* The calling activity must implement this interface */
    public interface OnBusItemSelectedListener {
        //Called by LocationFetchFragment when an item from the list view is selected
        public void onBusItemSelected(Vector<ContentValues> busPositionsCVV);
    }


    //Constructor
    public LocationFetchFragment() {
        Log.d(LOG_TAG, "empty constructor");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        ArrayList<String> busLocArrayList = new ArrayList<>();
        busLocArrayList.add("[39.96/-75.15] to Chestnut Hill, NorthBound (5617)");
        busLocArrayList.add("[39.91/-75.16] to Broad - Oregon, SouthBound (8235)");

        Log.d(LOG_TAG, "onCreateView: initialize Array Adapter");

        mBusLocAdapter = new ArrayAdapter<String>(
                getActivity(),                        // The current context (this activity)
                R.layout.bus_list_item,       // The name of the layout ID
//                R.id.bus_list_item_textview,        // The ID of the textview to populate.
                busLocArrayList);


        View rootView = inflater.inflate(R.layout.bus_location_fragment, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_bus_coords);
        listView.setAdapter(mBusLocAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                double lat = mContentValuesVector.get(position).getAsDouble("lat");
                double lng = mContentValuesVector.get(position).getAsDouble("lng");
                Log.d(LOG_TAG, "Lat = "+lat+" and Long = "+lng+ "at position "+position);
                try {
                    Log.d(LOG_TAG, "Content Values at 3 = " + mContentValuesVector.get(3).toString());
                    Log.d(LOG_TAG, "Content Values at 4 = " + mContentValuesVector.get(4).toString());
                }catch (ArrayIndexOutOfBoundsException e){
                    Log.d(LOG_TAG, "Looks like we have 3 or less buses showing up");
                }
                mCallback.onBusItemSelected(mContentValuesVector);

            }
        });


        Button fetchButton = (Button) rootView.findViewById(R.id.fetch_button);
        fetchButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLocations(v);
            }
        });

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

    private class DownloadBusLocation extends AsyncTask<Void, Void, ArrayList<String>>{
        ProgressDialog progressDialog = new ProgressDialog(getActivity());

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

            return busLocArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> busLocArrayList){
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

        }

        private ArrayList<String> fetchBusLocations(){
            ArrayList<String> busLocArrayList = new ArrayList<String>();
            ArrayList<String> errorArrayList = new ArrayList<String>();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuffer buffer = null;

            URL url = null;
            String baseRouteUri = "http://www3.septa.org/transitview/bus_route_data";
            String routeNum = "23";

            String locationJsonStr = null;

            try{

                Uri uri = Uri.parse(baseRouteUri).buildUpon().appendPath(routeNum).build();
                url = new URL(uri.toString());
                /* debug  url = new URL("http://www3.septa.org/transitview/bus_route_data/23");*/
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
                    busValues.put("lat", busObject.getString("lat"));
                    busValues.put("lng", busObject.getString("lng"));
                    busValues.put("vehicleID", busObject.getString("VehicleID"));
                    busValues.put("Direction", busObject.getString("Direction"));
                    busValues.put("destination", busObject.getString("destination"));
                    busValues.put("routeNum", "23"); //this needs to be parameterized

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

    } //End private class DownloadBusLocation
}
