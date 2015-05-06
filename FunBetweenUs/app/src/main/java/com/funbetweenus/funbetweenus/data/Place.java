package com.funbetweenus.funbetweenus.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Grant Peltier on 5/5/15.
 */
public class Place {
    private LatLng location;
    private String name;
    private String id;
    private String photo_ref;
    private String vicinity;
    private Marker marker;


    private String formattedAddress;
    private String phone;
    private String operatingHours;


    public Place(LatLng loc, String pName, String pid, String photo, String addr){
        location = loc;
        name = pName;
        id = pid;
        photo_ref = photo;
        vicinity = addr;
    }

    public void setMarker(Marker mark){
        marker = mark;
    }

    public LatLng getLocation(){return location;}

    public String getId(){return id;}

    public String getName(){return name;}

    public String getVicinity(){return vicinity;}

    public String getPhoto_ref(){return photo_ref;}

    public Marker getMarker(){return marker;}


    @Override
    public String toString(){
        return "{'location': " + location.toString() + ", 'name': " + name +", 'id': "+ id + ", 'photo_ref':" + photo_ref + ", 'vicinity':" + vicinity + "}";
    }

    public boolean equals(Place other){
        return id.equals(other.getId());
    }
}
