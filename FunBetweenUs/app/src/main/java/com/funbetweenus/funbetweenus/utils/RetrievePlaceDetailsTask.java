package com.funbetweenus.funbetweenus.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by grantpeltier93 on 5/5/15.
 */
public class RetrievePlaceDetailsTask extends AsyncTask<String, Void, String>{
    private OnTaskComplete onTaskComplete;
    private ProgressDialog progDailog;
    private Context context;
    private String apiKey;
    private String category;


    public RetrievePlaceDetailsTask(Context ctx, String key){
        context = ctx;
        progDailog = new ProgressDialog(ctx);
        apiKey = key;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progDailog.setMessage("Loading...");
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(true);
        progDailog.show();
    }


    @Override
    protected String doInBackground(String... params) {
        URL url;
        HttpURLConnection conn = null;
        BufferedReader read = null;
        String baseUrl = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + params[0] + "&key=" + apiKey;
        InputStream output = null;
        StringBuilder builder = new StringBuilder();
        String charset = "UTF-8";
        try {
            url = new URL(baseUrl);
            Log.e("PlacesQuery", url.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(15 * 1000);
            conn.connect();
            read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String ln = null;
            while ((ln = read.readLine()) != null) {
                builder.append(ln);
            }
            Log.v("Message", conn.getResponseMessage());
            Log.v("Code", "" + conn.getResponseCode());
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ERROR", "" + e.getMessage());
            Log.e("ERROR", "" + e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String json){
        onTaskComplete.setMyTaskComplete(json);
        progDailog.dismiss();
    }

    public interface OnTaskComplete {
        public void setMyTaskComplete(String message);
    }

    public void setMyTaskCompleteListener(OnTaskComplete onTaskComplete) {
        this.onTaskComplete = onTaskComplete;
    }
}
