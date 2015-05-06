package com.funbetweenus.funbetweenus.activityFind;

/**
 * Created by Cassandra on 4/14/2015.
 */

public interface ActivitySearchInterface {
    public static final String API_URL = "https://maps.googleapis.com/maps/api/place";
    public static final int MAXIMUM_PAGE_RESULTS = 3;
    public static final int MAXIMUM_RESULTS = 10;
    public static final String DOUBLE_LATITUDE = "lat";
    public static final String DOUBLE_LONGITUDE = "lng";
    public static final String STRING_PLACE_ID = "place_id";
    public static final String STRING_ICON = "icon";
    public static final String STRING_NAME = "name";
    public static final String STRING_ADDRESS = "formatted_address";
    public static final String STRING_ERROR_MESSAGE = "error_message";
    public static final String STRING_STATUS = "status";
    public static final String STRING_NEXT_PAGE_TOKEN = "next_page_token";
    public static final String OBJECT_GEOMETRY = "geometry";
    public static final String OBJECT_LOCATION = "location";
    public static final String OBJECT_RESULT = "result";
    public static final String ARRAY_TYPES = "types";



}
