package com.coolweather.android.util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coolweather.android.R;

import java.util.List;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.MyViewHolder> {

    private List<HourlyItem> mHourlyList;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView hourlyTime;
        ImageView hourlyImage;
        TextView hourlyTmp;

        public MyViewHolder(View view) {
            super(view);
            hourlyTime = view.findViewById(R.id.hourly_time);
            hourlyImage = view.findViewById(R.id.hourly_image);
            hourlyTmp = view.findViewById(R.id.hourly_tmp);
        }
    }

    public HourlyAdapter(List<HourlyItem> hourlyList) {
        mHourlyList = hourlyList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hourly_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        HourlyItem hourly = mHourlyList.get(position);
        holder.hourlyTime.setText(hourly.getHourlyTime());
        holder.hourlyImage.setImageResource(hourly.getImageId());
        holder.hourlyTmp.setText(hourly.getHourlyTmp());
    }

    @Override
    public int getItemCount() {
        return mHourlyList.size();
    }
}
