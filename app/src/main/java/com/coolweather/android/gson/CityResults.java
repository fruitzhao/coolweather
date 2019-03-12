package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CityResults {


    @SerializedName("basic")
    public List<CityBasic> cityBasicList;
}
