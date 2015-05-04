package com.funbetweenus.funbetweenus.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.funbetweenus.funbetweenus.R;
import com.funbetweenus.funbetweenus.data.Gem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by Grant Peltier on 4/20/15.
 */
public class SubmitGemTask extends AsyncTask<Gem, Void, String> {


    private OnTaskComplete onTaskComplete;
    private ProgressDialog progDailog;
    private Context context;

    public SubmitGemTask(Context ctx){
        context = ctx;
        progDailog = new ProgressDialog(ctx);
    }


    //TODO: NEED to implement callback for this Async as done in last homework
    @Override
    protected String doInBackground(Gem... params) {
        Gem newGem = params[0];
        Log.e("GEM_IN_BACK", newGem.toString());
        URL url;
        URI uri;
        HttpURLConnection conn = null;
        BufferedReader read = null;
        String strUrl = "http://104.236.195.188" + "/php/addGem.php?lat=" + newGem.getLocation().latitude +
                "&lng=" + newGem.getLocation().longitude +
                "&title=" + newGem.getTitle() +
                "&description=" + newGem.getDescription() +
                "&user=" + newGem.getUserId();
        StringBuilder builder = new StringBuilder();
        String charset = "UTF-8";
        try{
            uri = new URI("http", "104.236.195.188",
                    "/php/addGem.php",
                    "lat=" + newGem.getLocation().latitude + "&lng=" + newGem.getLocation().longitude + "&title=" + newGem.getTitle() + "&description=" + newGem.getDescription() + "&user=" + newGem.getUserId(),
                    null);
            url = uri.toURL();
            Log.e("Gem_URL", url.toString());
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(15*1000);
            conn.connect();

            read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String ln = null;
            while((ln = read.readLine()) != null){
                builder.append(ln);
            }
            Log.e("Gem_Message",conn.getResponseMessage());
            Log.e("Gem_Code", ""+conn.getResponseCode());
            Log.e("Gem_JSON", ""+builder.toString());
            read.close();
            return builder.toString();
        }catch (Exception e){
            e.printStackTrace();
            Log.e("ERROR", ""+e.getMessage());
            Log.e("ERROR", "" + e.toString());
            return null;
        }
    }


    @Override
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
