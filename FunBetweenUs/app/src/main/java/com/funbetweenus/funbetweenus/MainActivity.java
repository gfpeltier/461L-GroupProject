package com.funbetweenus.funbetweenus;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private User user;
    protected GoogleMap mMap;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private FrameLayout mDrawerFrame;

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

    }


    public void initializeSpinners(){
        Spinner spinner = (Spinner) findViewById(R.id.alone_or_friend_spinner);
            // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.alone_or_friend_array, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner = (Spinner) findViewById(R.id.what_to_do_spinner);
        adapter = ArrayAdapter.createFromResource(this,
                R.array.what_to_do_array, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
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



    private class DrawerItemClickListener implements ListView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}



