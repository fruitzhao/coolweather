package com.coolweather.android;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.githang.statusbar.StatusBarCompat;

public class Setting extends AppCompatActivity {

    private TextView introButton;

    private TextView updateFre;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //设置状态栏颜色
        int color = Color.parseColor("#6a92ca");
        StatusBarCompat.setStatusBarColor(this, color);

        introButton = findViewById(R.id.intro);
        introButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Setting.this);
                dialog.setTitle("开发者信息");
                dialog.setMessage("微博 ：爱吃水果的学长\n邮箱 ：zhaofei@bupt.edu.cn");
                dialog.show();
            }
        });

        updateFre = findViewById(R.id.update_fre);
        updateFre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSingleAlertDialog(v);
            }
        });
    }

    public void showSingleAlertDialog(View view){
        final String[] items = {"每三小时", "每六小时", "每天", "从不"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("更新频率");
        alertBuilder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });

        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });

        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

}
