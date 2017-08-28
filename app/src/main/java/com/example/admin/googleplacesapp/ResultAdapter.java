package com.example.admin.googleplacesapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.admin.googleplacesapp.model.NearbyResult.Result;

import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Admin on 8/27/2017.
 */

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    public static final String API_KEY = "AIzaSyArobT3WQlXm9fEf3zrEiZzghpQGQifeQ0";

    private static final String TAG = "ResultAdapter";
    ArrayList<Result> resultList;
    Context context;

    public ResultAdapter(ArrayList<Result> resultList) {
        this.resultList = resultList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.result_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResultAdapter.ViewHolder holder, final int position) {
        final Result result = resultList.get(position);
        holder.tvName.setText(result.getName());
        if(result.getPhotos() != null) {

            String out = "https://maps.googleapis.com/maps/api/place/photo"
                    + "?photoreference=" + result.getPhotos().get(0).getPhotoReference()
                    + "&sensor=false$maxheight=400&maxwidth=400" + "&key=" + API_KEY;
            Log.d(TAG, "onBindViewHolder: " + out);
            Glide.with(context).load(out).into(holder.ivPhoto);

        }
        holder.itemView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlaceDetailActivity.class);
                intent.putExtra("result", resultList.get(position).getPlaceId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        if(resultList == null){
            return 0;
        }
        else{return resultList.size();}
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        ImageView ivPhoto;

        public ViewHolder(View itemView) {
            super(itemView);

            tvName = (TextView) itemView.findViewById(R.id.tvName);
            ivPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
        }
    }
}

