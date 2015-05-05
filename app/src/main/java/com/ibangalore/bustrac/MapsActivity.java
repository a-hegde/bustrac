package com.ibangalore.bustrac;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class MapsActivity extends ActionBarActivity { //FragmentActivity {

//    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private final String LOG_TAG = MapsActivity.class.getSimpleName();

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


//        setContentView(R.layout.fragment_maps);
//        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        setUpMapIfNeeded();
    }


}
