package com.coolweather.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.bumptech.glide.Glide;
import com.coolweather.android.gson.AirQuility;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Hourly;
import com.coolweather.android.gson.LifeStyle;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HourlyAdapter;
import com.coolweather.android.util.HourlyItem;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;

    private Button navButton;

    private Button locButton;

    private LocationClient mLocationClient;

    private String myLocation;

    public SwipeRefreshLayout swipeRefresh;

    private String mWeatherId;

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private LinearLayout lifestyleLayout;

    private RecyclerView hourly;

    private ImageView bingPicImg;

    private ProgressBar progressBar;

    private LineChart forecastChart;

    private TextView airText;

    private TextView pm25;

    private TextView windSc;

    private TextView windDir;

    private TextView senseTmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //状态栏透明，填充软件内容
        if (Build.VERSION.SDK_INT >= 22) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化各控件
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        locButton = findViewById(R.id.loc_button);
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        lifestyleLayout = findViewById(R.id.lifestyle_layout);
        bingPicImg = findViewById(R.id.bing_pic_img);
        progressBar = findViewById(R.id.progress_bar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        forecastChart = findViewById(R.id.forecast_chart);
        windDir = findViewById(R.id.wind_dir);
        windSc = findViewById(R.id.wind_sc);
        hourly = findViewById(R.id.hourly);
        airText = findViewById(R.id.aqi_text);
        pm25 = findViewById(R.id.pm25);
        senseTmp = findViewById(R.id.sense_tmp);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        String airString = prefs.getString("air", null);
        if (weatherString != null && airString != null ) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
            AirQuility airQuility = Utility.handAirResponse(airString);
            showAirInfo(airQuility);
        } else {
            loadBingPic();
            progressBar.setVisibility(View.VISIBLE);
            requestLocation();
            progressBar.setVisibility(View.GONE);
        }


        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        locButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                requestLocation();
                progressBar.setVisibility(View.GONE);
            }
        });
    }


    /**
     * 根据天气id去服务器查询天气信息
     */
    public void requestWeather(final String weatherId) {

        String weatherUrl = "https://free-api.heweather.net/s6/weather?location=" + weatherId
                + "&key=2090e227836a43d591bb5b72820df5f4";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败(error:404)",
                                Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败(定位为：" + myLocation + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });

        String airUrl = "https://free-api.heweather.net/s6/air/now?location=" + weatherId
                + "&key=2090e227836a43d591bb5b72820df5f4";
        HttpUtil.sendOkHttpRequest(airUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取空气质量信息失败(error:404)",
                                Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final AirQuility airQuility = Utility.handAirResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (airQuility != null && "ok".equals(airQuility.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("air", responseText);
                            editor.apply();
                            showAirInfo(airQuility);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取空气质量信息失败(定位为：" + myLocation + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather) {

        forecastLayout.setVisibility(View.VISIBLE);
        lifestyleLayout.setVisibility(View.VISIBLE);
        String cityName = weather.basic.cityName;
        String updateTime = weather.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText("更新于 " + updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        //最高温度
        List<Entry> entriesMax = new ArrayList<Entry>();
        List<Entry> entriesMin = new ArrayList<Entry>();
        //横坐标值
        int i = 0;
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView dateWeek = view.findViewById(R.id.date_week);
            ImageView infoImage = view.findViewById(R.id.icon_forecast);
            TextView infoText = view.findViewById(R.id.info_text);
            String date[] = forecast.date.split("-");
            dateText.setText(date[1] + "/" + date[2]);
            if (i == 0) {
                dateWeek.setText("今天");
            } else {
                dateWeek.setText(dateToWeek(forecast.date));
            }
            int infoImgName = getResource("w" + forecast.forcastCode);
            infoImage.setImageResource(infoImgName);
            infoText.setText(forecast.info);
            forecastLayout.addView(view);
            Entry tmpMax = new Entry(i, Integer.parseInt(forecast.tmp_max));
            entriesMax.add(tmpMax);
            Entry tmpMin = new Entry(i, Integer.parseInt(forecast.tmp_min));
            entriesMin.add(tmpMin);
            i++;
            if (i > 5)
                break;
        }
        LineDataSet maxLine = new LineDataSet(entriesMax, "最高气温");
        LineDataSet minLine = new LineDataSet(entriesMin, "最低气温");
        maxLine.setAxisDependency(YAxis.AxisDependency.LEFT);
        maxLine.setColor(Color.parseColor("#FF3366"));
        maxLine.setValueTextSize(15);
        maxLine.setValueTextColor(Color.WHITE);
        maxLine.setLineWidth(2f);
        minLine.setAxisDependency(YAxis.AxisDependency.LEFT);
        List<ILineDataSet> dataSets = new ArrayList<>();
        minLine.setColor(Color.parseColor("#6699FF"));
        minLine.setValueTextSize(15);
        minLine.setValueTextColor(Color.WHITE);
        minLine.setLineWidth(2f);
        dataSets.add(maxLine);
        dataSets.add(minLine);
        LineData data = new LineData(dataSets);
        data.setValueFormatter(new MyValueFormatter());
        forecastChart.setData(data);
        forecastChart.setAutoScaleMinMaxEnabled(true);
        forecastChart.getXAxis().setDrawGridLines(false);
        forecastChart.getXAxis().setEnabled(false);
        forecastChart.getAxisRight().setDrawGridLines(false);
        forecastChart.getAxisLeft().setDrawGridLines(false);
        forecastChart.getAxisLeft().setEnabled(false);
        forecastChart.getAxisRight().setEnabled(false);
        forecastChart.getLegend().setTextColor(Color.WHITE);
        forecastChart.getLegend().setTextSize(12);
        forecastChart.setTouchEnabled(false);
        forecastChart.getDescription().setEnabled(false);
        forecastChart.invalidate(); // refresh

        lifestyleLayout.removeAllViews();
        for (LifeStyle lifeStyle : weather.lifeStyleList) {
            View view = LayoutInflater.from(this).inflate(R.layout.lifestyle_item, lifestyleLayout, false);
            TextView briefText = view.findViewById(R.id.lifestyle_brief_text);
            TextView infoText = view.findViewById(R.id.lifestyle_info_text);
            briefText.setText(lifeStyleType(lifeStyle.type) + " : " + lifeStyle.brief);
            infoText.setText(lifeStyle.info);
            lifestyleLayout.addView(view);
        }


        List<HourlyItem> hourlyItems = new ArrayList<>();
        for (Hourly hourly : weather.hourlyList) {
            String time[] =hourly.hourlyTime.split(" ");
            String nowTime = time[1];
            String imgName = null;
            if (isDaytime(nowTime)) {
                imgName = "w" + hourly.hourlyCode;
            } else {
                imgName = "w" + hourly.hourlyCode + "n";
            }
            HourlyItem hourlyItem = new HourlyItem(nowTime,
                    getResource(imgName), hourly.hourlyTmp + "°");
            hourlyItems.add(hourlyItem);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        hourly.setLayoutManager(layoutManager);
        HourlyAdapter adapter = new HourlyAdapter(hourlyItems);
        hourly.setAdapter(adapter);

        windDir.setText(weather.now.wind_dir);
        windSc.setText(weather.now.wind_sc + "级");
        senseTmp.setText(weather.now.senseTmp + "°");


        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /**
     * 展示空气状况
     */
    private void showAirInfo(AirQuility airQuility) {
        airText.setText(airQuility.air.airQuility);
        pm25.setText(airQuility.air.pm25);

    }

    private void requestLocation() {
        mLocationClient.start();
    }

    /**
     * 定位监听器
     */
    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            myLocation = location.getLongitude() + "," + location.getLatitude();
            requestWeather(myLocation);
            mLocationClient.stop();
        }

    }

    /**
     * 建立字典转换生活建议类型
     */
    private static String lifeStyleType(String type) {
        Map<String, String> map = new HashMap<>();
        map.put("comf", "舒适度指数");
        map.put("cw", "洗车指数");
        map.put("drsg", "穿衣指数");
        map.put("flu", "感冒指数");
        map.put("sport", "运动指数");
        map.put("trav", "旅游指数");
        map.put("uv", "紫外线指数");
        map.put("air", "空气污染扩散条件指数");
        map.put("ac", "空调开启指数");
        map.put("ag", "过敏指数");
        map.put("gl", "太阳镜指数");
        map.put("mu", "化妆指数");
        map.put("airc", "晾晒指数");
        map.put("ptfc", "交通指数");
        map.put("fsh", "钓鱼指数");
        map.put("spi", "防晒指数");
        if (map.get(type) != null) {
            return map.get(type);
        } else {
            return type;
        }
    }

    /**
     * 获取图片名称获取图片的资源id的方法
     *
     * @param imageName
     * @return
     */
    public int getResource(String imageName) {
        Context ctx = getBaseContext();
        int resId = getResources().getIdentifier(imageName, "drawable", ctx.getPackageName());
        return resId;

    }

    /**
     * 折线图数据格式
     */
    public class MyValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0"); // use int
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return mFormat.format(value) + "°"; // e.g. append a temp
        }
    }

    /**
     * 日期转星期
     *
     * @param datetime
     * @return
     */
    public static String dateToWeek(String datetime) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String[] weekDays = { "周日", "周一", "周二", "周三", "周四", "周五", "周六" };
        Calendar cal = Calendar.getInstance(); // 获得一个日历
        Date datet = null;
        try {
            datet = f.parse(datetime);
            cal.setTime(datet);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 指示一个星期中的某天。
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    /**
     * 判断是否白天
     */
    private boolean isDaytime(String time)  {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        try {
            Date now = df.parse(time);
            Date begin = df.parse("6:00");
            Date end = df.parse("18:00");
            Calendar nowTime = Calendar.getInstance();
            nowTime.setTime(now);
            Calendar beginTime = Calendar.getInstance();
            beginTime.setTime(begin);
            Calendar endTime = Calendar.getInstance();
            endTime.setTime(end);
            if (nowTime.before(endTime) && nowTime.after(beginTime)) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

}
