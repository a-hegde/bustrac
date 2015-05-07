package com.ibangalore.bustrac;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class MapsActivity extends ActionBarActivity
            implements LocationFetchFragment.OnBusItemSelectedListener /*, OnMapReadyCallback */ {


    private final String LOG_TAG = MapsActivity.class.getSimpleName();
    private String mRoute = "";
    private Double mLatitude = 0.0;
    private Double mLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate: about to set Content View");
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "onCreate: Content View has been set");

        if (findViewById(R.id.container) != null){
            if (savedInstanceState == null){
                LocationFetchFragment locationFetchFragment = new LocationFetchFragment();
                Log.d(LOG_TAG, "onCreate: invoking SupportFragMgr");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, locationFetchFragment)
                        .commit();
            }
            Log.d(LOG_TAG, "onCreate: done with invoking SupportFragMgr");
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onBusItemSelected(String route, double latitude, double longitude){
        MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Log.d(LOG_TAG, "starting func onBusItemSelected");
        if (mapsFragment == null){  // Single pane mode, we need to create a new Maps Fragment
            mapsFragment = new MapsFragment();
            //Pass the route number and lat-long as parameters in args bundle
            Bundle args = new Bundle();

            //Assign values to member variable
            mRoute = route; mLatitude = latitude; mLongitude = longitude;

            args.putString(mapsFragment.ROUTE_NUMBER, route);
            args.putDouble(mapsFragment.LATITUDE, latitude);
            args.putDouble(mapsFragment.LONGITUDE, longitude);
            mapsFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //Replace whatever is in this container with the new (maps) fragment.
            transaction.replace(R.id.container, mapsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else
            Log.d(LOG_TAG, "maps Fragment is not null, now what");

    }

//    @Override
//    public void onMapReady(GoogleMap map){
//        map.addMarker(new MarkerOptions()
//                .position(new LatLng(mLatitude, mLongitude))
//                .title(mRoute));
//    }


}
