package com.funbetweenus.funbetweenus.activityFind;

/**
 * Created by Cassandra on 4/14/2015.
 */
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

//import static com.funbetweenus.funbetweenus.activityFind.ActivitySearch;

/**
 * Represents a place returned by Google Places API_
 */
public class Place implements ActivitySearchInterface{
    private final List<String> types = new ArrayList<>();

    private ActivitySearch client;
    private String placeId;
    private double lat = -1, lng = -1;
    private JSONObject json;
    private String iconUrl;
    private InputStream icon;
    private String name;
    private String addr;
    private String lang;

    protected Place() {
    }

    /**
     * Parses a detailed Place object.
     *
     * @param client  api client
     * @param rawJson json to parse
     * @return a detailed place
     */
    public static Place parseDetails(ActivitySearch client, String rawJson) throws JSONException {
       JSONObject result = new JSONObject();
            JSONObject json = new JSONObject(rawJson);

            result = json.getJSONObject(OBJECT_RESULT);

            // easy stuff
            String name = result.getString(STRING_NAME);
            String id = result.getString(STRING_PLACE_ID);
            String address = result.optString(STRING_ADDRESS, null);
            String iconUrl = result.optString(STRING_ICON, null);

            // location
            JSONObject location = result.getJSONObject(OBJECT_GEOMETRY).getJSONObject(OBJECT_LOCATION);
            double lat = location.getDouble(DOUBLE_LATITUDE), lng = location.getDouble(DOUBLE_LONGITUDE);

            //Debug
        Place place = new Place();

        // types
        JSONArray jsonTypes = result.optJSONArray(ARRAY_TYPES);
        List<String> types = new ArrayList<>();
        try {
            if (jsonTypes != null) {
                for (int i = 0; i < jsonTypes.length(); i++) {
                    types.add(jsonTypes.getString(i));
                }
            }
        }catch (JSONException e){
                //debug
            }

        return place.setPlaceId(id).setClient(client).setName(name).setAddress(address).setIconUrl(iconUrl)
                .setLatitude(lat).setLongitude(lng).addTypes(types).setJson(result);
    }

    /**
     * Returns the client associated with this Place object.
     *
     * @return client
     */
    public ActivitySearch getClient() {
        return client;
    }

    /**
     * Sets the {@link } client associated with this Place object.
     *
     * @param client to set
     * @return this
     */
    protected Place setClient(ActivitySearch client) {
        this.client = client;
        return this;
    }

    /**
     * Returns the unique identifier for this place.
     *
     * @return id
     */
    public String getPlaceId() {
        return placeId;
    }

    /**
     * Sets the unique, stable, identifier for this place.
     *
     * @param placeId to use
     * @return this
     */
    protected Place setPlaceId(String placeId) {
        this.placeId = placeId;
        return this;
    }

    /**
     * Returns the latitude of the place.
     *
     * @return place latitude
     */
    public double getLatitude() {
        return lat;
    }

    /**
     * Sets the latitude of the place.
     *
     * @param lat latitude
     * @return this
     */
    protected Place setLatitude(double lat) {
        this.lat = lat;
        return this;
    }

    /**
     * Returns the longitude of this place.
     *
     * @return longitude
     */
    public double getLongitude() {
        return lng;
    }

    /**
     * Sets the longitude of this place.
     *
     * @param lon longitude
     * @return this
     */
    protected Place setLongitude(double lon) {
        this.lng = lon;
        return this;
    }
    /**
     * Returns the url of the icon to represent this place.
     *
     * @return icon to represent
     */
    public String getIconUrl() {
        return iconUrl;
    }

    /**
     * Sets the url of the icon to represent this place.
     *
     * @param iconUrl to represent place.
     * @return this
     */
    protected Place setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    /**
     * Returns the name of this place.
     *
     * @return name of place
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this place.
     *
     * @param name of place
     * @return this
     */
    protected Place setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the address of this place.
     *
     * @return address of this place
     */
    public String getAddress() {
        return addr;
    }

    /**
     * Sets the address of this place.
     *
     * @param addr address
     * @return this
     */
    protected Place setAddress(String addr) {
        this.addr = addr;
        return this;
    }

    /**
     * Adds a collection of string "types".
     *
     * @param types to add
     * @return this
     */
    protected Place addTypes(Collection<String> types) {
        this.types.addAll(types);
        return this;
    }

    /**
     * Returns all of this place's types in an unmodifiable list.
     *
     * @return types
     */
    public List<String> getTypes() {
        return Collections.unmodifiableList(types);
    }

    /**
     * Returns the JSON representation of this place. This does not build a JSON object, it only returns the JSON
     * that was given in the initial response from the server.
     *
     * @return the json representation
     */
    public JSONObject getJson() {
        return json;
    }

    /**
     * Sets the JSON representation of this Place.
     *
     * @param json representation
     * @return this
     */
    protected Place setJson(JSONObject json) {
        this.json = json;
        return this;
    }


    /**
     * Returns the language of the place.
     *
     * @return language
     */
    public String getLanguage() {
        return lang;
    }

    /**
     * Sets the language of the location.
     *
     * @param lang place language
     * @return this
     */
    protected Place setLanguage(String lang) {
        this.lang = lang;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Place{id=%s}", placeId);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Place && ((Place) obj).placeId.equals(placeId);
    }
}