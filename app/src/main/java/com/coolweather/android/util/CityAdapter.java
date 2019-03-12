package com.coolweather.android.util;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coolweather.android.OnItemClickListener;
import com.coolweather.android.R;

import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

    private Context mContext;

    private List<CityItem> mCityList;

    private OnItemClickListener mItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView cityName;
        //TextView cityWeather;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            cityName = view.findViewById(R.id.city_name);
            //cityWeather = view.findViewById(R.id.city_weather);
        }
    }

    public CityAdapter(List<CityItem> cityList) {
        mCityList = cityList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.city_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onItemClick(v);
            }
        });
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mItemClickListener.onItemLongClick(v);
                return true;
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CityItem cityItem = mCityList.get(position);
        String cityNameTemp = cityItem.getName();
        holder.cityName.setText(cityNameTemp.replaceAll(",", "  "));
        //holder.cityWeather.setText(cityItem.getWeather());
        if (mCityList != null) {
            holder.itemView.setTag(mCityList.get(position).getName());
        }
    }

    @Override
    public int getItemCount() {
        return  mCityList.size();
    }

    //  删除数据
    public void removeCity(int position) {
        mCityList.remove(position);
        //删除动画
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

}
