package com.example.stocksApp;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasySplashScreen config = new EasySplashScreen(SplashScreen.this)
                .withTargetActivity(MainActivity.class).withSplashTimeOut(4000)
                .withBackgroundColor(Color.parseColor("#f2f2f4"))
                .withLogo(R.mipmap.ic_launcher_icon);
        View view = config.create();
        setContentView(view);
    }
}
