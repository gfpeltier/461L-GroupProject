package com.funbetweenus.funbetweenus.data;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Grant Peltier on 4/7/15.
 */
public class DirectionsLeg {

    private int distance;
    private ArrayList<DirectionsStep> steps;

    public DirectionsLeg(ArrayList<DirectionsStep> mSteps){
        steps = mSteps;
        distance = calculateDistance();
    }

    public DirectionsLeg(){
        distance = 0;
        steps = new ArrayList<DirectionsStep>();
    }

    private int calculateDistance(){
        int mDistance = 0;
        Iterator<DirectionsStep> i = steps.iterator();
        while (i.hasNext()){
            DirectionsStep step = i.next();
            mDistance += step.getDistance();
        }
        return mDistance;
    }

    public void addStep(DirectionsStep step){
        steps.add(step);
        distance = calculateDistance();
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public ArrayList<DirectionsStep> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<DirectionsStep> steps) {
        this.steps = steps;
    }

    public boolean equals(DirectionsLeg other){
        if((this.steps.size() != other.getSteps().size()) || (this.distance != other.getDistance())){
            return false;
        }
        Iterator<DirectionsStep> i = this.steps.iterator();
        Iterator<DirectionsStep> j = other.getSteps().iterator();
        while (i.hasNext()){
            DirectionsStep iTmp = i.next();
            DirectionsStep jTmp = j.next();
            if(!iTmp.equals(jTmp)){
                return false;
            }
        }
        return true;
    }
}
