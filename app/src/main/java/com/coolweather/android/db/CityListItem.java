package com.coolweather.android.db;

import org.litepal.crud.LitePalSupport;

public class CityListItem extends LitePalSupport {

    private String cityName;

    //private String cityWeather;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    //public String getCityWeather() {
    //    return cityWeather;
    //}

    //public void setCityWeather(String cityWeather) {
    //    this.cityWeather = cityWeather;
    //}
}
