package com.funbetweenus.funbetweenus;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Grant Peltier on 3/23/15.
 */
public class User implements Parcelable {

    private String name, deviceId, email, id;
    private int ufunsId;


    public User(String name, String deviceId, String email, String id, int ufunsId){
        this.name = name;
        this.deviceId = deviceId;
        this.email = email;
        this.id = id;
        this.ufunsId = ufunsId;
    }

    private User(Parcel in){
        name = in.readString();
        deviceId = in.readString();
        email = in.readString();
        id = in.readString();
        ufunsId = in.readInt();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>(){

        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size){
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(deviceId);
        dest.writeString(email);
        dest.writeString(id);
        dest.writeInt(ufunsId);
    }


    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public int getUfunsId() {
        return ufunsId;
    }
}
