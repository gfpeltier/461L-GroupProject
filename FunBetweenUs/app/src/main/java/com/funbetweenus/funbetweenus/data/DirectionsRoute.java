package com.funbetweenus.funbetweenus.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Grant Peltier on 4/7/15.
 */
public class DirectionsRoute {

    private int distance;
    private ArrayList<DirectionsLeg> legs;



    private ArrayList<LatLng> bounds;

    public DirectionsRoute(ArrayList<DirectionsLeg> mLegs, ArrayList<LatLng> mBounds){
        legs = mLegs;
        bounds = mBounds;
        distance = calculateDistance();
    }

    public DirectionsRoute() {
        distance = 0;
        legs = new ArrayList<DirectionsLeg>();
    }

    private int calculateDistance(){
        int mDistance = 0;
        Iterator<DirectionsLeg> i = legs.iterator();
        while (i.hasNext()){
            DirectionsLeg step = i.next();
            mDistance += step.getDistance();
        }
        return mDistance;
    }

    public void addLeg(DirectionsLeg leg){
        legs.add(leg);
        distance = calculateDistance();
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public ArrayList<DirectionsLeg> getLegs() {
        return legs;
    }

    public void setLegs(ArrayList<DirectionsLeg> legs) {
        this.legs = legs;
    }

    public ArrayList<LatLng> getBounds() {
        return bounds;
    }

    public void setBounds(ArrayList<LatLng> bounds) {
        this.bounds = bounds;
    }
}
