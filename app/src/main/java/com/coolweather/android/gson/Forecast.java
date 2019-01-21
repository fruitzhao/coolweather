package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

    public String date;

    public String tmp_max;

    public String tmp_min;

    @SerializedName("cond_txt_d")
    public String info;

    @SerializedName("cond_code_d")
    public String forcastCode;
}
