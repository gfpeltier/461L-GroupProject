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
 * Created by Grant Peltier on 5/9/15.
 */
public class RetrievePathGemsTask extends AsyncTask<String, Void, ArrayList<String>> {
    private OnTaskComplete onTaskComplete;
    private ProgressDialog progDailog;
    private Context context;
    private String apiKey;
    private String category;
    private ArrayList<LatLng> points;


    public RetrievePathGemsTask(Context ctx, ArrayList<LatLng> qPoints){
        context = ctx;
        progDailog = new ProgressDialog(ctx);
        points = qPoints;
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


    /**
     *
     * @param params: params[0] = radius (in meters)
     * @return
     */
    @Override
    protected ArrayList<String> doInBackground(String... params) {
        URL url;
        HttpURLConnection conn = null;
        BufferedReader read = null;
        ArrayList<String> resultArray = new ArrayList<String>();
        Iterator i = points.iterator();
        while (i.hasNext()) {
            LatLng next = (LatLng) i.next();
            String baseUrl = "http://104.236.195.188/php/findGemsOnPath.php?lat=" + next.latitude
                    + "&lng=" + next.longitude
                    + "&radius=" + params[0];
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
                Log.e("GemMessage", conn.getResponseMessage());
                Log.e("GemCode", "" + conn.getResponseCode());
                resultArray.add(builder.toString());
                Log.e("GemResponse", builder.toString());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ERROR", "" + e.getMessage());
                Log.e("ERROR", "" + e.toString());
                return null;
            }
        }
        return resultArray;
    }

    @Override
    protected void onPostExecute(ArrayList<String> json){
        onTaskComplete.setMyTaskComplete(json);
        progDailog.dismiss();
    }

    public interface OnTaskComplete {
        public void setMyTaskComplete(ArrayList<String> message);
    }

    public void setMyTaskCompleteListener(OnTaskComplete onTaskComplete) {
        this.onTaskComplete = onTaskComplete;
    }
}
