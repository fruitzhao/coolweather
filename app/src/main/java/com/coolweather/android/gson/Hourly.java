package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Hourly {

    @SerializedName("time")
    public String hourlyTime;

    @SerializedName("cond_code")
    public String hourlyCode;

    @SerializedName("tmp")
    public String hourlyTmp;


}
