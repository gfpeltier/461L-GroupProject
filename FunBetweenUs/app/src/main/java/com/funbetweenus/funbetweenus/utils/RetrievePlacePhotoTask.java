package com.funbetweenus.funbetweenus.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Grant Peltier on 5/6/15.
 */
public class RetrievePlacePhotoTask extends AsyncTask<String, Void, Bitmap> {
    private OnTaskComplete onTaskComplete;
    private ProgressDialog progDailog;
    private Context context;
    private String apiKey;
    private String category;


    public RetrievePlacePhotoTask(Context ctx, String key){
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
    protected Bitmap doInBackground(String... params) {
        URL url;
        HttpURLConnection conn = null;
        BufferedReader read = null;
        String baseUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + params[0] + "&key=" + apiKey;
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
            Bitmap photo = BitmapFactory.decodeStream(conn.getInputStream());
            Log.e("BitMapPhoto", ""+photo.getWidth());
            return photo;
            /*String ln = null;
            while ((ln = read.readLine()) != null) {
                builder.append(ln);
            }
            Log.v("Message", conn.getResponseMessage());
            Log.v("Code", "" + conn.getResponseCode());
            return builder.toString();*/
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ERROR", "" + e.getMessage());
            Log.e("ERROR", "" + e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap stream){
        onTaskComplete.setMyTaskComplete(stream);
        progDailog.dismiss();
    }

    public interface OnTaskComplete {
        public void setMyTaskComplete(Bitmap photoStream);
    }

    public void setMyTaskCompleteListener(OnTaskComplete onTaskComplete) {
        this.onTaskComplete = onTaskComplete;
    }
}
