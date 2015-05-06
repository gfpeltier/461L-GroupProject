package com.funbetweenus.funbetweenus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.funbetweenus.funbetweenus.data.DirectionsLeg;
import com.funbetweenus.funbetweenus.data.DirectionsRoute;
import com.funbetweenus.funbetweenus.data.DirectionsStep;

import com.funbetweenus.funbetweenus.data.Gem;
import com.funbetweenus.funbetweenus.data.Place;
import com.funbetweenus.funbetweenus.utils.FineQueryPointsFinder;
import com.funbetweenus.funbetweenus.utils.PointsAlgorithm;
import com.funbetweenus.funbetweenus.utils.RetrievePlaceDetailsTask;
import com.funbetweenus.funbetweenus.utils.RetrievePlacePhotoTask;
import com.funbetweenus.funbetweenus.utils.RetrievePlacesDataTask;
import com.funbetweenus.funbetweenus.utils.SubmitGemTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.funbetweenus.funbetweenus.R.id.nearby_address_search_btn;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, TextWatcher, AdapterView.OnItemSelectedListener {

    private User user;
    protected GoogleMap mMap;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private FrameLayout mDrawerFrame;

    private static final int MESSAGE_TEXT_CHANGED = 0;
    private static final int AUTOCOMPLETE_DELAY = 500;
    private static final int THRESHOLD = 3;
    private static final double METERSINMILE = 1609.0;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "174213887506";

    private List<Address> autoCompleteSuggestionAddresses;
    private ArrayAdapter<String> autoCompleteAdapter;
    private AutoCompleteTextView locationInput;
    private Address destination;
    private Marker destMarker;
    private Marker userMarker;
    private boolean noGPSFlag;
    private boolean placeGem;

    private ArrayList<DirectionsRoute> routesToDestination;
    private ArrayList<Place> placeResults;
    private List<LatLng> directionsPathPoints;
    private Polyline fullPath;
    private boolean viewPathState;
    private double lowLimitSearchRadius;
    private double highLimitSearchRadius;


    GoogleCloudMessaging gcm;
    String regid;
    AtomicInteger msgId = new AtomicInteger();

    protected Context mainCon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initializeSpinners();

        mainCon = getBaseContext();

        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(mainCon);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i("GCM", "No valid Google Play Services APK found.");
        }

        String greet = "Hello, ";
        try {
            user = getIntent().getExtras().getParcelable("currentUser");
            Log.i("CURRENT USER NAME", user.getName());
            greet += user.getName();
            //setTitle(greet);
        }catch (Exception e){
            e.printStackTrace();
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerFrame = (FrameLayout) findViewById(R.id.drawer_frame);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        String[] mDrawerOptions = getResources().getStringArray(R.array.nav_drawer_items);

        if(user != null){
            TextView txt = (TextView)findViewById(R.id.nav_bar_greet);
            txt.setText(greet);
        }

        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.user_mode_switch);
        Paint paint = toggleButton.getPaint();
        float length = paint.measureText(getString(R.string.user_mode_on_str));

        toggleButton.setWidth((int) length+toggleButton.getPaddingLeft()+toggleButton.getPaddingRight());


        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerOptions));
        mDrawerList.setOnItemClickListener((ListView.OnItemClickListener) new DrawerItemClickListener());

        ImageButton img = (ImageButton) findViewById(R.id.menu_button);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("CLICK MENU", "MENU BUTTON CLICKED!!!!");
                if(!mDrawerLayout.isDrawerOpen(mDrawerFrame)){
                    Log.e("Drawer Status", "Closed!");
                    mDrawerLayout.openDrawer(mDrawerFrame);
                    if(mDrawerLayout.isDrawerVisible(mDrawerFrame)){
                        Log.e("Drawer Vis","Drawer Now OPEN AND VISIBLE");
                    }else{Log.e("Drawer Vis","Drawer Now OPEN AND NOT VISIBLE");}
                }else{
                    Log.e("Drawer Status", "Open!");
                    mDrawerLayout.closeDrawer(mDrawerFrame);}

            }
        });



        locationInput = (AutoCompleteTextView) findViewById(R.id.geo_search_edit);
        locationInput.addTextChangedListener(this);
        locationInput.setOnItemSelectedListener(this);
        locationInput.setThreshold(THRESHOLD);


    }

    @Override
    protected void onResume(){
        super.onResume();

    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("GCM", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("GCM", "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("GCM", "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            Log.e("AppVersion", ""+context.toString());
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            Log.e("PackInfo", ""+packageInfo.toString());
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(mainCon);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the registration ID - no need to register again.
                    storeRegistrationId(mainCon, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");

            }

        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("GCM", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }


    private void setSearchBounds(double low, double high){
        lowLimitSearchRadius = low;
        highLimitSearchRadius = high;
    }

    private double evaluateRealRadius(int progress){
        double constant = lowLimitSearchRadius;
        double increment = ((highLimitSearchRadius - lowLimitSearchRadius) / 100.0);
        return (progress * increment) + constant;
    }

    public static double roundDouble(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void setSliderReading(){
        SeekBar seekBar = (SeekBar) findViewById(R.id.radius_slider);
        int progress = seekBar.getProgress();
        double realRadius = evaluateRealRadius(progress);
        TextView reading = (TextView) findViewById(R.id.radius_reading);
        DecimalFormat df = new DecimalFormat("####0.00");
        reading.setText(df.format(realRadius)+"mi");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView reading = (TextView) findViewById(R.id.radius_reading);
                double realVal = evaluateRealRadius(progress);
                DecimalFormat df = new DecimalFormat("####0.00");
                //Log.e("ProgressChanged", ""+df.format(realVal));
                reading.setText(df.format(realVal)+"mi");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    /**
     *
     * OnClick Methods
     */

    public void backToAddressEnter(View view){
        findRadiusViewState(false);
    }

    public void backToChooseRadius(View view) { showScrollUI(false); }

    public void onToggleClicked(View view){
        boolean on = ((ToggleButton) view).isChecked();
        Log.e("ToggleMode","Toggling user mode");
        if(on){
            Log.e("User Mode On", "Find a friend mode");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.dialog_find_friend, null))
                .setTitle(getString(R.string.find_friend_dialog_title))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        ToggleButton tgl = (ToggleButton) findViewById(R.id.user_mode_switch);
                        tgl.setChecked(false);
                    }
                });
            AlertDialog dialog = builder.create();
            findViewById(R.id.friend_search_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: Need to contact GCM here
                }
            });
            dialog.show();
        }else{
            //TODO: anything to do when set to "JUST ME!"
        }
    }


    public void findFunListener(View view){
        //ArrayList<LatLng> queryPoints = new PointsAlgorithm().getPoints(routesToDestination.get(0));
        ArrayList<LatLng> queryPoints = new FineQueryPointsFinder(routesToDestination.get(0).getDistance(), directionsPathPoints).getQueryPoints();
        SeekBar radiusSlider = (SeekBar) findViewById(R.id.radius_slider);
        double radius = (int) Math.round(evaluateRealRadius(radiusSlider.getProgress()) * METERSINMILE);
        Spinner categorySpinner = (Spinner) findViewById(R.id.what_to_do_spinner);
        String category = categorySpinner.getSelectedItem().toString();
        RetrievePlacesDataTask search = new RetrievePlacesDataTask(this,getString(R.string.google_places_key), queryPoints);
        Iterator<LatLng> i = queryPoints.iterator();
        switch(category){
            case "Restaurants":
                search.execute(String.valueOf(radius), "restaurant");
                search.setMyTaskCompleteListener(new RetrievePlacesDataTask.OnTaskComplete(){

                    @Override
                    public void setMyTaskComplete(ArrayList<String> json) {
                        parsePlaceJSON(json);
                    }
                });
                break;
            case "Bars":
                search.execute(String.valueOf(radius), "bar");
                search.setMyTaskCompleteListener(new RetrievePlacesDataTask.OnTaskComplete() {

                    @Override
                    public void setMyTaskComplete(ArrayList<String> json) {
                        parsePlaceJSON(json);
                    }
                });
                break;
            case "Cafes":
                search.execute(String.valueOf(radius), "cafe");
                search.setMyTaskCompleteListener(new RetrievePlacesDataTask.OnTaskComplete() {

                    @Override
                    public void setMyTaskComplete(ArrayList<String> json) {
                        parsePlaceJSON(json);
                    }
                });
                break;
            case "Wellness":
                search.execute(String.valueOf(radius), "health");
                search.setMyTaskCompleteListener(new RetrievePlacesDataTask.OnTaskComplete() {

                    @Override
                    public void setMyTaskComplete(ArrayList<String> json) {
                        parsePlaceJSON(json);
                    }
                });
                break;
            case "Gems":
                while (i.hasNext()){
                    LatLng next = i.next();
                    //TODO: Need to implement something else for gems
                }
                break;
            case "Out N About":
                showToast("This category is not yet supported");
                break;
        }
    }


    private void parsePlaceJSON(ArrayList<String> results){
        Log.e("JSONResponse", ""+results.get(0));
        Iterator<String> i = results.iterator();
        placeResults = new ArrayList<Place>();
        while(i.hasNext()){
            String queryResultStr = i.next();
            try {
                JSONObject queryResult = new JSONObject(queryResultStr);
                JSONArray jsonResults = (JSONArray) queryResult.get("results");
                for(int k = 0; k < jsonResults.length(); k++){
                    JSONObject jsonResult = jsonResults.getJSONObject(k);
                    JSONObject geo = jsonResult.getJSONObject("geometry");
                    JSONObject loc = geo.getJSONObject("location");
                    LatLng latLng = new LatLng(Double.parseDouble(loc.getString("lat")),Double.parseDouble(loc.getString("lng")));
                    String name = jsonResult.getString("name");
                    String id = jsonResult.getString("place_id");
                    JSONArray photoArr = jsonResult.getJSONArray("photos");
                    JSONObject photoObj = photoArr.getJSONObject(0);
                    String photo = photoObj.getString("photo_reference");
                    String vicinity = jsonResult.getString("vicinity");
                    Place newPlace = new Place(latLng, name, id, photo, vicinity);
                    Log.e("GeneratedPlace", newPlace.toString());
                    if(!placeResults.contains(newPlace)){
                        Marker mark = mMap.addMarker(new MarkerOptions()
                            .title(newPlace.getName())
                            .snippet(newPlace.getVicinity())
                            .position(newPlace.getLocation()));
                        newPlace.setMarker(mark);
                        placeResults.add(newPlace);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        populatePlacesScrollView();
    }


    private void populatePlacesScrollView(){
        ScrollView sv = (ScrollView) findViewById(R.id.place_scroll_view);
        sv.removeAllViews();
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        Iterator<Place> i = placeResults.iterator();
        while(i.hasNext()){
            final Place current = i.next();
            LinearLayout ill = new LinearLayout(this);
            ill.setBackground(getResources().getDrawable(R.drawable.scroll_back));
            ill.setWeightSum(2);
            TextView tv = new TextView(this);
            TextView tv2 = new TextView(this);
            tv.setText(current.getName());
            tv.setTextSize(22);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(15, 0, 25, 0);
            tv.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            tv2.setText(current.getVicinity());
            tv2.setTextSize(18);
            tv2.setGravity(Gravity.CENTER);
            tv2.setPadding(0, 0, 0, 15);
            tv2.setLayoutParams(new TableLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            ill.addView(tv);
            ill.addView(tv2);
            ill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: Need to figure out how to link the object to this listener. Highlight marker, get picture, and do other necessary things
                    Iterator<Place> j = placeResults.iterator();
                    while (j.hasNext()){j.next().getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));}
                    LinearLayout placeTextDetails = (LinearLayout) findViewById(R.id.place_text_details);
                    placeTextDetails.removeAllViews();

                    current.getMarker().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(current.getMarker().getPosition()));

                    RetrievePlaceDetailsTask detailsTask = new RetrievePlaceDetailsTask(MainActivity.this, getString(R.string.google_places_key));
                    detailsTask.execute(current.getId());
                    detailsTask.setMyTaskCompleteListener(new RetrievePlaceDetailsTask.OnTaskComplete() {
                        @Override
                        public void setMyTaskComplete(String message) {
                            parseDetails(message,current);
                        }
                    });
                    RetrievePlacePhotoTask photoTask = new RetrievePlacePhotoTask(MainActivity.this, getString(R.string.google_places_key));
                    photoTask.execute(current.getPhoto_ref());
                    photoTask.setMyTaskCompleteListener(new RetrievePlacePhotoTask.OnTaskComplete() {
                        @Override
                        public void setMyTaskComplete(Bitmap photo) {
                            Log.e("PHOTOBITMAP", ""+photo.getByteCount());
                            ImageView iv = (ImageView) findViewById(R.id.place_photo);
                            iv.setImageBitmap(photo);

                        }
                    });

                    findViewById(R.id.back_to_list_btn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            findViewById(R.id.place_scroll_view).setVisibility(View.VISIBLE);
                            findViewById(R.id.place_details_layout).setVisibility(View.GONE);
                        }
                    });

                    findViewById(R.id.place_scroll_view).setVisibility(View.GONE);
                    findViewById(R.id.place_details_layout).setVisibility(View.VISIBLE);
                }
            });
            ll.addView(ill);
        }
        sv.addView(ll);
        showScrollUI(true);
    }


    private void parseDetails(String detailsJSON,Place c){
        LinearLayout deets = (LinearLayout) findViewById(R.id.place_text_details);
        TextView tv3 = new TextView(MainActivity.this);
        TextView tv4 = new TextView(MainActivity.this);
        TextView tv5 = new TextView(MainActivity.this);
        TextView tv6 = new TextView(MainActivity.this);
        TextView tv7 = new TextView(MainActivity.this);
        StringBuilder currentAddress = new StringBuilder("");
        StringBuilder currentPhone = new StringBuilder("");
        StringBuilder currentHours = new StringBuilder("");
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        try {
            JSONObject queryResult1 = new JSONObject(detailsJSON);
            JSONObject resultJson = queryResult1.getJSONObject("result");
            currentAddress.append(resultJson.getString("formatted_address"));
            currentPhone.append(resultJson.getString("formatted_phone_number"));
            JSONObject hoursObject = resultJson.getJSONObject("opening_hours");
            JSONArray weekdayArray = hoursObject.getJSONArray("weekday_text");
            currentHours.append(weekdayArray.get(day-2));
            /*switch (day){
                case 1: currentHours.append(weekdayObject.getString("Sunday"));
                break;
                case 2: currentHours.append(weekdayObject.getString("Monday"));
                break;
                case 3: currentHours.append(weekdayObject.getString("Tuesday"));
                break;
                case 4: currentHours.append(weekdayObject.getString("Wednesday"));
                break;
                case 5: currentHours.append(weekdayObject.getString("Thursday"));
                break;
                case 6: currentHours.append(weekdayObject.getString("Friday"));
                break;
                case 7: currentHours.append(weekdayObject.getString("Saturday"));
                break;
            }*/

        } catch (JSONException e) {
            e.printStackTrace();
        }


        tv3.setText(c.getName());
        tv3.setTextSize(12);
        tv3.setTextColor(Color.BLACK);
        tv3.setGravity(Gravity.LEFT);
        tv3.setPadding(15, 0, 25, 0);
        tv4.setText(currentAddress.toString());
        tv4.setTextSize(12);
        tv4.setTextColor(Color.BLACK);
        tv4.setGravity(Gravity.LEFT);
        tv4.setPadding(15, 0, 25, 0);
        tv5.setText(currentPhone.toString());
        tv5.setTextSize(12);
        tv5.setTextColor(Color.BLACK);
        tv5.setGravity(Gravity.LEFT);
        tv5.setPadding(15, 0, 25, 0);
        tv6.setText(currentHours.toString());
        tv6.setTextSize(12);
        tv6.setTextColor(Color.BLACK);
        tv6.setGravity(Gravity.LEFT);
        tv6.setPadding(15, 0, 25, 0);
        tv7.setText("five stars");
        tv7.setTextSize(12);
        tv7.setTextColor(Color.BLACK);
        tv7.setGravity(Gravity.LEFT);
        tv7.setPadding(15, 0, 25, 0);
        deets.addView(tv3);
        deets.addView(tv4);
        deets.addView(tv5);
        deets.addView(tv6);
        deets.addView(tv7);
    }

    private void showScrollUI(boolean showScroll){
        findViewById(R.id.user_mode_switch).setVisibility(showScroll ? View.GONE:View.VISIBLE);
        findViewById(R.id.what_to_do_spinner).setVisibility(showScroll ? View.GONE:View.VISIBLE);
        findViewById(R.id.radius_group).setVisibility(showScroll ? View.GONE:View.VISIBLE);

        findViewById(R.id.place_scroll_view).setVisibility(showScroll ? View.VISIBLE:View.GONE);
        findViewById(R.id.back_to_radius_button).setVisibility(showScroll ? View.VISIBLE:View.GONE);


        //Need to make sure the place details go away whenever we go back to select a different radius
        findViewById(R.id.place_details_layout).setVisibility(View.GONE);

        //TODO: Dynamically change padding for the map to account for changes

        if(showScroll){
            mMap.setPadding(0,120,0,200);
        }else{mMap.setPadding(0,600,0,0);}
        fixZoomOverPath();
    }


    public void initializeSpinners(){
        /*Spinner spinner = (Spinner) findViewById(R.id.alone_or_friend_spinner);
            // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.alone_or_friend_array, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
        spinner.setAdapter(adapter);*/
        Spinner spinner = (Spinner) findViewById(R.id.what_to_do_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.what_to_do_array, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
    }


    private void fixZoomOverPath(){
        List<LatLng> points = fullPath.getPoints(); // route is instance of PolylineOptions
        LatLngBounds.Builder bc = new LatLngBounds.Builder();
        for (LatLng item : points) {
            bc.include(item);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50));
    }

    /*private void updateViewState(boolean showUI){
        if(!showUI){

            ImageButton arrowDown = (ImageButton) findViewById(R.id.show_ui_button);
            arrowDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPathState = false;
                    updateViewState(true);
                }
            });
        }
        findViewById(R.id.show_ui_button).setVisibility(showUI ? View.GONE:View.VISIBLE);
        findViewById(R.id.user_mode_switch).setVisibility(showUI ? View.VISIBLE:View.GONE);
        findViewById(R.id.what_to_do_spinner).setVisibility(showUI ? View.VISIBLE:View.GONE);
        findViewById(R.id.user_search_area).setVisibility(showUI ? View.VISIBLE:View.GONE);
        findViewById(R.id.radius_group).setVisibility(showUI ? View.VISIBLE : View.GONE);
    }*/

    private void findRadiusViewState(boolean radiusUI){
        findViewById(R.id.user_search_area).setVisibility(radiusUI ? View.GONE:View.VISIBLE);
        findViewById(R.id.radius_group).setVisibility(radiusUI ? View.VISIBLE:View.GONE);
        //LinearLayout mainContainer = (LinearLayout) findViewById(R.id.container);
        //int containerHeight = mainContainer.getHeight();
        if(radiusUI){

            mMap.setPadding(0,600,0,0);
            double mileDistance = routesToDestination.get(0).getDistance() / METERSINMILE;
            double minRadius = mileDistance/12.0;
            //Log.e("PATH DISTANCE", ""+routesToDestination.get(0).getDistance());
            double maxRadius;
            if(minRadius == 0){
                minRadius = .1;
                maxRadius = 2;
            }else{maxRadius = minRadius+5;}
            setSearchBounds(minRadius, maxRadius);
            setSliderReading();
        }else{mMap.setPadding(0,300,0,0);}
    }

    public void drawDirectionsPath(){

        if(directionsPathPoints != null && !directionsPathPoints.isEmpty()){
            PolylineOptions pathOptions = new PolylineOptions();
            Iterator<LatLng> i = directionsPathPoints.iterator();
            while (i.hasNext()){
                pathOptions.add(i.next());
            }
            pathOptions.color(Color.BLUE);
            fullPath = mMap.addPolyline(pathOptions);
            viewPathState = true;
            findRadiusViewState(true);
            //updateViewState(false);
            fixZoomOverPath();
        }else{showToast("An error has occurred and we cannot display directions");}
        /*if(routesToDestination != null && !routesToDestination.isEmpty()){
            DirectionsRoute bestRoute = routesToDestination.get(0);
            ArrayList<DirectionsLeg> legs = bestRoute.getLegs();
            PolylineOptions pathOptions = new PolylineOptions();
            Iterator<DirectionsLeg> i = legs.iterator();
            while (i.hasNext()){
                DirectionsLeg currentLeg = i.next();
                ArrayList<DirectionsStep> steps = currentLeg.getSteps();
                Iterator<DirectionsStep> j = steps.iterator();
                DirectionsStep firstStep = j.next();
                pathOptions.add(firstStep.getStartLocation());
                pathOptions.add(firstStep.getEndLocation());
                while(j.hasNext()){
                    pathOptions.add(j.next().getEndLocation());
                }
            }
            Polyline polyline = mMap.addPolyline(pathOptions);
        }else{showToast("An error has occurred and we cannot display directions");}*/
    }

    /**
     * Method to decode polyline points
     * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


    public void objectifyDirections(JSONObject object){
        if(routesToDestination == null){
            routesToDestination = new ArrayList<DirectionsRoute>();
        }else{routesToDestination.clear();}
        JSONArray routes = null;
        Log.e("object_to_objectify", object.toString());
        try {
            routes = object.getJSONArray("routes");
            JSONObject singleRoute = (JSONObject) routes.get(0);
            JSONObject overviewPoly = singleRoute.getJSONObject("overview_polyline");
            String encodedStr = (String) overviewPoly.get("points");
            directionsPathPoints = decodePoly(encodedStr);
            JSONArray legs = null;
            JSONArray steps = null;
            JSONObject tmp = null;
            for (int i = 0; i < routes.length(); i++) {
                Log.e("ObjNum_routes", routes.length()+ "");
                tmp = (JSONObject) routes.get(i);
                legs = tmp.getJSONArray("legs");
                DirectionsRoute mRoute = new DirectionsRoute();
                for (int j = 0; j < legs.length(); j++) {
                    Log.e("ObjNum_legs", legs.length()+ "");
                    tmp = (JSONObject) legs.get(j);
                    steps = tmp.getJSONArray("steps");
                    DirectionsLeg mLeg = new DirectionsLeg();
                    for (int k = 0; k < steps.length(); k++) {
                        Log.e("ObjNum_steps", steps.length()+ "");
                        JSONObject currentStep = (JSONObject) steps.get(k);
                        JSONObject jsonDist = (JSONObject) currentStep.get("distance");
                        JSONObject jsonStart = (JSONObject) currentStep.get("start_location");
                        JSONObject jsonEnd = (JSONObject) currentStep.get("end_location");
                        int distance = Integer.parseInt(jsonDist.getString("value"));
                        double tmpLat = (double) jsonStart.get("lat");
                        double tmpLng = (double) jsonStart.get("lng");
                        LatLng start = new LatLng(tmpLat, tmpLng);
                        tmpLat = (double) jsonEnd.get("lat");
                        tmpLng = (double) jsonEnd.get("lng");
                        LatLng end = new LatLng(tmpLat, tmpLng);
                        mLeg.addStep(new DirectionsStep(start, end, distance));
                        Log.e("StepAdded", mLeg.getSteps().get(k).toString());
                    }
                    mRoute.addLeg(mLeg);
                }
                routesToDestination.add(mRoute);
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e("PathParseError", e.toString());
        }
        drawDirectionsPath();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
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
    }


    private void showFindNearbyDialog(){
        showToast("Uh Oh... It looks like we can't find your location. Please make sure location services are enabled");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_enter_nearby_address, null))
                .setTitle(getString(R.string.enter_nearby_address_dialog_title))
                .setNeutralButton(R.id.nearby_address_search_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText edit = (EditText) findViewById(R.id.nearby_address_edit);
                        if (edit.getText() != null) {
                            noGPSCurrentLocation();
                        } else {
                            showToast("Please enter a nearby address first");
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        final AutoCompleteTextView nearbyLocationEdit = (AutoCompleteTextView) dialog.findViewById(R.id.nearby_address_edit);
        //nearbyLocationEdit.addTextChangedListener(this);
        //nearbyLocationEdit.setOnItemSelectedListener(this);
        //nearbyLocationEdit.setThreshold(THRESHOLD);
        Button nearbyLocationSearch = (Button) dialog.findViewById(nearby_address_search_btn);
        dialog.show();
    }


    private long getGPSCheckMilliSecsFromPrefs(){
        return System.currentTimeMillis() - 100000000;
    }

    /**
     * try to get the 'best' location selected from all providers
     */
    private Location getBestLocation() {
        Location gpslocation = getLocationByProvider(LocationManager.GPS_PROVIDER);
        Location networkLocation =
                getLocationByProvider(LocationManager.NETWORK_PROVIDER);
        // if we have only one location available, the choice is easy
        if (gpslocation == null) {
            Log.d("TAG", "No GPS Location available.");
            return networkLocation;
        }
        if (networkLocation == null) {
            Log.d("TAG", "No Network Location available");
            return gpslocation;
        }
        // a locationupdate is considered 'old' if its older than the configured
        // update interval. this means, we didn't get a
        // update from this provider since the last check
        long old = System.currentTimeMillis() - getGPSCheckMilliSecsFromPrefs();
        boolean gpsIsOld = (gpslocation.getTime() < old);
        boolean networkIsOld = (networkLocation.getTime() < old);
        // gps is current and available, gps is better than network
        if (!gpsIsOld) {
            Log.d("TAG", "Returning current GPS Location");
            return gpslocation;
        }
        // gps is old, we can't trust it. use network location
        if (!networkIsOld) {
            Log.d("TAG", "GPS is old, Network is current, returning network");
            return networkLocation;
        }
        // both are old return the newer of those two
        if (gpslocation.getTime() > networkLocation.getTime()) {
            Log.d("TAG", "Both are old, returning gps(newer)");
            return gpslocation;
        } else {
            Log.d("TAG", "Both are old, returning network(newer)");
            return networkLocation;
        }
    }

    /**
     * get the last known location from a specific provider (network/gps)
     */
    private Location getLocationByProvider(String provider) {
        Location location = null;
        LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(provider)) {
            return null;
        }

        try {
            if (locationManager.isProviderEnabled(provider)) {
                location = locationManager.getLastKnownLocation(provider);
            }
        } catch (IllegalArgumentException e) {
            Log.d("TAG", "Cannot access Provider " + provider);
        }
        return location;
    }


    private void gatherGemData(final Gem gem, final Marker marker){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_enter_gem_data, null))
                .setTitle(getString(R.string.gem_dialog_title))
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: Need to add implementation for adding gem to back end
                        Dialog d = (Dialog) dialog;
                        EditText gemTitleEdit = (EditText) d.findViewById(R.id.gem_title_text);
                        EditText gemDescriptionEdit = (EditText) d.findViewById(R.id.gem_description_text);
                        String gemTitle = gemTitleEdit.getText().toString().trim();
                        String gemDescription = gemDescriptionEdit.getText().toString().trim();
                        if (!gemTitle.equals(null) && !gemDescription.equals(null) && !gemTitle.equals("") && !gemDescription.equals("")) {
                            gem.setTitle(gemTitle);
                            gem.setDescription(gemDescription);
                            SubmitGemTask subGem = new SubmitGemTask(getApplicationContext());
                            subGem.execute(gem);
                            subGem.setMyTaskCompleteListener(new SubmitGemTask.OnTaskComplete() {

                                @Override
                                public void setMyTaskComplete(String JsonMsg) {
                                    JSONObject rootOfResult = null;
                                    String status = "";
                                    String result = "";
                                    try{
                                        rootOfResult = new JSONObject(JsonMsg);
                                        status = (String) rootOfResult.get("code");
                                        result = (String) rootOfResult.get("result");
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                    if(status.equals("success")){
                                        showToast("Gem added successfully!");
                                    }else{
                                        showToast("Uh Oh... An error has occurred. We are unable to add your gem right now.");
                                    }
                                }
                            });
                            Log.e("GEM_TITLE", gemTitle);
                            Log.e("GEM_DESCRIPTION", gemDescription);
                            marker.setTitle(gem.getTitle());
                            marker.setSnippet(gem.getDescription());
                            dialog.dismiss();
                        } else {
                            showToast("You must add a title and description to save your gem");
                            marker.remove();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        marker.remove();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        LinearLayout mainContainer = (LinearLayout) findViewById(R.id.container);
        int containerHeight = mainContainer.getHeight();
        mMap.setPadding(0, containerHeight+280, 0 ,0);          //Allow 300 dps initial padding to still use "My Location" button
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        noGPSFlag = false;
        Location location = getBestLocation();
        LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 13));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.v("Tap Coordinates", latLng.latitude + " " + latLng.longitude);
                if(placeGem){
                    Marker userMark = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("User Placed Gem"));
                    Gem placedGem = new Gem(userMark.getPosition(), user);
                    placeGem = false;
                    gatherGemData(placedGem, userMark);
                }else{return;}
            }
        });
    }

    private void selectItem(int pos){
        Log.e("NavTableSelect", "" + pos);
        switch (pos){
            case 0:                 // Add Gem
                Log.e("NavTable", "Attempt to add Gem to map");
                mDrawerLayout.closeDrawer(mDrawerFrame);
                placeGem = true;
                showToast("Tap on the map where you would like to place a Gem!");
                break;
            case 1:                 // Sign Out
                Log.e("NavTable", "Sign Out attempted");
                Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                logoutIntent.putExtra("fromMain",true);
                startActivity(logoutIntent);
                break;
            case 2:                 // Settings
                Log.e("NavTable", "Settings view attempted");
                showToast("Settings feature not yet ready");
                break;
            default:
                break;
        }
    }



    public void showToast(String msg){
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String value = s.toString();
        if (!"".equals(value) && value.length() >= THRESHOLD) {
            new GeocodeTask().execute(s.toString());
            Log.e("Text Changed", s.toString());
        } else if(autoCompleteAdapter != null){
            autoCompleteAdapter.clear();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }


    public String getFormattedAddress(Address address){
        String addr = "";
        int numLines = address.getMaxAddressLineIndex();
        for(int j = 0; j < numLines; j++){
            addr += address.getAddressLine(j);
            if(j != numLines - 1){
                addr += ", ";
            }
        }
        return addr;
    }


    public void noGPSCurrentLocation(){
        EditText search = (EditText) findViewById(R.id.nearby_address_edit);
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
        String address = search.getText().toString();
        Address realAddr;
        try {
            realAddr = new Geocoder(mainCon).getFromLocationName(address, 1).get(0);
            destination = realAddr;
            LatLng loc = new LatLng(realAddr.getLatitude(), realAddr.getLongitude());
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
            mMap.clear();
            userMarker = mMap.addMarker(new MarkerOptions()
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .snippet(address)
                    .position(loc));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToAddress(View view){
        EditText search = (EditText) findViewById(R.id.geo_search_edit);
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
        String address = search.getText().toString();
        if(address.equals(null) || address.equals("")){
            showToast("Please enter an address first!");
            return;
        }
        Address realAddr;
        try {
            realAddr = new Geocoder(mainCon).getFromLocationName(address, 1).get(0);
            destination = realAddr;
            Log.e("RETURNED ADDRESS", ""+destination.toString());
            LatLng loc = new LatLng(realAddr.getLatitude(), realAddr.getLongitude());
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
            mMap.clear();
            if(userMarker != null){
                mMap.addMarker(new MarkerOptions()
                    .title(userMarker.getTitle())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .snippet(userMarker.getSnippet())
                    .position(userMarker.getPosition()));
            }
            destMarker = mMap.addMarker(new MarkerOptions()
                    .title("Your Destination")
                    .snippet(address)
                    .position(loc));
            new GetDirectionsTask().execute(loc);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private class GeocodeTask extends AsyncTask<String, Void, List<Address>>{

        @Override
        protected List<Address> doInBackground(String... params) {
            try {
                autoCompleteSuggestionAddresses = new Geocoder(mainCon).getFromLocationName(params[0], 10);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Address> addresses){
            synchronized (this){
                if(autoCompleteSuggestionAddresses != null){
                    ArrayList<String> addrStr = new ArrayList<String>();
                    for(int i = 0; i < autoCompleteSuggestionAddresses.size(); i++){
                        Address address = autoCompleteSuggestionAddresses.get(i);
                        String addr = getFormattedAddress(address);
                        addrStr.add(addr);
                    }
                    autoCompleteAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_dropdown_item_1line, addrStr);
                    locationInput.setAdapter(autoCompleteAdapter);
                }
            }
        }
    }


    /**
     * AsyncTask meant to find directions from current location to LatLng offered as parameter
     */
    private class GetDirectionsTask extends AsyncTask<LatLng, Void, String>{

        private String apiKey;
        private LatLng userLoc;

        @Override
        protected void onPreExecute(){
            apiKey = getString(R.string.google_maps_key);
            Location location = getBestLocation();
            userLoc = new LatLng(location.getLatitude(), location.getLongitude());
        }

        @Override
        protected String doInBackground(LatLng... params) {
            String baseUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=";
            if(userMarker != null){
                baseUrl += userMarker.getPosition().latitude + "," + userMarker.getPosition().longitude + "&destination=" + params[0].latitude + "," + params[0].longitude + "&sensor=false";
            }else{baseUrl += userLoc.latitude + "," + userLoc.longitude + "&destination=" + params[0].latitude + "," + params[0].longitude + "&sensor=false";}
            try{
                URL url = new URL(baseUrl);
                Log.v("Query", url.toString());
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(15*1000);
                conn.connect();
                BufferedReader read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String ln = null;
                StringBuilder builder = new StringBuilder();
                while((ln = read.readLine()) != null){
                    builder.append(ln);
                }
                Log.e("DirectionsMessage",conn.getResponseMessage());
                Log.e("DirectionsCode", ""+conn.getResponseCode());
                conn.disconnect();
                return builder.toString();
            }catch (Exception e){
                e.printStackTrace();
                Log.e("ERROR", ""+e.getMessage());
                Log.e("ERROR", "" + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result){
            JSONObject rootOfResult = null;
            String status = "";
            Log.e("DIRECTIONS_RESULT", "" + result);
            try{
                rootOfResult = new JSONObject(result);
                status = (String) rootOfResult.get("status");
            }catch(Exception e){
                e.printStackTrace();
            }
            if(status.equals("OK")){
                objectifyDirections(rootOfResult);
            }else{
                showToast("An error has occurred. Unable to find directions because: " + status);
            }
        }
    }



}



