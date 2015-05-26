package com.ibangalore.bustrac;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by ahegde on 5/10/15.
 */
public class HeaderFragment extends Fragment implements Spinner.OnItemSelectedListener {

    private final String LOG_TAG = HeaderFragment.class.getSimpleName();
    OnSpinnerChangeListener mCallback;

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

        // Create an array adapter for the spinner values. In future, we will get this list from a Database
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.route_nums_array,
                android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Apply Adapter to spinner
        spinner.setAdapter(adapter);

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

}
