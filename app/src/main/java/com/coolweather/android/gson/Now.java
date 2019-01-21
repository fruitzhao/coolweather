package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Now {


    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond_txt")
    public String info;

    @SerializedName("fl")
    public String senseTmp;

    public String wind_sc;

    public String wind_dir;
}
