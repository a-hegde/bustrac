package com.ibangalore.bustrac.UXAssets;

/**
 * Class corresponding to row item in Bus Arrivals ListView
 * Created by ahegde on 6/24/15.
 */
public class ArrivalsRowItem {

    private String routeNum;
    private String destination;
    private String direction;
    private String etaMins;

    public ArrivalsRowItem(String routeNum, String destination,
                           String direction, String etaMins){
        this.routeNum = routeNum;
        this.destination = destination;
        this.direction = direction;
        this.etaMins = etaMins;
    }

    public String getRouteNum(){
        return this.routeNum;
    }

    public String getDestination(){
        return this.destination + " via "
                + this.direction;
    }

    public String getEtaMin(){
        return this.etaMins  +" mins";
    }

    public String toString(){
        return routeNum +" | "+destination+" via "+direction+" | "+etaMins +" mins";
    }
}
