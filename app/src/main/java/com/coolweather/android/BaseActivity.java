package com.coolweather.android;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * 这个类主要用于销毁活动
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);//将活动添加到活动收集器
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        ActivityCollector.removeActivity(this);//将活动移除活动收集器
    }
}
