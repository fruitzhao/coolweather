package com.coolweather.android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.coolweather.android.db.CityListItem;
import com.coolweather.android.util.CityAdapter;
import com.coolweather.android.util.CityItem;
import com.githang.statusbar.StatusBarCompat;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

public class ChooseCity extends BaseActivity {

    FloatingActionButton fab;

    RecyclerView cityRecyclerView;

    List<CityItem> cityItems = new ArrayList<>();

    CityAdapter adapter;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        状态栏透明，填充软件内容
//        if (Build.VERSION.SDK_INT >= 22) {
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
//                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }
        setContentView(R.layout.activity_choose_city);
        //设置状态栏颜色
        int color = Color.parseColor("#6a92ca");
        StatusBarCompat.setStatusBarColor(this, color);
        toolbar = findViewById(R.id.toolbar_choose_city);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        fab = findViewById(R.id.add_area);
        cityRecyclerView = findViewById(R.id.city_recycle_view);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseCity.this, AddCity.class);
                startActivity(intent);
                finish();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cityRecyclerView.setLayoutManager(layoutManager);

        //从数据库中读取已保存城市列表
        List<CityListItem> cityList = LitePal.findAll(CityListItem.class);
        for (CityListItem cityListItem:cityList) {
            CityItem cityItem = new CityItem(cityListItem.getCityName());
            cityItems.add(cityItem);
        }
        adapter = new CityAdapter(cityItems);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view) {
                Intent intent = new Intent(ChooseCity.this, WeatherActivity.class);
                intent.putExtra("weatherId", view.getTag().toString());
                startActivity(intent);
                ActivityCollector.finishAll();
            }

            @Override
            public void onItemLongClick(View view) {
                showPopMenu(view, cityRecyclerView.getChildAdapterPosition(view));
            }
        });
        cityRecyclerView.setAdapter(adapter);
    }

    //删除城市
    public void showPopMenu(View view,final int pos){
        PopupMenu popupMenu = new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_item,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                adapter.removeCity(pos);
                LitePal.delete(CityListItem.class, pos + 1);
                return false;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                //Toast.makeText(getApplicationContext(), "关闭PopupMenu", Toast.LENGTH_SHORT).show();
            }
        });
        popupMenu.show();
    }

}
