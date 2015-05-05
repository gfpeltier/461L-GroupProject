package com.funbetweenus.funbetweenus.utils;

import android.util.Log;

import com.funbetweenus.funbetweenus.data.DirectionsLeg;
import com.funbetweenus.funbetweenus.data.DirectionsRoute;
import com.funbetweenus.funbetweenus.data.DirectionsStep;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Ishaq on 4/07/2015.
 */

public class PointsAlgorithm {

    private ArrayList<LatLng> points;
    private int counter= 0;
    private int pointsSkipper;


    public PointsAlgorithm(){
    }

    public ArrayList<LatLng> getPoints(DirectionsRoute routeToDestination){

        ArrayList<DirectionsLeg> legsArray = new ArrayList<DirectionsLeg>();
        ArrayList<DirectionsStep> stepsArray = new ArrayList<DirectionsStep>();
        ArrayList<LatLng> points = new ArrayList<LatLng>();
        ArrayList<Integer> whichOnes = new ArrayList<Integer>();
        whichOnes.add(0);
        whichOnes.add(0);
        whichOnes.add(0);
        whichOnes.add(0);
        whichOnes.add(0);
        float completeDistance = routeToDestination.getDistance();
        float firstDistance = completeDistance/6;
        float secondDistance = firstDistance + firstDistance;
        float thirdDistance = secondDistance + firstDistance;
        float fourthDistance = thirdDistance + firstDistance;
        float fifthDistance = fourthDistance + firstDistance;
        float tempDistance = 0;

        for(int k = 0; k<routeToDestination.getLegs().size(); k++){
            legsArray.addAll(routeToDestination.getLegs());
        }

        for(int i = 0; i<legsArray.size(); i++){
             stepsArray.addAll(legsArray.get(i).getSteps());
        }


        float currentMin1 = Math.abs(stepsArray.get(0).getDistance()-firstDistance);
        float currentMin2 = Math.abs(stepsArray.get(1).getDistance()-secondDistance);
        float currentMin3 = Math.abs(stepsArray.get(2).getDistance()-thirdDistance);
        float currentMin4 = Math.abs(stepsArray.get(3).getDistance()-fourthDistance);
        float currentMin5 = Math.abs(stepsArray.get(4).getDistance()-fifthDistance);

        points.add(0,stepsArray.get(0).getEndLocation());
        points.add(1,stepsArray.get(1).getEndLocation());
        points.add(2,stepsArray.get(2).getEndLocation());
        points.add(3,stepsArray.get(3).getEndLocation());
        points.add(4,stepsArray.get(4).getEndLocation());

        for(int o = 0; o<stepsArray.size(); o++){
            tempDistance += stepsArray.get(o).getDistance();
            if(Math.abs((firstDistance-tempDistance))<=currentMin1){
                if(!points.contains(stepsArray.get(o).getEndLocation())){
                whichOnes.set(0,o);
                points.set(0,stepsArray.get(o).getEndLocation());
                currentMin1 = Math.abs(firstDistance - tempDistance);}
            }
            if((Math.abs(secondDistance-tempDistance)<=currentMin2)){
                if(!points.contains(stepsArray.get(o).getEndLocation())){
                whichOnes.set(1,o);
                points.set(1,stepsArray.get(o).getEndLocation());
                currentMin2 = Math.abs(secondDistance - tempDistance);}
            }
            if((Math.abs((thirdDistance-tempDistance))<=currentMin3)){
                if(!points.contains(stepsArray.get(o).getEndLocation())){
                whichOnes.set(2,o);
                points.set(2,stepsArray.get(o).getEndLocation());
                currentMin3 = Math.abs(thirdDistance - tempDistance);}
            }
            if((Math.abs((fourthDistance-tempDistance))<=currentMin4)){
                if(!points.contains(stepsArray.get(o).getEndLocation())){
                whichOnes.set(3,o);
                points.set(3,stepsArray.get(o).getEndLocation());
                currentMin4 = Math.abs(fourthDistance - tempDistance);}
            }
            if(Math.abs((fifthDistance-tempDistance))<=currentMin5){
                if(!points.contains(stepsArray.get(o).getEndLocation())){
                whichOnes.set(4,o);
                points.set(4,stepsArray.get(o).getEndLocation());
                currentMin5 = Math.abs(fifthDistance - tempDistance);}
            }
        }

        return points;
    }
}
