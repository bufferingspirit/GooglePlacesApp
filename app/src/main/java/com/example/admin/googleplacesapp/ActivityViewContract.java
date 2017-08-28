package com.example.admin.googleplacesapp;

import android.location.Location;

/**
 * Created by Admin on 8/27/2017.
 */

public interface ActivityViewContract {

     void ParseNearbyPlacesResult(String s);

     Location getCurrentLocation();

}
