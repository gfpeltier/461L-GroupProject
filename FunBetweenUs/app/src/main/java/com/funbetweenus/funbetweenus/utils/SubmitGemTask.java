package com.funbetweenus.funbetweenus.utils;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.funbetweenus.funbetweenus.R;
import com.funbetweenus.funbetweenus.data.Gem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Grant Peltier on 4/20/15.
 */
public class SubmitGemTask extends AsyncTask<Gem, Void, String> {


    //TODO: NEED to implement callback for this Async as done in last homework
    @Override
    protected String doInBackground(Gem... params) {
        Gem newGem = params[0];
        Log.e("GEM_IN_BACK", newGem.toString());
        URL url;
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
            url = new URL(strUrl);
            Log.v("Query", url.toString());
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(15*1000);
            conn.connect();

            read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String ln = null;
            while((ln = read.readLine()) != null){
                builder.append(ln);
            }
            Log.i("Gem_Message",conn.getResponseMessage());
            Log.i("Gem_Code", ""+conn.getResponseCode());
            read.close();
            return builder.toString();
        }catch (Exception e){
            e.printStackTrace();
            Log.e("ERROR", ""+e.getMessage());
            Log.e("ERROR", "" + e.toString());
            return null;
        }
    }
}
