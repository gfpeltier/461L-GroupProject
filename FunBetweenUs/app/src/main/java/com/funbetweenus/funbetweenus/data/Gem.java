package com.funbetweenus.funbetweenus.data;

import android.content.Intent;

import com.funbetweenus.funbetweenus.User;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by Grant Peltier on 4/20/15.
 */
public class Gem {

    private LatLng location;
    private String title;
    private String description;
    private int userId;
    private int id;
    private Date date;


    public Gem(LatLng location, User user) {
        this.location = location;
        this.date = getDate();
        this.userId = user.getUfunsId();
    }

    public Gem(LatLng location, String title, String description, int userId, int id, Date date) {
        this.location = location;
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.id = id;
        this.date = date;
    }


    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Override
    public String toString(){
        return "Lat: " + location.latitude +
                ", Lng: " + location.longitude +
                ", title: " + title +
                ", desc: " + description +
                ", user: " + userId;
    }

}
