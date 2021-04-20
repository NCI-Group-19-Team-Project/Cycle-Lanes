package com.example.cyclelanes;
/*
 *
 *
 *
 *
 *
 *
 *
 *
 * */

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonLineStringStyle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static com.example.cyclelanes.R.raw.dublinbikelanes;
import static com.example.cyclelanes.R.raw.galwaycyclelanes;
import static com.example.cyclelanes.R.raw.test;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    //data members
    private GoogleMap mMap;//GoogleMap Object
    SearchView searchView;
    ToggleButton toggleBikeLanes;

    //current and destination location objects
    Location myLocation = null;//used to store location of device
    Location destinationLocation = null;
    protected LatLng start = null;//coordinates of start
    protected LatLng end = null;//coordinates of end
    protected LatLng setMapStart = null;

    //to get location permissions.
    private final static int LOCATION_REQUEST_CODE = 23;//used for location permission
    boolean locationPermission = false;//initialise location permission as false

    //polyline object
    private List<Polyline> polylines = null;//stores coordinates for routes
    private List<Address> addressList = null;//stores address for geocoder object

    //routeCoordinates array
    private List<LatLng> routeCoordinates = null;
    private List<BikeLanesObject> jsonBikeLanes = null;
    private List<Integer> laneObjectID = null;
    private List<Integer> laneObjectSize = null;
    private List<LatLng> laneCoordiantes= null;

    private static final String TAG = MainActivity.class.getSimpleName();

    LatLngBounds bounds;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {//This acts as the main method for the android application
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//R is used to locate files in the res folder such as XML, JSON and other text formats

        requestPermision();//calls method to request location data

        //initialise the google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);//gets the map fragment from the activity_main.xml. It then uses R to locate the ID of the fragment which looks like "android:id="@+id/map" in the file
        mapFragment.getMapAsync(this);//calls the map Object

        try {
            parseJSONData();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void requestPermision() {//this method gets the permission from the manifest file
        //if the location permission is false, ask user to share their location. Note that the manifest file will save that choice by altering location permission in the phone settings
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        } else {
            locationPermission = true;//else if the location permission is already granted, set the value to true
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //if permission granted.
                    locationPermission = true;
                    getMyLocation();//if location permission is granted call the method to get the users location

                } else {

                }
                return;
            }
        }
    }

    private void getMyLocation() {//this method gets the location of the users device
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {//uses method in the google map api to track changes in location

                myLocation = location;
                LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());


                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ltlng, 10));
            }

        });


        //when the user clicks on an are on the map, set the end latlng to the coordinates that the user clicked
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                end = latLng;

                mMap.clear();

                //start=new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
                start = new LatLng(53.3330556, -6.2488889);//for testing purposes

                Findroutes(start, end);//calls FindRoutes method to calculate route using the start as the user location and the end as the area that the user clicked
            }
        });


    }//end getMyLocation()


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getMyLocation();
        getCycleLaneData();

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        LatLng latlng=new LatLng(53.3330556,-6.2488889);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14));

        Log.i(TAG, "onCameraMove: "+ mMap.getProjection().getVisibleRegion().latLngBounds);

    }


    private void getCycleLaneData() {
        /*try {
            GeoJsonLayer layer=new GeoJsonLayer(mMap, R.raw.test, getApplicationContext());
            GeoJsonLineStringStyle lineStringStyle = layer.getDefaultLineStringStyle();
            lineStringStyle.setColor(Color.RED);
            lineStringStyle.setPolygonStrokeWidth(1);
            layer.addLayerToMap();

            /*toggleBikeLanes = findViewById(R.id.toggleBikeLanes);
            toggleBikeLanes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        layer.addLayerToMap();
                    }else{
                        layer.removeLayerFromMap();
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/



    }

    //parse json and get coordinates of bike lanes
    public void parseJSONData() throws JSONException, IOException, ParseException {
        double lat, lng;
        int count = 0;
        LatLng bikeLaneCoordinates=null;
        jsonBikeLanes=new ArrayList<>();

        String jsonStr=readJSONFromResource(dublinbikelanes);
        JSONObject reader=new JSONObject(jsonStr);
        JSONArray features=reader.getJSONArray("features");

        for(int i=0;i<features.length();i++){
            JSONObject position =(JSONObject) features.get(i);

            JSONObject properties= (JSONObject) position.get("properties");
            int objectID= (int) properties.get("OBJECTID");

            JSONObject geometry= (JSONObject) position.get("geometry");

            JSONArray coordinates=geometry.getJSONArray("coordinates");
            String type= (String) geometry.get("type");


            JSONArray latLngCoordinates;

            BikeLanesObject bikeLanesObject;

            if(type.equals("MultiLineString")){

                for(int j=0;j<coordinates.length();j++){
                    JSONArray mutliCoordinates = (JSONArray) coordinates.get(j);

                    for(int k=0;k<mutliCoordinates.length();k++){
                        latLngCoordinates= (JSONArray) mutliCoordinates.get(k);
                        lat = (double) latLngCoordinates.get(1);
                        lng = (double) latLngCoordinates.get(0);
                        bikeLaneCoordinates=new LatLng(lat,lng);
                        bikeLanesObject=new BikeLanesObject(objectID, bikeLaneCoordinates);
                        jsonBikeLanes.add(bikeLanesObject);

                    }
                    count++;
                }
            }
            else if(type.equals("LineString")){

                for(int j=0;j<coordinates.length();j++){
                    latLngCoordinates= (JSONArray) coordinates.get(j);
                    lat = (double) latLngCoordinates.get(1);
                    lng = (double) latLngCoordinates.get(0);
                    bikeLaneCoordinates=new LatLng(lat,lng);
                    bikeLanesObject=new BikeLanesObject(objectID, bikeLaneCoordinates);
                    jsonBikeLanes.add(bikeLanesObject);

                }
                count++;
            }

        }
        Log.i(TAG, "parseJSONData: "+jsonBikeLanes.get(jsonBikeLanes.size()-1).objectID);
        Log.i(TAG, "parseJSONData: count: "+count);
        Log.i(TAG, "parseJSONData: array: "+jsonBikeLanes.size());
    }

    //read in galwaycyclelanes geojson from raw resources folder and convert to string
    public String readJSONFromResource(int fileID) throws IOException {
        InputStream is = getResources().openRawResource(dublinbikelanes);
        Writer writer = new StringWriter();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line = reader.readLine();
        while(line !=null){
            writer.write(line);
            line = reader.readLine();
        }
        String jsonStr= writer.toString();
        return jsonStr;
    }




    // Calculate the route
    public void Findroutes(LatLng Start, LatLng End)
    {
        if(Start==null || End==null) {
            Toast.makeText(MainActivity.this,"Unable to get location",Toast.LENGTH_LONG).show();
        }
        else
        {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.BIKING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyBar81Cy8YtRv1fpXNVxUy4taLgI8Pn238")
                    .build();
            routing.execute();

        }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
//        Findroutes(start,end);
    }

    @Override
    public void onRoutingStart() {
        Toast.makeText(MainActivity.this,"Finding Route...",Toast.LENGTH_LONG).show();//shows text on screen when the route is calculated
    }

    //If the route builder finds  route
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        try {
            if (polylines != null) {//if there are polylines already drawn in the map clear them
                polylines.clear();
            }
            PolylineOptions polyOptions = new PolylineOptions();
            LatLng polylineStartLatLng = null;
            LatLng polylineEndLatLng = null;


            polylines = new ArrayList<>();
            routeCoordinates=new ArrayList<>();
            //draw the route on the mp using the polyline object from google maps api
            for (int i = 0; i < route.size(); i++) {

                if (i == shortestRouteIndex) {
                    polyOptions.color(getResources().getColor(R.color.colorPrimary));
                    polyOptions.width(10);
                    polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                    Polyline polyline = mMap.addPolyline(polyOptions);
                    polylineStartLatLng = polyline.getPoints().get(0);
                    int k = polyline.getPoints().size();
                    polylineEndLatLng = polyline.getPoints().get(k - 1);
                    polylines.add(polyline);

                    //for each coordinate in the route add to ArrayList
                    for (LatLng point: route.get(i).getPoints()) {
                        routeCoordinates.add(point);
                    }

                    /*double routeLat=routeCoordinates.get(i).latitude;
                    double routeLng=routeCoordinates.get(i).longitude;

                    for(int j=0;j<jsonBikeLanes.size();j++){
                        double jsonLat=jsonBikeLanes.get(j).latitude;
                        double jsonLng=jsonBikeLanes.get(j).longitude;

                    }*/



                } else {

                }


            }

            //center camera over route
            mMap.moveCamera(CameraUpdateFactory.newLatLng(route.get(0).getLatLgnBounds().getCenter()));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();


            builder.include(polylineStartLatLng);
            builder.include(polylineEndLatLng);
            bounds=builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

            //Add Marker on route starting position
            MarkerOptions startMarker = new MarkerOptions();
            startMarker.position(polylineStartLatLng );
            startMarker.title("My Location");
            mMap.addMarker(startMarker);

            //Add Marker on route ending position
            MarkerOptions endMarker = new MarkerOptions();
            endMarker.position(polylineEndLatLng);
            endMarker.title("Destination");
            mMap.addMarker(endMarker);

            Log.i(TAG, "onRoutingSuccess: bounds: "+bounds);
            Log.i(TAG, "findCycleLanesOnRoute: routeCoordinates: "+routeCoordinates.toString());
            Log.i(TAG, "findCycleLanesOnRoute: routeCoordinates: "+routeCoordinates.size());

            findCycleLanesOnRoute();


        }catch(Exception e){

        }
    }

    private void findCycleLanesOnRoute() throws JSONException, IOException {

        double currentLat = 0, currentLng = 0, nextLat = 0, nextLng = 0;
        LatLng currentPoint = null;
        LatLng nextPoint=null;

        double routeBearing;
        
        double bikeLat, bikeLng;
        LatLng bikeLane;
        int objectID;
        BikeLanesObject laneObject;

        laneObjectID=new ArrayList<Integer>();
        ArrayList <Integer> testList=new ArrayList<>();

        for(int i=0;i<routeCoordinates.size();i++){

            if(i != routeCoordinates.size()-1){
                currentLat=routeCoordinates.get(i).latitude;
                currentLng=routeCoordinates.get(i).longitude;
                currentPoint=new LatLng(currentLat,currentLng);

                nextLat=routeCoordinates.get(i+1).latitude;
                nextLng=routeCoordinates.get(i+1).longitude;
                nextPoint=new LatLng(nextLat,nextLng);
            }

            routeBearing=findRouteBearing(currentPoint, nextPoint);



            for(int j=0;j<jsonBikeLanes.size();j++){
                bikeLat=jsonBikeLanes.get(j).coordinates.latitude;
                bikeLng=jsonBikeLanes.get(j).coordinates.longitude;
                bikeLane=new LatLng(bikeLat, bikeLng);
                objectID=jsonBikeLanes.get(j).objectID;
                laneObject=new BikeLanesObject(objectID, bikeLane);
                int testInt=0;

                //if statement to find matches
                if(routeBearing>=0 && routeBearing<=90){
                    if((bikeLat>=currentLat && bikeLat<=nextLat) && (bikeLng>=currentLng && bikeLng<=nextLng)){
                        findBikeLane(objectID);
                        Log.i(TAG, "findCycleLanesOnRoute: found 1");
                    }
                }
                else if(routeBearing>90 && routeBearing<=180){
                    if((bikeLat<=currentLat && bikeLat>=nextLat) && (bikeLng>=currentLng && bikeLng<=nextLng)){
                        findBikeLane(objectID);
                        Log.i(TAG, "findCycleLanesOnRoute: found 2");
                    }
                }
                else if(routeBearing>180 && routeBearing<=270){
                    if((bikeLat<=currentLat && bikeLat>=nextLat) && (bikeLng<=currentLng && bikeLng>=nextLng)){
                        findBikeLane(objectID);
                        Log.i(TAG, "findCycleLanesOnRoute: found 3");
                    }
                }
                else if(routeBearing>270 && routeBearing<=360){
                    if((bikeLat>=currentLat && bikeLat<=nextLat) && (bikeLng<=currentLng && bikeLng>=nextLng)){
                        findBikeLane(objectID);
                        Log.i(TAG, "findCycleLanesOnRoute: found 4");
                    }
                }
            }
        }

        Log.i(TAG, "findCycleLanesOnRoute: count: "+laneObjectSize);

        laneCoordinatesToJson();


    }

    private List<Integer> findBikeLane(int objectID) {
        for(int i=0;i<jsonBikeLanes.size();i++){
            if(jsonBikeLanes.get(i).objectID==objectID){
                if(laneObjectID.size()==0){
                    laneObjectID.add(objectID);
                }else{
                    for(int j=0;j<laneObjectID.size();j++){
                        if(!laneObjectID.contains(jsonBikeLanes.get(i).objectID)){
                            laneObjectID.add(objectID);
                        }
                    }
                }

            }
        }


        return laneObjectID;
    }

    private void laneCoordinatesToJson() throws JSONException, IOException {
        JSONObject cycleLanesObject;//stores the whole object
        JSONArray coordinatesArray;
        JSONArray coordinates;
        JSONObject geometryObj;
        JSONObject propertiesObj;
        JSONObject featuresObj;
        JSONArray featuresArray;

        Log.i(TAG, "laneCoordinatesToJson: parsing json: "+laneObjectID.toString());
        Log.i(TAG, "laneCoordinatesToJson: parsing json: "+laneObjectID.size());


        featuresArray = new JSONArray();

        for(int i=0;i<laneObjectID.size();i++){
            coordinatesArray=new JSONArray();

            for(int j=0;j<jsonBikeLanes.size();j++){
                if(laneObjectID.get(i) == jsonBikeLanes.get(j).objectID){
                    coordinates = new JSONArray();
                    coordinates.put(jsonBikeLanes.get(j).coordinates.longitude);
                    coordinates.put(jsonBikeLanes.get(j).coordinates.latitude);

                    coordinatesArray.put(coordinates);
                }
            }


            geometryObj = new JSONObject();
            geometryObj.put("type","LineString");
            geometryObj.put("coordinates",coordinatesArray);

            propertiesObj = new JSONObject();
            propertiesObj.put("OBJECTID",laneObjectID.get(i));

            featuresObj = new JSONObject();
            featuresObj.put("type","Feature");
            featuresObj.put("properties",propertiesObj);
            featuresObj.put("geometry",geometryObj);
            featuresArray.put(featuresObj);
        }

        cycleLanesObject=new JSONObject();
        cycleLanesObject.put("type","FeatureCollection");
        cycleLanesObject.put("features",featuresArray);
        String cycleLanes=cycleLanesObject.toString();

        Log.i(TAG, "laneCoordinatesToJson: json: "+cycleLanesObject.toString());

        GeoJsonLayer layer=new GeoJsonLayer(mMap, cycleLanesObject);
        GeoJsonLineStringStyle lineStringStyle = layer.getDefaultLineStringStyle();
        lineStringStyle.setColor(Color.RED);
        lineStringStyle.setPolygonStrokeWidth(1);
        layer.addLayerToMap();
    }

    private double findRouteBearing(LatLng startLatLng, LatLng endLatLng) {
        double startLat=startLatLng.latitude;
        double startLng=startLatLng.longitude;
        double endLat=endLatLng.latitude;
        double endLng=endLatLng.longitude;

        double startLatRad=Math.toRadians(startLat);
        double endLatRad=Math.toRadians(endLat);
        double lngDiff=Math.toRadians(endLng-startLng);

        double y=Math.sin(lngDiff)*Math.cos(endLatRad);
        double x=Math.cos(startLatRad)*Math.sin(endLatRad)-Math.sin(startLatRad)*Math.cos(endLatRad)*Math.cos(lngDiff);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }

    @Override
    public void onRoutingCancelled() {
        Findroutes(start,end);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Findroutes(start,end);

    }

}