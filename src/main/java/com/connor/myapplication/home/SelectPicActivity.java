package com.connor.myapplication.home;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.connor.myapplication.R;
import com.connor.myapplication.data.Constant;

public class SelectPicActivity extends AppCompatActivity {

    private Bundle bundle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_pic);
        //获取屏幕宽高，设置背景纹理大小用
        DisplayMetrics dm =getResources().getDisplayMetrics();
        Constant.ScreenWidth = dm.widthPixels;
        Constant.ScreenHeight = dm.heightPixels;

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


}
