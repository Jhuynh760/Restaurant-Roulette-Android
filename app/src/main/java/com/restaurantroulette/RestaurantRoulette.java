package com.restaurantroulette;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

class RestaurantRoulette{
    private DatabaseReference rollHistoryRef;

    private int numNearbyRestaurants;
    private int numVisited;

    private final String API_KEY;

    //google geocode api information
    private final String GE_BASE_LINK;

    private String longitude; //reverse Geocode result
    private String latitude;  //reverse Geocode result
    //google geocode api information

    //google places api information
    private final String GP_BASE_SEARCH_LINK;
    private final String GP_BASE_DETAILS_LINK;
    //google places api information

    //settings
    private double travelRadius; //will be the result of miles converted into meters.
    private String address;
    //settings


    private HashMap<String, String> restaurantList;
    //random store vairables
    private HashMap<String, String> randRestaurantInfo;

    // Constructor
    public RestaurantRoulette(DatabaseReference rollHistoryRef){
        this.rollHistoryRef = rollHistoryRef;

        this.numNearbyRestaurants = 0;
        this.numVisited = 0;

        this.API_KEY = "<INSERT API KEY>";

        //google places api information
        this.GP_BASE_SEARCH_LINK = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        this.GP_BASE_DETAILS_LINK = "https://maps.googleapis.com/maps/api/place/details/json?";
        //google places api information

        //google geocode api information
        this.GE_BASE_LINK = "https://maps.googleapis.com/maps/api/geocode/json?";
        //google geocode api information

        this.longitude = null;
        this.latitude = null;

        this.travelRadius = 10 * 1609.344;
        this.address = "";
        this.randRestaurantInfo = null;

        this.restaurantList = new HashMap<>();
    }
    // Constructor

    // PUBLIC METHOD CALLS
    public void clearRollHistoryRef(){
        this.rollHistoryRef.setValue(null);
        Log.d("RESTAURANTROULETTELOG","Cleared rollHistoryRef");
        this.numVisited = 0;
    }

    public void setAddress(String address){
        this.address = address;
        Log.d("RESTAURANTROULETTELOG","Set Address = " + address);
    }

    public void setTravelRadius(int radius){
        double newRadius = radius * 1609.344;
        this.travelRadius = newRadius;
        Log.d("RESTAURANTROULETTELOG","Set Travel Radius = " + newRadius);
    }

    public HashMap<String, String> getRandRestaurantInfo(){
        return this.randRestaurantInfo;
    }

    public void main(){
        this.parseGEResponse(getJsonGE());
        this.parseGPSearchResponse(getGPSearchJson());
        this.getRandomRestaurantInfo();
        Log.d("RESTAURANTROULETTELOG","Successfully rand Main()");
    }
    // PUBLIC METHOD CALLS

    // PRIVATE METHOD CALLS
    private String getLatitude(){
        return this.latitude;
    }
    private String getLongitude(){
        return this.longitude;
    }
    private void setLatitude(String latitude){
        this.latitude = latitude;
        Log.d("RESTAURANTROULETTELOG","Set Latitude = " + latitude);
    }
    private void setLongitude(String longitude){
        this.longitude = longitude;
        Log.d("RESTAURANTROULETTELOG","Set Longitude = " + longitude);
    }

    private void pushToRestaurantList(String key, String restaurantID){
        this.restaurantList.put(key, restaurantID);
        this.numNearbyRestaurants++;
        Log.d("RESTAURANTROULETTELOG", "PUSHED TO RestaurantList: KEY = " + key + " VALUE = "+ restaurantID);
    }

    private void clearRestaurantList(){
        this.numNearbyRestaurants = 0;
        this.restaurantList.clear();
    }


    private void pushToRollHistoryRef(String key, Object obj){
        this.rollHistoryRef.child(key).setValue(obj);
        this.numVisited++;
        Log.d("RESTAURANTROULETTELOG", "PUSHED TO VISITED DB: KEY = " + key + " VALUE = "+ obj);
    }

