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
public class RetrieveBackEndDataTask extends AsyncTask<String, Void, String> {
    private OnTaskComplete onTaskComplete;
    private ProgressDialog progDailog;
    private Context context;

    public RetrieveBackEndDataTask(Context ctx){
        context = ctx;
        progDailog = new ProgressDialog(ctx);
    }

    /*@Override
    protected void onPreExecute() {
        super.onPreExecute();
        progDailog.setMessage("Loading...");
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(true);
        progDailog.show();
    }*/


    /**
     *
     * @param params; params[0] = file name, params[1] - params[n] = complete search parameters
     * @return
     */
    @Override
    protected String doInBackground(String... params) {
        URL url;
        HttpURLConnection conn = null;
        BufferedReader read = null;

        String baseUrl = "http://104.236.195.188/php/" + params[0] + "?";
        for(int k = 1; k < params.length; k++){
            if(k == 1){
                baseUrl += params[k];
            }else{
                baseUrl += "&" + params[k];
            }
        }
        InputStream output = null;
        StringBuilder builder = new StringBuilder();
        String charset = "UTF-8";
        try {
            url = new URL(baseUrl);
            Log.e("GemsQuery", url.toString());
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
