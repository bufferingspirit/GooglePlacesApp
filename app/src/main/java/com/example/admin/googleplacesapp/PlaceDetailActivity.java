package com.example.admin.googleplacesapp;

import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.admin.googleplacesapp.model.AutocompleteResult.AutocompleteResult;
import com.example.admin.googleplacesapp.model.AutocompleteResult.Prediction;
import com.example.admin.googleplacesapp.model.PlacesDetail.PlaceDetails;
import com.example.admin.googleplacesapp.model.PlacesDetail.Result;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class PlaceDetailActivity extends AppCompatActivity {

    public static final String API_KEY = "AIzaSyArobT3WQlXm9fEf3zrEiZzghpQGQifeQ0";
    private static final String TAG = "PlaceDetailActivity";

    OkHttpClient client;
    Handler handler;

    TextView tvName, tvPhoneNumber, tvWebsite, tvAddress, tvRating;

    //name
    //website
    //phone number
    //address
    //pictures
    //rating

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);
        client = new OkHttpClient();
        handler = new Handler();

        tvName = (TextView) findViewById(R.id.tvName);
        tvWebsite = (TextView) findViewById(R.id.tvWebsite);
        tvPhoneNumber = (TextView) findViewById(R.id.tvPhoneNumber);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvRating = (TextView) findViewById(R.id.tvRating);

        Intent intent = getIntent();
        String id = intent.getStringExtra("result");
        GetPlaceDetail(id);
    }

    //https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJN1t_tDeuEmsRUsoyG83frY4&key=YOUR_API_KEY

    public void GetPlaceDetail(String id) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("place")
                .addPathSegment("details")
                .addPathSegment("json")
                .addQueryParameter("placeid", id)
                .addQueryParameter("key", API_KEY)
                .build();

        final Request request = new Request.Builder().url(url).build();

        Log.d(TAG, "GetPlaceDetail: " + request.toString());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String out = client.newCall(request).execute().body().string();
                    Log.d(TAG, "run: " + out);
                    Gson gson = new Gson();
                    PlaceDetails foo = gson.fromJson(out, PlaceDetails.class);
                    final Result result = foo.getResult();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(result.getName() != null)
                            tvName.setText(result.getName());
                            if(result.getWebsite() != null)
                            tvWebsite.setText(result.getWebsite());
                            if(result.getFormattedPhoneNumber() != null)
                            tvPhoneNumber.setText(result.getFormattedPhoneNumber());
                            if(result.getFormattedAddress()!=null)
                            tvAddress.setText(result.getFormattedAddress());
                            if(result.getRating()!=null)
                            tvRating.setText(Double.toString(result.getRating()));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