    //GE FUNCTIONS
    private String getJsonGE (){
        URL url;
        HttpsURLConnection connection;
    	try {
    	    String urlStr = this.GE_BASE_LINK + "address=" + URLEncoder.encode(this.address, "UTF-8") + "&key=" + this.API_KEY;
    	    Log.d("RESTAURANTROULETTELOG", "GE URL: "  + urlStr);
        	url = new URL(urlStr);
        	connection = (HttpsURLConnection) url.openConnection();
        	connection.setRequestMethod("GET");
        	connection.connect();
        	
        	int httpStatus = connection.getResponseCode();
        	System.out.println(httpStatus);
        	if (httpStatus == 201 || httpStatus == 200) {
        		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        		StringBuilder sb = new StringBuilder();
        		String line;
        		while ((line = br.readLine()) != null) {
        			sb.append(line + "\n");
        		}
        		br.close();
                Log.d("RESTAURANTROULETTELOG", "Successfully got GE Json");
        		return sb.toString();
        	}
        	
    	}
    	catch (MalformedURLException e) {
    		e.printStackTrace();
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    	}
        Log.d("RESTAURANTROULETTELOG", "Failed to get GE Json");
    	return null;
    }
    
    private void parseGEResponse(String httpsResponse) {
        try{
            JSONObject json = new JSONObject(httpsResponse);
            JSONArray results = json.getJSONArray("results");
            json = results.getJSONObject(0);
            json = json.getJSONObject("geometry");
            json = json.getJSONObject("location");
            this.setLatitude(Double.toString(json.getDouble("lat")));
            this.setLongitude(Double.toString(json.getDouble("lng")));
            Log.d("RESTAURANTROULETTELOG", "Successfully Parsed GE Json");
        }
        catch( org.json.JSONException e){
            Log.d("RESTAURANTROULETTELOG", "Failed to Parse GE Json");
            e.printStackTrace();
        }
    }
    //GE FUNCTIONS

