package com.example.admin.googleplacesapp;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.admin.googleplacesapp.model.AutocompleteResult.AutocompleteResult;
import com.example.admin.googleplacesapp.model.AutocompleteResult.Prediction;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Admin on 8/26/2017.
 */

public class PlacesHelper {

    //https://maps.googleapis.com/maps/api/place/autocomplete/json?input=ban&key=API_KEY
    //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&type=restaurant&keyword=cruise&key=YOUR_API_KEY

    public static final String TAG = "PlacesAPI";
    public static final String API_KEY = "AIzaSyArobT3WQlXm9fEf3zrEiZzghpQGQifeQ0";

    OkHttpClient client = new OkHttpClient();
    Handler handler = new Handler(Looper.getMainLooper());

    ActivityViewContract view;

    PlacesHelper(ActivityViewContract view){
        this.view = view;
    }


    public void GetNearbyPlaces(Location location, String type){
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("place")
                .addPathSegment("nearbysearch")
                .addPathSegment("json")
                .addQueryParameter("location", Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude()))
                .addQueryParameter("radius", "10000")
                .addQueryParameter("type", type)
                //.addQueryParameter("keyword", keyword)
                .addQueryParameter("key", API_KEY)
                .build();

        Log.d(TAG, "GetNearbyPlaces: " + url);

        final Request request = new Request.Builder().url(url).build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String result = client.newCall(request).execute().body().string();
                    Log.d(TAG, "run: " + result);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            view.ParseNearbyPlacesResult(result);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //https://maps.googleapis.com/maps/api/place/autocomplete/json?input=ban&location=lat,lng&radius=10000&type=address&key=API_KEY
    //types: establishment, address

    public ArrayList<String> GetAutocompleteData(String input){
        Location location = view.getCurrentLocation();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("place")
                .addPathSegment("autocomplete")
                .addPathSegment("json")
                .addQueryParameter("location", Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude()))
                .addQueryParameter("radius", "10000")
                .addQueryParameter("type", "address")
                .addQueryParameter("input", input)
                .addQueryParameter("key", API_KEY)
                .build();

        final Request request = new Request.Builder().url(url).build();

        String result = "";

        try {
           result = client.newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "GetAutocompleteData: " + result);
        Gson gson = new Gson();
        AutocompleteResult foo = gson.fromJson(result, AutocompleteResult.class);
        ArrayList<String> out = new ArrayList<>();
        for (Prediction prediction : foo.getPredictions()) {
            out.add(prediction.getDescription());
        }
        return out;
    }

    private HttpUrl BuildQuery(String queryType, String queryArg){
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("place")
                .addPathSegment("autocomplete")
                .addPathSegment("json")
                .addQueryParameter(queryType, queryArg)
                .addQueryParameter("key", API_KEY)
                .build();
        return url;
    }


}
