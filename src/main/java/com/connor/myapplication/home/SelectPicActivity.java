package com.connor.myapplication.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;

import com.connor.myapplication.R;
import com.connor.myapplication.data.Constant;

public class SelectPicActivity extends AppCompatActivity {

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_pic);
 //       calculateSurfaceSize();

        bundle = new Bundle();
    }

    public void vertical(View view) {
        bundle.putInt("id", R.drawable.vertical);
        Intent intent = new Intent(SelectPicActivity.this, MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void landscape(View view) {
        bundle.putInt("id", R.drawable.landscape);
        Intent intent = new Intent(SelectPicActivity.this, MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

/*
    *//**
     * 通过布局中weight简单计算出SurfaceView宽高和获取屏幕宽高
     * 获取屏幕宽高，设置背景纹理大小用
     *//*
    private void calculateSurfaceSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Constant.ScreenWidth = dm.widthPixels;
        Constant.ScreenHeight = dm.heightPixels;
        Constant.mSurfaceViewWidth = dm.widthPixels;
        Constant.mSurfaceViewHeight = (int) ((dm.heightPixels - getStatusHeight()) * 0.9f);
    }

    *//**
     * 获取通知栏高度
     *//*
    private int getStatusHeight() {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }*/
}
