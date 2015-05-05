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
 * Created by Grant Peltier on 5/5/15.
 */
public class RetrievePlacesDataTask extends AsyncTask<String, Void, ArrayList<String>> {
    private OnTaskComplete onTaskComplete;
    private ProgressDialog progDailog;
    private Context context;
    private String apiKey;
    private String category;
    private ArrayList<LatLng> points;


    public RetrievePlacesDataTask(Context ctx, String key, ArrayList<LatLng> qPoints){
        context = ctx;
        progDailog = new ProgressDialog(ctx);
        apiKey = key;
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


    @Override
    protected ArrayList<String> doInBackground(String... params) {
        URL url;
        HttpURLConnection conn = null;
        BufferedReader read = null;
        ArrayList<String> resultArray = new ArrayList<String>();
        Iterator i = points.iterator();
        while (i.hasNext()) {
            LatLng next = (LatLng) i.next();
            String baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + next.latitude +
                    "," + next.longitude + "&radius=" + params[0] + "&types=" + params[1] + "&opennow=true&key=" + apiKey;
            //String address = params[1];
            InputStream output = null;
            StringBuilder builder = new StringBuilder();
            //String apiKey = params[2];
            String charset = "UTF-8";
            try {
                //address = URLEncoder.encode(address, "UTF-8");
                //address.replaceAll("%2C", ",");
                url = new URL(baseUrl);
                Log.e("PlacesQuery", url.toString());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(15 * 1000);
                conn.connect();
                //conn.setRequestProperty("Accept-Charset", charset);
                //byte[] out = new byte[1024];
                //output = conn.getInputStream();
                read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String ln = null;
                while ((ln = read.readLine()) != null) {
                    builder.append(ln);
                }
                Log.v("Message", conn.getResponseMessage());
                Log.v("Code", "" + conn.getResponseCode());
                resultArray.add(builder.toString());
                //int amt = output.read();
                //Log.v("NUMBYTESREAD", ""+amt);
                //byte[] xmlOut = new byte[100000];
                //output.read(xmlOut);
                //String oString = new String(xmlOut, "UTF-8");
                //Log.v("XML",oString);
                //output.close();
                //return oString;
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
