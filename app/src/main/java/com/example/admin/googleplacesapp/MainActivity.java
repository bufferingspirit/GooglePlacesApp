package com.example.admin.googleplacesapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import com.example.admin.googleplacesapp.model.NearbyResult.NearbyResult;
import com.example.admin.googleplacesapp.model.NearbyResult.Result;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ActivityViewContract, OnMapReadyCallback{

    ArrayList<String> placeTypes = new ArrayList<>(Arrays.asList(
            "accounting", "airport", "amusement_park", "aquarium",
            "art_gallery", "atm", "bakery", "bank", "bar", "beauty_salon", "bicycle_store", "book_store",
            "bowling_alley", "bus_station", "cafe", "campground", "car_dealer", "car_rental", "car_repair",
            "car_wash", "casino", "cemetery", "church", "city_hall", "clothing_store", "convenience_store",
            "courthouse", "dentist", "department_store", "doctor", "electrician", "electronics_store",
            "embassy", "fire_station", "florist", "funeral_home", "furniture_store", "gas_station", "gym",
            "hair_care", "hardware_store", "hindu_temple", "home_goods_store", "hospital", "insurance_agency",
            "jewelry_store", "laundry", "lawyer", "library", "liquor_store", "local_government_office", "locksmith",
            "lodging", "meal_delivery", "meal_takeaway", "mosque", "movie_rental", "movie_theater", "moving_company",
            "museum", "night_club", "painter", "park", "parking", "pet_store", "pharmacy", "physiotherapist", "plumber",
            "police", "post_office", "real_estate_agency", "restaurant", "roofing_contractor", "rv_park",
            "school", "shoe_store", "shopping_mall", "spa", "stadium", "storage", "store", "subway_station", "synagogue",
            "taxi_stand", "train_station", "transit_station", "travel_agency", "university", "veterinary_care", "zoo"
    ));

    public static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 10;

    Location currentLocation;
    PlacesHelper placesHelper;
    Geocoder geocoder;

    AutoCompleteTextView tvAutoComplete;
    Spinner typeSpinner;
    RecyclerView recView;
    RecyclerView.ItemAnimator itemAnimatior;
    RecyclerView.LayoutManager linearLayout;
    ArrayList<Result> resultList;
    ResultAdapter resultAdapter;

    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check the permissions
        checkPermissions();
        checkLocationEnabled();

        //Bind Recycler view
        recView = (RecyclerView) findViewById(R.id.recView);
        linearLayout = new LinearLayoutManager(this);
        itemAnimatior = new DefaultItemAnimator();
        recView.setItemAnimator(itemAnimatior);
        recView.setLayoutManager(linearLayout);
        resultList = new ArrayList<>();
        resultAdapter = new ResultAdapter(resultList);
        recView.setAdapter(resultAdapter);

        //Bind Autocomplete Text
        placesHelper = new PlacesHelper(this);
        geocoder = new Geocoder(this);
        tvAutoComplete = (AutoCompleteTextView) findViewById(R.id.tvAutoComplete);
        tvAutoComplete.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item, placesHelper));
        tvAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String description = (String) parent.getItemAtPosition(position);
                if(geocoder.isPresent()){
                    List<Address> list = null;
                    try {
                        list = geocoder.getFromLocationName(description, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = list.get(0);
                    LatLng foo = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(foo).title(description));
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .zoom(11)
                            .target(foo)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

            }
        });

        //Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getLocation();

        //Create place type spinner and bind it
        typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, placeTypes);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(dataAdapter);
        typeSpinner.setSelection(ArrayAdapter.NO_SELECTION, false);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mMap.clear();
                resultList.clear();
                LatLng pos = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().icon(bitmapDescriptorFromVector(getBaseContext()
                        ,R.drawable.ic_my_location_black_24dp)).position(pos).title("You are here"));
                placesHelper.GetNearbyPlaces(currentLocation, adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public Location getCurrentLocation(){
        return currentLocation;
    }


    FusedLocationProviderClient fusedLocationProviderClient;
    public void getLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d(TAG, "onSuccess: " + location.toString());

                        currentLocation = location;
                        //placesHelper.GetNearbyPlaces(location, "restaurant");
                        // Add a marker in Sydney and move the camera
                        LatLng pos = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        mMap.addMarker(new MarkerOptions().icon(bitmapDescriptorFromVector(getBaseContext()
                                ,R.drawable.ic_my_location_black_24dp)).position(pos).title("You are here"));
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .zoom(11)
                                .target(pos)
                                .build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.toString());
                    }
                });

        //LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100, this);

    }

    public void checkLocationEnabled(){
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Activate GPS");
            dialog.setPositiveButton(("Activate GPS"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("Do Not Activate", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }
    }

    private void checkPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else{
            //getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void ParseNearbyPlacesResult(String s) {
        //Log.d(TAG, "ParseNearbyPlacesResult: " + s);
        Gson gson = new Gson();
        NearbyResult nearbyResults = gson.fromJson(s, NearbyResult.class);
        List<Result> results = nearbyResults.getResults();
        for (Result foo: results) {
            LatLng bar = new LatLng(foo.getGeometry().getLocation().getLat()
                    , foo.getGeometry().getLocation().getLng());
            mMap.addMarker(new MarkerOptions().position(bar).title(foo.getName()));
            resultList.add(foo);
        }
        resultAdapter.notifyDataSetChanged();
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
