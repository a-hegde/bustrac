package com.ibangalore.bustrac.kml;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ahegde on 6/1/15.
 */
public class Placemark{
    public static final String POINT = "Point";
    public static final String LINESTRING = "LineString";
    private static final String LOG_TAG = Placemark.class.getSimpleName();

    String title;
    String description;
    String type; //Can be Point or LineString
    String coordinates;
    String address;
    LatLng startPoint;
    LatLng endPoint;  //relevant for a line, e point doesn't have one

    public Placemark(String typeParam){
        if(typeParam==POINT || typeParam==LINESTRING)
            type = typeParam;
        else type = "Unknown";
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getCoordinates() {
        return coordinates;
    }
    public LatLng getStartPoint() { return startPoint;}

    public LatLng getEndPoint() {
        if(type == LINESTRING)
            return endPoint;
        else // Points don't have an endpoint
            return null;
    }

    public void setCoordinates(String coordinates) {
        // Co-ords for line looks like '<coordinates>-75.16656999999999,39.953973,0 -75.166612,39.953799,0</coordinates>'
        // So first split by space (" ") in case there are multiple points such as a line
        String [] points = coordinates.split(" ");
        //Now separate out lat & long by comma separation
        String [] point = points[0].split(",");
        //Coordinates in KML are in Long, Lat order, so reverse when creating start and end point
        startPoint = new LatLng(Double.parseDouble(point[1]), Double.parseDouble(point[0]));
        Log.d(LOG_TAG, "Start Point is "+startPoint.toString());

        //Now pick out the second point if entity is line
        if(type == LINESTRING && points.length >1){
            point = points[1].split(",");
            endPoint = new LatLng(Double.parseDouble(point[1]), Double.parseDouble(point[0]));
            Log.d(LOG_TAG, "End Point is "+endPoint.toString());
        }
        this.coordinates = coordinates;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
} //End class placemark