package com.funbetweenus.funbetweenus;

import android.content.Context;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.funbetweenus.funbetweenus.data.Directions;
import com.funbetweenus.funbetweenus.data.DirectionsLeg;
import com.funbetweenus.funbetweenus.data.DirectionsRoute;
import com.funbetweenus.funbetweenus.data.DirectionsStep;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, TextWatcher, AdapterView.OnItemSelectedListener {

    private User user;
    protected GoogleMap mMap;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private FrameLayout mDrawerFrame;

    private static final int MESSAGE_TEXT_CHANGED = 0;
    private static final int AUTOCOMPLETE_DELAY = 500;
    private static final int THRESHOLD = 3;

    private List<Address> autoCompleteSuggestionAddresses;
    private ArrayAdapter<String> autoCompleteAdapter;
    private AutoCompleteTextView locationInput;
    private Address destination;
    private Marker destMarker;

    private ArrayList<DirectionsRoute> routesToDestination;
    private List<LatLng> directionsPathPoints;
    private Polyline fullPath;

    private Context mainCon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initializeSpinners();

        String greet = "Hello, ";
        try {
            user = getIntent().getExtras().getParcelable("currentUser");
            Log.i("CURRENT USER NAME", user.getName());
            greet += user.getName();
            //setTitle(greet);
        }catch (Exception e){
            e.printStackTrace();
        }
        ImageButton img = (ImageButton) findViewById(R.id.menu_button);
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

        mainCon = getBaseContext();
        locationInput = (AutoCompleteTextView) findViewById(R.id.geo_search_edit);
        locationInput.addTextChangedListener(this);
        locationInput.setOnItemSelectedListener(this);
        locationInput.setThreshold(THRESHOLD);

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

    public void drawDirectionsPath(){


        if(directionsPathPoints != null && !directionsPathPoints.isEmpty()){
            PolylineOptions pathOptions = new PolylineOptions();
            Iterator<LatLng> i = directionsPathPoints.iterator();
            while (i.hasNext()){
                pathOptions.add(i.next());
            }
            pathOptions.color(Color.BLUE);
            fullPath = mMap.addPolyline(pathOptions);
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


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 13));
    }

    private void selectItem(int pos){
        Log.e("NavTableSelect", "" + pos);
        switch (pos){
            case 0:                 // Sign Out
                Log.e("NavTable", "Sign Out attempted");
                Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                logoutIntent.putExtra("fromMain",true);
                startActivity(logoutIntent);
                break;
            case 1:                 // Settings
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


    public void goToAddress(View view){
        EditText search = (EditText) findViewById(R.id.geo_search_edit);
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
        String address = search.getText().toString();
        Address realAddr;
        try {
            realAddr = new Geocoder(mainCon).getFromLocationName(address, 1).get(0);
            destination = realAddr;
            LatLng loc = new LatLng(realAddr.getLatitude(), realAddr.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
            mMap.clear();
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


    /**
     * AsyncTask meant to find directions from current location to LatLng offered as parameter
     */
    private class GetDirectionsTask extends AsyncTask<LatLng, Void, String>{

        private String apiKey;
        private LatLng userLoc;

        @Override
        protected void onPreExecute(){
            apiKey = getString(R.string.google_maps_key);
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            userLoc = new LatLng(location.getLatitude(), location.getLongitude());
        }

        @Override
        protected String doInBackground(LatLng... params) {
            String baseUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=";
            baseUrl += userLoc.latitude + "," + userLoc.longitude + "&destination=" + params[0].latitude + "," + params[0].longitude + "&sensor=false";
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



