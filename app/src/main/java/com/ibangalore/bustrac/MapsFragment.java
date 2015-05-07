package com.ibangalore.bustrac;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by ahegde on 5/5/15.
 */
public class MapsFragment extends Fragment {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
//    private OnMapReadyCallback mMapReadyCallback;

    private final String LOG_TAG = MapsFragment.class.getSimpleName();
    public static final String ROUTE_NUMBER = "ROUTE_NUMBER";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String LATITUDE = "LATITUDE";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        Log.d(LOG_TAG, " about to inflate");
        View mapView = inflater.inflate(R.layout.fragment_maps,container, false);
        Log.d(LOG_TAG, " finished inflating");
//        setUpMapIfNeeded();
        return mapView;
    }



    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     *
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
//            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
//                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
//                setUpMap();
            }
        }
    }
*/

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     *
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
*/


//    @Override
//    public void onMapReady(GoogleMap map){
//        this.mMap = map;
//
//    }
}
