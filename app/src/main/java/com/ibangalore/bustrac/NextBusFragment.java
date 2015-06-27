package com.ibangalore.bustrac;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ibangalore.bustrac.UXAssets.ArrivalsLViewAdapter;
import com.ibangalore.bustrac.UXAssets.ArrivalsRowItem;

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
import java.util.Iterator;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Created by ahegde on 6/13/15.
 */
public class NextBusFragment extends Fragment {
    private static final String LOG_TAG=NextBusFragment.class.getSimpleName();
    Vector<ContentValues> mContentValuesVector;
    ArrivalsLViewAdapter mArrivalsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(LOG_TAG, "func onCreateView starts");
        ArrayList<ArrivalsRowItem> arrayArrivals = new ArrayList<ArrivalsRowItem>();
        mArrivalsAdapter = new ArrivalsLViewAdapter(getActivity(), arrayArrivals);

        View rootView = inflater.inflate(R.layout.next_bus, null, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_bus_arrivals);
        listView.setAdapter(mArrivalsAdapter);

        new DownloadArrivals().execute();
        return rootView;

//        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private class DownloadArrivals extends AsyncTask<Void, Void, ArrayList<ArrivalsRowItem>> {

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        private final String LOG_TAG = DownloadArrivals.class.getSimpleName();

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

/*debug*/   String stationCode = "90401";  //mStationCode;
//            Log.d(LOG_TAG, "Station Code = " + mStationCode);

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

            if (arrivalsArrayList == null)
                return;
            for (ArrivalsRowItem bus : arrivalsArrayList) {
                Log.d(LOG_TAG, "> " + bus);
            }

            mArrivalsAdapter.clear();
            mArrivalsAdapter.addAll(arrivalsArrayList);
            mArrivalsAdapter.notifyDataSetChanged();

        }

    }

}
