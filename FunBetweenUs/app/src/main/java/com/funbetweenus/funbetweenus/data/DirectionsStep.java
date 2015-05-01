package com.funbetweenus.funbetweenus.data;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Grant Peltier on 4/7/15.
 */
public class DirectionsStep {

    private LatLng startLocation;
    private LatLng endLocation;
    private int distance;

    public DirectionsStep(LatLng start, LatLng stop, int dist){
        startLocation = start;
        endLocation = stop;
        distance = dist;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public String toString(){
        return "{Start: "+ startLocation.latitude +", "+ startLocation.longitude+ " End: "+endLocation.latitude +", "+endLocation.longitude +" Distance: "+distance+"}";
    }


    public boolean equals(DirectionsStep other){
        if(this.startLocation == other.getStartLocation() && this.endLocation == other.getEndLocation() && this.distance == other.getDistance()){
            return true;
        }else{return false;}
    }
}
