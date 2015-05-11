package com.funbetweenus.funbetweenus;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.funbetweenus.funbetweenus.utils.OnTaskCompleted;

import org.json.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class TitleActivity extends ActionBarActivity implements OnTaskCompleted {

    public static Toast myToast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_title);

        myToast = Toast.makeText(getApplicationContext(), "Please enable Data and Location.", Toast.LENGTH_SHORT);

        /**
         * Showing splashscreen while making network calls to download necessary
         * data before launching the app Will use AsyncTask to make http call
         */
        new PrefetchData(this).execute();
    }

    public void waitDataGPS(){
        LocationManager lm = null;
        boolean gps_enabled = false,network_enabled = false;

        int counter = 0;
       while(!gps_enabled&&!network_enabled){
            if(lm==null)
                lm = (LocationManager) getApplicationContext().getSystemService(getApplicationContext().LOCATION_SERVICE);
            try{
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }catch(Exception ex){}
            try{
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }catch(Exception ex){}
           if(counter==999){
               myToast.show();
               counter = 0;
           }
           counter++;
        }

    }

    @Override
    public void onTaskCompleted(JSONObject obj) {

        String code = null;
        JSONObject userObject = null;

        try{
            code = (String) obj.get("code");
            userObject = obj.getJSONObject("object");
            Log.i("ResultCode", code);
            Thread.sleep(2000);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(code != null && code.equals("success")){                           //Needed to move this block out of background thread
            User currentUser = null;
            assert userObject != null;
            try {
                currentUser = new User(userObject.getString("user_name"), userObject.getString("device_id"), userObject.getString("user_email"), userObject.getString("user_id"), Integer.parseInt(userObject.getString("id")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i("CurrentUserName", currentUser.getName());
            Intent existingUser = new Intent(TitleActivity.this, MainActivity.class);
            existingUser.putExtra("currentUser", currentUser);
            startActivity(existingUser);
        }else{
            Intent newUser = new Intent(TitleActivity.this, LoginActivity.class);
            newUser.putExtra("fromMain", false);
            startActivity(newUser);
        }
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_title, menu);
        return true;
    }*/

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/


    /**
     * Async Task to make http call
     */
    private class PrefetchData extends AsyncTask<Void, Void, String> {

        private TitleActivity listener;

        public PrefetchData(TitleActivity listener){
            this.listener = listener;
        }


        TelephonyManager telephonyManager;
        String deviceId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls
            telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            waitDataGPS();

            /*
             * Will make http call here This call will download required data
             * before launching the app
             * example:
             * 1. Downloading and storing in SQLite
             * 2. Downloading images
             * 3. Fetching and parsing the xml / json
             * 4. Sending device information to server
             * 5. etc.,
             */
            URL url;
            HttpURLConnection conn = null;
            BufferedReader read = null;
            String strUrl = "http://" + getString(R.string.serverIPAddress) + "/php/checkDevice.php?deviceId=" + deviceId;
            InputStream output = null;
            StringBuilder builder = new StringBuilder();
            String charset = "UTF-8";
            try{
                url = new URL(strUrl);
                Log.v("Query", url.toString());
                conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(15*1000);
                conn.connect();
                //conn.setRequestProperty("Accept-Charset", charset);
                //byte[] out = new byte[1024];
                //output = conn.getInputStream();
                read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String ln = null;
                while((ln = read.readLine()) != null){
                    builder.append(ln);
                }
                Log.i("Message",conn.getResponseMessage());
                Log.i("Code", ""+conn.getResponseCode());
                conn.disconnect();
                return builder.toString();
                //int amt = output.read();
                //Log.v("NUMBYTESREAD", ""+amt);
                //byte[] xmlOut = new byte[100000];
                //output.read(xmlOut);
                //String oString = new String(xmlOut, "UTF-8");
                //Log.v("XML",oString);
                //output.close();
                //return oString;
            }catch (Exception e){
                e.printStackTrace();
                Log.e("ERROR", ""+e.getMessage());
                Log.e("ERROR", "" + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("CheckDBResult", result);
            // After completing http call
            // will close this activity and lauch main activity
            JSONObject rootOfResult = null;
            JSONObject userObject = null;
            String code = null;
            try{
                rootOfResult = new JSONObject(result);
                code = (String) rootOfResult.get("code");
                userObject = rootOfResult.getJSONObject("object");
                Log.i("ResultCode", code);
            }catch(Exception e){
                e.printStackTrace();
            }
            listener.onTaskCompleted(rootOfResult);
            finish();
            /*if(code.equals("success")){                           //Needed to move this block out of background thread
                try{
                    Thread.sleep(2000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                User currentUser = null;
                assert userObject != null;
                try {
                    currentUser = new User(userObject.getString("user_name"), userObject.getString("device_id"), userObject.getString("user_email"), userObject.getString("user_id"), Integer.parseInt(userObject.getString("id")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("CurrentUserName", currentUser.getName());
                Intent existingUser = new Intent(TitleActivity.this, MainActivity.class);
                existingUser.putExtra("currentUser", currentUser);
                startActivity(existingUser);
            }else{
                try{
                    Thread.sleep(2000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                Intent newUser = new Intent(TitleActivity.this, LoginActivity.class);
                startActivity(newUser);
            }*/



            // close this activity
            finish();
        }

    }



}
