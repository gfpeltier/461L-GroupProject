package com.funbetweenus.funbetweenus.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.funbetweenus.funbetweenus.MainActivity;
import com.funbetweenus.funbetweenus.R;
import com.funbetweenus.funbetweenus.User;
import com.funbetweenus.funbetweenus.data.Gem;
import com.funbetweenus.funbetweenus.data.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Cassandra on 5/5/2015.
 */
public class FindingGems extends AsyncTask<Void, Void, String> {
    protected GoogleMap mMap;
    private OnTaskComplete onTaskComplete;
    private ProgressDialog progDailog;
    private Context context;
    private String  userId;

    public FindingGems(Context ctx){
        context = ctx;
        progDailog = new ProgressDialog(ctx);
    }

    public FindingGems(Context ctx, User user){
        context = ctx;
        progDailog = new ProgressDialog(ctx);
        this.userId = user.getId();
    }

    @Override
    protected String doInBackground(Void... params) {

        String result = null;
        InputStream inpt = null;
        URI uri;
        URL url;
        HttpURLConnection conn = null;
        BufferedReader read = null;
       /* User current = new User();
        String id = current.getId();*/
        ArrayList<String> Gems = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        try{
             uri = new URI("http", "104.236.195.188",
                     "/php/findGem.php",
                     null);
            String strUrl = "http://" + "104.236.195.188" + "/php/findGem.php?uid=" + userId;
            url = new URL(strUrl);
            conn = (HttpURLConnection)url.openConnection();
            int statusCode = conn.getResponseCode();
            if (statusCode != 200 /* or statusCode <= 200 && statusCode < 300 */) {
                inpt = conn.getErrorStream();
                System.out.println("******** Error");
            }

            //conn.setDoOutput(false);
            conn.setReadTimeout(15*1000);
            conn.connect();

            read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String ln = null;
            while((ln = read.readLine()) != null){
                builder.append(ln +"/n");
            }
            Log.e("Gem_JSON", ""+builder.toString());
            read.close();
            //result = builder.toString()
            return builder.toString();

        }catch (Exception e){
            e.printStackTrace();
            Log.e("ERROR", ""+e.getMessage());
            Log.e("ERROR", "" + e.toString());
            return null;
        }
    }

    protected void onPostExecute(String xml){
        onTaskComplete.setMyTaskComplete(xml);
        progDailog.dismiss();
    }

    public interface OnTaskComplete {
        public void setMyTaskComplete(String message);
    }

    public void setMyTaskCompleteListener(OnTaskComplete onTaskComplete) {
        this.onTaskComplete = onTaskComplete;
    }

}
