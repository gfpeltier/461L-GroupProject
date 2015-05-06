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

    public FindingGems(Context ctx){
        context = ctx;
        progDailog = new ProgressDialog(ctx);
    }

    @Override
    protected String doInBackground(Void... params) {

        String result = null;
        InputStream inpt = null;
        URI uri;
        URL url;
        HttpURLConnection conn = null;
        BufferedReader read = null;

        ArrayList<String> Gems = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        try{
             uri = new URI("http", "104.236.195.188",
                     "/php/findGem.php",
                     null);

            //url = new URL("http://104.236.195.188/php/findGem.php?");

            //url = uri.toURL();
            String strUrl = "http://" + "104.236.195.188" + "/php/findGem.php?uid=" + "16";
            url = new URL(strUrl);
            //Log.e("Gem_URL", url.toString());
            //System.out.println("******************* GEM_URL: " + url.toString());
            //conn.setRequestMethod("GET");
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
            //result = builder.toString();
            return builder.toString();
        }catch (Exception e){
            e.printStackTrace();
            Log.e("ERROR", ""+e.getMessage());
            Log.e("ERROR", "" + e.toString());
            return null;
        }
/*
        //parse json data
        try{
            JSONArray jsonArray = new JSONArray(result);
            for (int i=0; i<jsonArray.length()-1; i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                Gems.add(jsonData.getString("latitude"));
                Gems.add(jsonData.getString("longitude"));
                Gems.add(jsonData.getString("title"));
                Gems.add(jsonData.getString("description"));
                Gems.add(jsonData.getString("user"));
            }
        }catch (JSONException e) {
            Log.e("log_tag", "Error parsing data" + e.toString());
        }

       // System.out.println(Gems);
        return Gems;*/
    }

  /*  protected void onPostExecute(String xml){
        onTaskComplete.setMyTaskComplete(xml);
        progDailog.dismiss();
    }*/

    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.i("CheckDBResult", result);
        // After completing http call
        // will close this activity and lauch main activity
        JSONObject jsonObject = null;
        JSONObject gemObject = null;
        //placeResults = new ArrayList<Place>();
        String code = "";
        try{
            jsonObject = new JSONObject(result);
            gemObject = jsonObject.getJSONObject("object");
            code = (String) jsonObject.get("code");
            Log.i("ResultCode", code);
        }catch(Exception e){
            e.printStackTrace();
        }

        if(code.equals("success")){
            try {
                JSONArray jsonResults = (JSONArray) jsonObject.get("result");
                for (int k = 0; k < jsonResults.length(); k++) {
                    JSONObject jsonResult = jsonResults.getJSONObject(k);
                    LatLng latLng = new LatLng(Double.parseDouble(jsonResult.getString("latitude")),Double.parseDouble(jsonResult.getString("longitude")));
                    String title = jsonResult.getString("title");
                    String id = jsonResult.getString("id");
                    String description = jsonResult.getString("desription");
                    Place newPlace = new Place(latLng, title, id, null, description);
                    Log.e("GeneratedPlace", newPlace.toString());
                    //if(!placeResults.contains(newPlace)){
                        Marker mark = mMap.addMarker(new MarkerOptions()
                                .title(newPlace.getName())
                                .snippet(newPlace.getVicinity())
                                .position(newPlace.getLocation()));
                        newPlace.setMarker(mark);
                      //  placeResults.add(newPlace);
                    //}
                }
            }catch (JSONException e){
                    e.printStackTrace();
                }

        }else{
            //error
        }
    }
    public interface OnTaskComplete {
        public void setMyTaskComplete(String message);
    }

    public void setMyTaskCompleteListener(OnTaskComplete onTaskComplete) {
        this.onTaskComplete = onTaskComplete;
    }

}
