package com.coolweather.android.util;

public class HourlyItem {

    private String hourlyTime;

    private int imageId;

    private String hourlyTmp;

    public HourlyItem(String hourlyTime, int imageId, String hourlyTmp) {
        this.hourlyTime = hourlyTime;
        this.imageId = imageId;
        this.hourlyTmp= hourlyTmp;
    }

    public String getHourlyTime() {
        return hourlyTime;
    }

    public int getImageId() {
        return imageId;
    }

    public String getHourlyTmp() {
        return hourlyTmp;
    }
}
