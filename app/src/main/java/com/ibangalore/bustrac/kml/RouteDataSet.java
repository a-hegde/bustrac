package com.ibangalore.bustrac.kml;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by ahegde on 6/1/15.
 */
public class RouteDataSet {

    private ArrayList<Placemark> placemarks = new ArrayList<Placemark>();
    private Placemark currentPlacemark;
    private Placemark routePlacemark;

    public String toString(){
        String s="";
        for(Iterator<Placemark> iter=placemarks.iterator(); iter.hasNext();){
            Placemark p = (Placemark) iter.next();
            s+= p.getType()+ " | "+ p.getCoordinates() + '\n';
        }
        return s;
    }

    public void addCurrentPlacemark(){
        placemarks.add(currentPlacemark);

    }

    public ArrayList<Placemark> getPlacemarks(){
        return placemarks;
    }

    public void addPlacemark(Placemark placemark){
        placemarks.add(placemark);
    }

}
