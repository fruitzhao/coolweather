package com.coolweather.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
//import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.CityListItem;
import com.coolweather.android.gson.CityBasic;
import com.coolweather.android.gson.CityResults;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;
import com.githang.statusbar.StatusBarCompat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddCity extends BaseActivity {

    private List<String> listData;

    private SearchView searchView;

    private ListView listView;

    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);

        //设置状态栏颜色
        int color = Color.parseColor("#6a92ca");
        StatusBarCompat.setStatusBarColor(this, color);

        listData = new ArrayList<>();
        searchView = findViewById(R.id.searchView);
        listView = findViewById(R.id.search_list);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);

        //修改搜索框文字颜色
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.parseColor("#ffffff"));
        textView.setHintTextColor(Color.parseColor("#ffffff"));
        //取消下划线
        if (searchView != null) {
            try {        //--拿到字节码
                Class<?> argClass = searchView.getClass();
                //--指定某个私有属性,mSearchPlate是搜索框父布局的名字
                Field ownField = argClass.getDeclaredField("mSearchPlate");
                //--暴力反射,只有暴力反射才能拿到私有属性
                ownField.setAccessible(true);
                View mView = (View) ownField.get(searchView);
                //--设置背景
                mView.setBackgroundColor(Color.TRANSPARENT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //修改搜索图标颜色
        int magId = getResources().getIdentifier("android:id/search_mag_icon",null, null);
        ImageView magImage = (ImageView) searchView.findViewById(magId);
        //magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        magImage.setColorFilter(Color.parseColor("#ffffff"));

        searchView.setFocusable(true);
        searchView.setFocusableInTouchMode(true);
        searchView.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) searchView.getContext().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(searchView, 0);

        // 设置搜索文本监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                requestCity(query);
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    //Toast.makeText(AddCity.this, "输入内容已改变", Toast.LENGTH_SHORT).show();
                    requestCity(newText);
                } else {
                    listView.clearTextFilter();
                }
                return false;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AddCity.this, WeatherActivity.class);
                String[] item = listView.getItemAtPosition(position).toString().split(" ");
                String weatherId = item[0] + "," + item[1];
                intent.putExtra("weatherId", weatherId);
                //储存到数据库
                CityListItem cityListItem = new CityListItem();
                cityListItem.setCityName(weatherId);
                cityListItem.save();
                startActivity(intent);
                ActivityCollector.finishAll();
            }
        });

    }

    private void requestCity(final String cityInput) {
        String airUrl = "https://search.heweather.net/find?location=" + cityInput
                + "&key=f49f7d4b2296444ba9d8d0a3f653723c";
        HttpUtil.sendOkHttpRequest(airUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AddCity.this, "查询失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final CityResults cityResults = Utility.handCityResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (cityResults != null) {
                            showCity(cityResults.cityBasicList);
                        } else {
                            Toast.makeText(AddCity.this, "无匹配结果",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void showCity(List<CityBasic> cityResults) {
        listData.clear();
        if (cityResults != null) {
            for (int i = 0; i < cityResults.size(); i++) {
                listData.add(cityResults.get(i).cityName + " " +
                        cityResults.get(i).parent_city + " " +cityResults.get(i).admin_area);
            }
            adapter.notifyDataSetChanged();
        }
    }
}
