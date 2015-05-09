package com.ibangalore.bustrac;

import android.content.ContentValues;
import android.os.Bundle;
import android.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Vector;

public class MapsActivity extends ActionBarActivity
            implements LocationFetchFragment.OnBusItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap=null;
    private boolean mShowMap = true;

    private final String LOG_TAG = MapsActivity.class.getSimpleName();
    private String mRoute = "";
    private Double mLatitude = 0.0;
    private Double mLongitude = 0.0;
    Vector<ContentValues> mBusPositionsCVV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate: about to set Content View");
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "onCreate: Content View has been set");

        if (findViewById(R.id.container) != null){
            if (savedInstanceState == null){
                LocationFetchFragment locationFetchFragment = new LocationFetchFragment();
                Log.d(LOG_TAG, "onCreate: invoking FragMgr");
                getFragmentManager().beginTransaction()
                        .add(R.id.container, locationFetchFragment)
                        .commit();
            }
            Log.d(LOG_TAG, "onCreate: done with invoking FragMgr");
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onBusItemSelected(Vector<ContentValues> busPositionsCVV){

        Log.d(LOG_TAG, "starting func onBusItemSelected");

        MapFragment mapFragment = MapFragment.newInstance();

        mapFragment.getMapAsync(this);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        //Replace whatever is in this container with the new (maps) fragment.
        transaction.replace(R.id.container, mapFragment);
        transaction.addToBackStack(null);
        transaction.commit();

            //Assign values to member variable
        mBusPositionsCVV = busPositionsCVV;
        //mRoute = route; mLatitude = latitude; mLongitude = longitude;

    }

    @Override
    public void onMapReady(GoogleMap map){
        double avgLat = 0, avgLng = 0;
        int busCount = 0;
        for (ContentValues busPosition:mBusPositionsCVV){
            double lat = busPosition.getAsDouble("lat");
            double lng = busPosition.getAsDouble("lng");
            String marker = busPosition.getAsString("routeNum");
            LatLng point = new LatLng(lat, lng);

            //calculations to figure out a centre point for map zoom
            avgLat+=lat; avgLng+=lng; busCount++;

            map.addMarker(new MarkerOptions()
                    .position(point)
                    .title(marker));
        }

        if(busCount >0) {
        //Find the mean point across all the points by diving the sum of latitudes
        // (and longitudes) by total number of points
            avgLat /= busCount;
            avgLng /= busCount;
        }

        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(avgLat, avgLng), 11));
    }


}
