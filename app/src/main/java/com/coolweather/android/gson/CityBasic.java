package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class CityBasic {

    @SerializedName("cid")
    public String cityId;

    @SerializedName("location")
    public String cityName;

    public String parent_city;

    public String admin_area;
}
