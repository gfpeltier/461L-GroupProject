/**
 * Created by grantpeltier93 on 4/14/15.
 */


import android.util.Log;

import com.funbetweenus.funbetweenus.data.DirectionsLeg;
import com.funbetweenus.funbetweenus.data.DirectionsRoute;
import com.funbetweenus.funbetweenus.data.DirectionsStep;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.junit.Assert.*;

import java.util.ArrayList;


public class MainMethodTest {

    private String directionsJson;

    private ArrayList<DirectionsRoute> dirRoutes;

    @Before
    public void initialize(){
        directionsJson = "{\"status\":\"OK\",\"routes\":[{\"legs\":[{\"steps\":[{\"start_location\": {" +
                "          \"lat\": 41.8507300," +
                "          \"lng\": -87.6512600" +
                "        }," +
                "        \"end_location\": {" +
                "          \"lat\": 41.8525800," +
                "          \"lng\": -87.6514100" +
                "        }," +
                "        \"polyline\": {" +
                "          \"points\": \"a~l~Fjk~uOwHJy@P\"" +
                "        }," +
                "        \"duration\": {" +
                "          \"value\": 19," +
                "          \"text\": \"1 min\"" +
                "        }," +
                "        \"html_instructions\": \"Head \\u003cb\\u003enorth\\u003c/b\\u003e on \\u003cb\\u003eS Morgan St\\u003c/b\\u003e toward \\u003cb\\u003eW Cermak Rd\\u003c/b\\u003e\"," +
                "        \"distance\": {" +
                "          \"value\": 207," +
                "          \"text\": \"0.1 mi\"" +
                "        }}]}]}]}";
    }

    @Test
    public void testSingleStep(){
        JSONObject rootOfResult = null;
        String status = "";
        System.out.println(directionsJson);
        try{
            rootOfResult = new JSONObject(directionsJson);
            status = (String) rootOfResult.getString("status");
            System.out.println(status);
        }catch(Exception e){
            e.printStackTrace();
        }
        if(status != null && status.equals("OK")){
            if(dirRoutes == null){
                dirRoutes = new ArrayList<DirectionsRoute>();
            }else{dirRoutes.clear();}
            JSONArray routes = null;
            try {
                routes = rootOfResult.getJSONArray("routes");
                JSONObject singleRoute = (JSONObject) routes.get(0);
                JSONObject overviewPoly = singleRoute.getJSONObject("overview_polyline");
                String encodedStr = (String) overviewPoly.get("points");
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
                    dirRoutes.add(mRoute);
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.e("PathParseError", e.toString());
            }
            DirectionsRoute checkRoute = new DirectionsRoute();
            DirectionsLeg checkLeg = new DirectionsLeg();
            DirectionsStep checkStep = new DirectionsStep(new LatLng(41.8507300, -87.6512600), new LatLng(41.8525800, -87.6514100), 207);
            checkLeg.addStep(checkStep);
            checkRoute.addLeg(checkLeg);
            Assert.assertTrue(dirRoutes.get(0).equals(checkRoute));
        }else{
            Assert.fail();
        }
    }

}