    //GP FUNCTIONS
    private String getGPSearchJson(){
        URL url;
        HttpsURLConnection connection;
        try {
            String urlStr = this.GP_BASE_SEARCH_LINK + "key=" + this.API_KEY + "&location=" +
                    this.getLatitude() + "," + this.getLongitude() + "&type=restaurant" + "&radius=" + this.travelRadius;
            Log.d("RESTAURANTROULETTELOG", "URL: "  + urlStr);
            url = new URL(urlStr);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int httpStatus = connection.getResponseCode();
            System.out.println(httpStatus);
            if (httpStatus == 201 || httpStatus == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                Log.d("RESTAURANTROULETTELOG", "Successfully got GP Search Json");
                return sb.toString();
            }

        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("RESTAURANTROULETTELOG", "Failed to get GP Search Json");
        return null;
    }

    private void parseGPSearchResponse(String httpsResponse) {
        this.clearRestaurantList();
        try{
            JSONObject jsonobj = new JSONObject(httpsResponse);
            JSONArray results = jsonobj.getJSONArray("results");
            for (int i = 0; i < results.length(); i++){
                jsonobj = results.getJSONObject(i);
                String restaurantID = jsonobj.getString("place_id");
//                this.pushToNearbyDatabase("" + i, restaurantID);
                this.pushToRestaurantList("" + i, restaurantID);
            }
            Log.d("RESTAURANTROULETTELOG", "Successfully parsed GP Search Json");
        }
        catch( org.json.JSONException e){
            Log.d("RESTAURANTROULETTELOG", "Failed to parse GP Search Json");
            e.printStackTrace();
        }
    }
    //GP FUNCTIONS


    private void getRandomRestaurantInfo(){
        String randomIndex = "" + (int) (Math.random() * (this.numNearbyRestaurants));
        Log.d("RESTAURANTROULETTELOG", "CHOSEN RANDOM RESTAURANT Index: " + randomIndex);
        String randRestaurantID = this.restaurantList.get(randomIndex);
        Log.d("RESTAURANTROULETTELOG", "CHOSEN RANDOM RESTAURANT ID: " + randRestaurantID);
        this.parseGPDetailsResponse(this.getGPDetailsJson(randRestaurantID));
    }

    // GP Place Details
    private String getGPDetailsJson (String randRestaurantID){
        URL url;
        HttpsURLConnection connection;
        try {
            String urlStr = this.GP_BASE_DETAILS_LINK + "key=" + this.API_KEY + "&placeid=" + randRestaurantID;
            Log.d("RESTAURANTROULETTELOG", "URL: "  + urlStr);
            url = new URL(urlStr);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int httpStatus = connection.getResponseCode();
            System.out.println(httpStatus);
            if (httpStatus == 201 || httpStatus == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                Log.d("RESTAURANTROULETTELOG", "Successfully got GP Details Json");
                return sb.toString();
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("RESTAURANTROULETTELOG", "Failed to get GP Details Json");
        return "";
    }
    //https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJfXQ5wgne3IARUHJ-tm201Nc&key=<INSERT KEY>
    private void parseGPDetailsResponse(String httpsResponse) {
        try{
            HashMap<String, String> restInfo = new HashMap<>();
            JSONObject json = new JSONObject(httpsResponse);
            if (json.has("result")) {
                json = json.getJSONObject("result");
            }
            if (json.has("place_id")) {
                String restID = json.getString("place_id");
                restInfo.put("restID", restID);
                Log.d("RESTAURANTROULETTELOG","Restaurant ID: " + restID);
            }
            if (json.has("name")) {
                String restName = json.getString("name");
                restInfo.put( "restName", restName);
                Log.d("RESTAURANTROULETTELOG","Restaurant Name: " + restName);
            }
            if (json.has("rating")) {
                String restRating = json.getString("rating");
                restInfo.put( "restRating", restRating);
                Log.d("RESTAURANTROULETTELOG","Restaurant Rating: " + restRating);
            }
            if (json.has("formatted_address")) {
                String address = json.getString("formatted_address");
                restInfo.put( "restAddress", address);
                Log.d("RESTAURANTROULETTELOG","Restaurant Address: " + address);
            }
            if (json.has("formatted_phone_number")) {
                String phoneNumber = json.getString("formatted_phone_number");
                restInfo.put( "restPhoneNumber", phoneNumber);
                Log.d("RESTAURANTROULETTELOG","Restaurant Phone #: " + phoneNumber);
            }
            if (json.has("website")) {
                String website = json.getString("website");
                restInfo.put("website", website);
                Log.d("RESTAURANTROULETTELOG","Restaurant Website: " + website);
            }
            if (json.has("url")) {
                String gMapsUrl = json.getString("url");
                restInfo.put("gMapsURL", gMapsUrl);
                Log.d("RESTAURANTROULETTELOG","Restaurant GMapsURL: " + gMapsUrl);
            }
            if (json.has("opening_hours")) {
                json = json.getJSONObject("opening_hours");
                if (json.has("weekday_text")) {
                    JSONArray hoursArray = json.getJSONArray("weekday_text");
                    String hours = "";
                    for (int i = 0; i < hoursArray.length(); i++){
                        hours += '\t' + hoursArray.getString(i) + '\n';
                    }
                    restInfo.put( "restHours", hours);
                    Log.d("RESTAURANTROULETTELOG","Restaurant Hours: " + hours);
                }
            }
            DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM,FormatStyle.SHORT);
            String time = LocalDateTime.now().format(dtf);
            restInfo.put("timeRolled", time);
            Log.d("RESTAURANTROULETTELOG", "Restaurant Accessed " + time);
            Log.d("RESTAURANTROULETTELOG", "Successfully Parsed Details Json");
            this.randRestaurantInfo = restInfo;
            this.pushToRollHistoryRef("" + this.numVisited, restInfo);
        }
        catch( org.json.JSONException e){
            Log.d("RESTAURANTROULETTELOG", "Failed to get Parse Details Json");
            this.randRestaurantInfo = null;
            e.printStackTrace();
        }
    }
    // GP Place Details
    // PRIVATE METHOD CALLS
}
