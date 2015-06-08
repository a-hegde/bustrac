package com.ibangalore.bustrac.kml;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by ahegde on 6/1/15.
 */
public class TransitSAXHandler extends DefaultHandler {
    private boolean in_kmltag = false;
    private boolean in_placemarktag = false;
    private boolean in_multigtag = false;
    private boolean in_pointtag = false;
    private boolean in_linestrtag = false;
    private boolean in_tesstag = false;
    private boolean in_coordtag = false;

    private StringBuffer mBuffer;
    private RouteDataSet mRouteDataSet = new RouteDataSet();
    private static final String LOG_TAG = TransitSAXHandler.class.getSimpleName();

    public RouteDataSet getParsedData(){
        Log.d(LOG_TAG,"func getParsedData");
        return this.mRouteDataSet;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void startElement(String nameSpaceURI, String localName,
                             String qName, Attributes attributes) throws SAXException {
        switch (localName){
            case "kml": in_kmltag = true;
                break;
            case "Placemark": in_placemarktag = true;
                break;
            case "MultiGeometry": in_multigtag = true;
                break;
            case "Point": in_pointtag = true;
                break;
            case "LineString": in_linestrtag = true;
                break;
            case "tessellate": in_tesstag = true;
                break;
            case "coordinates": in_coordtag = true;
                mBuffer = new StringBuffer();
                break;
            default:
                break;
        }
        Log.d(LOG_TAG,"Start Element Local Name = "+ localName);

    }

    /** Gets be called on closing tags like:
     * </tag> */
    @Override
    public void endElement(String nameSpaceURI, String localName, String qName) throws SAXException {
        switch (localName){
            case "kml": in_kmltag = false;
                break;
            case "Placemark": in_placemarktag = false;
                break;
            case "MultiGeometry": in_multigtag = false;
                break;
            case "Point": in_pointtag = false;
                break;
            case "LineString": in_linestrtag = false;
                break;
            case "tessellate": in_tesstag = false;
                break;
            case "coordinates": in_coordtag = false;
                if (in_pointtag){
                    Placemark placePoint = new Placemark(Placemark.POINT);
                    placePoint.setCoordinates(mBuffer.toString());
                    Log.d(LOG_TAG,"In Point Tag, string is "+ mBuffer);
                    mRouteDataSet.addPlacemark(placePoint);
                    Log.d(LOG_TAG,"Added point");
                }
                else if (in_linestrtag){
                    Placemark placeLine = new Placemark(Placemark.LINESTRING);
                    placeLine.setCoordinates(mBuffer.toString());
                    Log.d(LOG_TAG,"In Point Tag, string is "+ mBuffer);
                    mRouteDataSet.addPlacemark(placeLine);
                    Log.d(LOG_TAG,"Added line");
                }
                break;
            default:
                break;
        }
        Log.d(LOG_TAG,"End Element Local Name = "+ localName);
    }

    /** Gets be called on the following structure:
     * <tag>characters</tag> */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (in_coordtag){
            mBuffer.append(ch, start, length);
        }
    }// end function characters

}


