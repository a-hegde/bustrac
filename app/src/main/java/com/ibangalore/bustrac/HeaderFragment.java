package com.ibangalore.bustrac;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.ibangalore.bustrac.data.TrackerContract;

/**
 * Created by ahegde on 5/10/15.
 */
public class HeaderFragment extends Fragment implements Spinner.OnItemSelectedListener {

    private final String LOG_TAG = HeaderFragment.class.getSimpleName();
    OnSpinnerChangeListener mCallback;
    String[] mRouteNumsArray;
    String [] mRouteDirsArray;

    private static final String[] sRoutesProjection =
            {TrackerContract.RoutesMaster.TABLE_NAME + "."+
                    TrackerContract.RoutesMaster.COLUMN_ROUTE_NUM,
                    TrackerContract.RoutesMaster.TABLE_NAME + "."+
                    TrackerContract.RoutesMaster.COLUMN_DIRECTION};

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mCallback = (OnSpinnerChangeListener) activity;
        }catch(ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + " must implement OnSpinnerChangeListener");
        }
    }

    /* The calling activity must implement this interface */
    public interface OnSpinnerChangeListener {
        //Called by LocationFetchFragment when an item from the list view is selected
        public void onSpinnerChange(String busRoute);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView: initialize ");

        View rootView = inflater.inflate(R.layout.header_fragment, null, false);
        Spinner spinner = (Spinner) rootView.findViewById(R.id.route_num_spinner);
        if (spinner == null){
            Log.d(LOG_TAG, "Null Spinner causing a problem, quitting on create now");
            return rootView;
        }
        spinner.setOnItemSelectedListener(this);
        Log.d(LOG_TAG, "Created listener for spinner");

        //Get route list from route master table in database rather than hard coding it.
        Log.d(LOG_TAG, " Fetching routes list from routes_master table in db");
        Uri routesUri = TrackerContract.RoutesMaster.CONTENT_URI;
        Cursor c = getActivity().getContentResolver().query(routesUri, sRoutesProjection,
                null, null, TrackerContract.RoutesMaster.COLUMN_ROUTE_NUM +" ASC");

        String[] routesArray = new String[c.getCount()];
        mRouteNumsArray = new String[c.getCount()];
        mRouteDirsArray = new String[c.getCount()];
        int i = 0;

        while (c.moveToNext()){
            routesArray[i]= c.getString(c.getColumnIndex(TrackerContract.RoutesMaster.COLUMN_ROUTE_NUM)) +
                    " : "+ c.getString(c.getColumnIndex(TrackerContract.RoutesMaster.COLUMN_DIRECTION));
            Log.d(LOG_TAG, "routesArray[] = "+ routesArray[i]);
            mRouteNumsArray[i] = c.getString(c.getColumnIndex(TrackerContract.RoutesMaster.COLUMN_ROUTE_NUM));
            mRouteDirsArray[i] = c.getString(c.getColumnIndex(TrackerContract.RoutesMaster.COLUMN_DIRECTION));

            i+=1; //increment array counter
        }
        Log.d(LOG_TAG, "Finished populating routesArray, number of elements = "+ Integer.toString(i));

        // Create an array adapter for the spinner values. In future, we will get this list from a Database
/**** Old spinner adapter code with standard adapter. Now we are using a custom adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.route_nums_array,
                android.R.layout.simple_spinner_item);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                routesArray);


        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Apply Adapter to spinner
        spinner.setAdapter(adapter2);
*/

        spinner.setAdapter(new customSpinnerAdapter(getActivity(), R.layout.spinner_item_2col, mRouteNumsArray ));

        return rootView;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id){
        String busRoute = parent.getItemAtPosition(pos).toString();
        Log.d(LOG_TAG, "onItemSelected for spinner = "+ busRoute);
        mCallback.onSpinnerChange(busRoute);

    }

    public void onNothingSelected(AdapterView<?> parent){

    }

    // Create a custom Adapter Class to support a two column spinner.
    public class customSpinnerAdapter extends ArrayAdapter{
        public customSpinnerAdapter(Context context, int textViewResourceId, String[] objects){
            super(context, textViewResourceId, objects);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent){

            // Inflating the layout for the custom Spinner
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View layout = inflater.inflate(R.layout.spinner_item_2col, parent, false);

            // Declaring and Typecasting the textview in the inflated layout
            TextView routeNumTV = (TextView) layout.findViewById(R.id.route_num);
            TextView routeDirTV = (TextView) layout.findViewById(R.id.route_dir);

            // Setting the text using the array
            routeNumTV.setText(mRouteNumsArray[position]);
            routeDirTV.setText(mRouteDirsArray[position]);

            routeNumTV.setTextSize(20f);

            return layout;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

    } //End nested class customSpinnerAdapter

}//End class HeaderFragment
