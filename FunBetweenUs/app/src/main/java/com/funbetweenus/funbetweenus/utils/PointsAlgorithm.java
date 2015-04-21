package com.funbetweenus.funbetweenus.utils;

import com.funbetweenus.funbetweenus.data.DirectionsRoute;
import com.funbetweenus.funbetweenus.data.DirectionsStep;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Ishaq on 4/14/2015.
 */

public class PointsAlgorithm {

    private ArrayList<LatLng> points;
    private int counter= 0;
    private int pointsSkipper;


    public PointsAlgorithm(){
    }

    public ArrayList<LatLng> getPoints(ArrayList<DirectionsStep> steps){

        pointsSkipper = steps.size()/5;

        for(int i = 0; i<steps.size(); i++){
            counter++;
            if(counter == pointsSkipper){
                points.add(steps.get(i).getEndLocation());
                counter = 0;
            }
        }
        return points;
    }

    public LatLng getPoint(int l){
       return points.get(l);
    }


}
