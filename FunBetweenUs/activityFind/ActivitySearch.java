package com.funbetweenus.funbetweenus.activityFind;

//import com.google.android.gms.common.api.Result;
//import com.googleplaces.query.NearbySearchQuery;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Cassandra on 4/12/2015.
 */
public class ActivitySearch implements ActivitySearchInterface{

    private String apiKey; // = "AIzaSyAb8cOeH98byFxIRQDhKk0vnFpNCIxjaeE";
    private RequestHandler requestHandler;

    public ActivitySearch(String apiKey, RequestHandler requestHandler){
        this.apiKey = apiKey;
        this.requestHandler = requestHandler;
    }

    /**
     * Creates a new ActivitySearch object using the specificed API key registered on the
     * Google Developer Console
     * @param apiKey
     */

    public ActivitySearch(String apiKey){
        this(apiKey, new DefaultRequestHandler());
    }

    /**
     * Creates the url used to search for activities within a certain range and type
     * @param params
     * @param extraParams
     * @return
     */
    private static String url(String params, Param...  extraParams){
        String url = String.format(Locale.ENGLISH, "%s%s/json?%s", API_URL, "nearbysearch", params);
        //url = addExtraParams(url, extraParams);
        url = url.replace(' ', '+');
        return url;
    }

    /**
     * Parses the json String returned into a list of places
     *
     * @param place to parse into
     * @param str raw json
     * @param max the maximum amount of places to return
     * @return list of parsed places
     */

    public static String parse(ActivitySearch client, List<Place> place, String str, int max,
           String category) {

        JSONObject json = new JSONObject();
        try {
            //parse the json
            json = new JSONObject(str);

            String statusCode = json.getString(STRING_STATUS);
            //checkStatus(statusCode, json.optString(STRING_ERROR_MESSAGE));

        if (statusCode.equals("zero_results")) {
            return null;
        }

        JSONArray results = json.getJSONArray("results");
        parseResults(client, place, results, max, category);
        } catch (JSONException e){
            //debug
        }

        return json.optString(STRING_NEXT_PAGE_TOKEN, null);
    }

    private static void parseResults(ActivitySearch client, List<Place> places, JSONArray results,
        int max, String category) throws JSONException {
        double lat = -1;
        double lon = -1;
        String placeId = null;
        String iconUrl = null;
        String placeName = null;
        String address = null;

        max = Math.min(max, MAXIMUM_PAGE_RESULTS);
        JSONObject result = new JSONObject();
        try {
            for (int i = 0; i < max; i++) {
                if (i >= results.length()) {
                    return;
                }
                result = results.getJSONObject(i);


                JSONObject location = result.getJSONObject(OBJECT_GEOMETRY).getJSONObject(OBJECT_LOCATION);
                lat = location.getDouble(DOUBLE_LATITUDE);
                lon = location.getDouble(DOUBLE_LONGITUDE);

                placeId = result.getString(STRING_PLACE_ID);
                iconUrl = result.optString(STRING_ICON, null);
                placeName = result.optString(STRING_NAME);
                address = result.optString(STRING_ADDRESS, null);
            }
        }catch (JSONException e){
                //debug
            }

            // Limit it to user's chosen category
            List<String> types = new ArrayList<>();
            JSONArray jsonTypes = result.optJSONArray("types");
            if (jsonTypes != null) {
                for (int a = 0; a < jsonTypes.length(); a++){
                    if( jsonTypes.getString(a).equals(category));
                    types.add(jsonTypes.getString(a));}
            }

            Place place = new Place();

            places.add(place.setClient(client).setPlaceId(placeId).setName(placeName).setLatitude(lat).setLongitude(lon)
                    .setIconUrl(iconUrl).setAddress(address).addTypes(types).setJson(result));
        }

    public List<Place> getNearbyPlaces (double lat, double lon, double radius, String category){
           List<Place> foundPlaces = null;
        int max = 5;
        if (category.equals("Gems")){
            //do special look for Gems
            return foundPlaces;
        } else {
            String activityUrl = url(String.format(Locale.ENGLISH,
                    "key=%s&location=%f,%f&radius=%f", apiKey, lat, lon, radius));
            try {
                foundPlaces = getPlaces(activityUrl, max, category);
            } catch (Exception e) {
                //debug
            }
            return foundPlaces;
        }
    }

    private List<Place> getPlaces(String activityUrl, int max, String category) throws IOException {
        max = Math.min(max, MAXIMUM_RESULTS);
        int pages = (int) Math.ceil(max/(double) MAXIMUM_PAGE_RESULTS);

        List<Place> places = new ArrayList<>();

        for (int i=0; i<pages; i++){
            //debug("Page: " + (i + 1));
            String raw = requestHandler.get(activityUrl);
            //debug(raw);
            String nextPage = parse(this, places, raw, max, category);
            if (nextPage != null) {
                max -= MAXIMUM_PAGE_RESULTS;
                activityUrl = String.format("%s%s/json?pagetoken=%s&key=%s",
                        API_URL, "nearbysearch", nextPage, apiKey);
                //sleep(3000); // Page tokens have a delay before they are available
            } else {
                break;
            }
        }

        for (Place p : places)
            System.out.println("Place: " + places.get(0));
        return places;
    }
/*
    protected static void checkStatus(String statusCode, String errorMessage) {
        //GooglePlacesException e = GooglePlacesException.parse(statusCode, errorMessage);
        Exception e = (String) errorMessage;
        if (e != null)
            throw e;
    }*/
}
