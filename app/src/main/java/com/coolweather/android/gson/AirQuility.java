package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class AirQuility {

    @SerializedName("air_now_city")
    public Air air;

    public String status;

}
