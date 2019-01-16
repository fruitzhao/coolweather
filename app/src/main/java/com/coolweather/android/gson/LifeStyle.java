package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class LifeStyle {

    public String type;

    @SerializedName("brf")
    public String brief;

    @SerializedName("txt")
    public String info;
}
