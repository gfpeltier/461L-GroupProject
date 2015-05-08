package com.funbetweenus.funbetweenus.utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Grant Peltier on 5/5/15.
 */
public class FineQueryPointsFinder {

    private final static int NUMQUERYPOINTS = 5;

    private int mDistance;          //Distance of full route in meters
    private List<LatLng> path;      //Full list of path points

    public FineQueryPointsFinder(int dist, List<LatLng> points){
        mDistance = dist;
        path = points;
    }


    /**
     *
     * @return List of points to actually query Google places from
     */
    public ArrayList<LatLng> getQueryPoints(){
        ArrayList<LatLng> queryPoints = new ArrayList<LatLng>();
        if(mDistance > 1609){
            double currentDistance;
            double pointInterval = mDistance / (NUMQUERYPOINTS + 1);
            int pointNumber = 1;
            while(pointNumber < 6){
                currentDistance = 0.0;
                double targetDistance = (pointInterval * pointNumber);
                for(int k = 0; k < path.size() - 2; k++) {
                    if (currentDistance != mDistance) {
                        if ((currentDistance + distance(path.get(k).latitude, path.get(k).longitude, path.get(k + 1).latitude, path.get(k + 1).longitude, 'm')) < targetDistance) {
                            currentDistance += distance(path.get(k).latitude, path.get(k).longitude, path.get(k + 1).latitude, path.get(k + 1).longitude, 'm');
                        } else {
                            queryPoints.add(evalQueryPoint(currentDistance, targetDistance, path.get(k), path.get(k + 1)));
                            currentDistance = mDistance;
                            pointNumber++;
                        }
                    }
                }
            }
        }else{
            double currentDistance = 0.0;
            double targetDistance = mDistance/2;
            for(int k = 0; k < path.size() - 2; k++) {
                if (currentDistance != mDistance) {
                    if ((currentDistance + distance(path.get(k).latitude, path.get(k).longitude, path.get(k + 1).latitude, path.get(k + 1).longitude, 'm')) < targetDistance) {
                        currentDistance += distance(path.get(k).latitude, path.get(k).longitude, path.get(k + 1).latitude, path.get(k + 1).longitude, 'm');
                    } else {
                        queryPoints.add(evalQueryPoint(currentDistance, targetDistance, path.get(k), path.get(k + 1)));
                        currentDistance = mDistance;
                    }
                }
            }
        }
        return queryPoints;
    }


    /**
     * Way to approximate correct latitude and longitude for the query point. Method works by linearly
     * transforming line segment AB such that A is at the origin. Then scale the coordinates of B
     * by the ratio of the (targetDistance - currentDistance)/(distance(AB)). Then reverse transform.
     *
     * @param currentDistance
     * @param targetDistance
     * @param pointA
     * @param pointB
     * @return
     */
    private LatLng evalQueryPoint(double currentDistance, double targetDistance, LatLng pointA, LatLng pointB){
        //TODO: Algorithmically determine point at target distance along line from A to B
        LatLng newB = new LatLng(pointB.latitude-pointA.latitude, pointB.longitude-pointA.longitude);
        double scalar = (targetDistance - currentDistance)/distance(pointA.latitude, pointA.longitude, pointB.latitude, pointB.longitude, 'm');
        return new LatLng((newB.latitude * scalar) + pointA.latitude, (newB.longitude * scalar) + pointA.longitude);
    }


    /**
     * The following 3 methods are adapted from http://www.geodatasource.com/developers/java
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @param unit
     * @return distance in terms of specified unit (K = kilometers, N = nautical miles, M = miles, m = meters)
     */
    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }else if (unit == 'm'){         // Get meters
            dist = dist * 1609.344;
        }
        return (dist);
    }



    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }



    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }



}
