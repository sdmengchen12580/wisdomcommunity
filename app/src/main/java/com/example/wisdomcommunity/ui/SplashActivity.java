package com.example.wisdomcommunity.ui;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import com.example.mylibrary.httpUtils.utils.AppUtils;
import com.example.wisdomcommunity.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AppUtils.changeAct(SplashActivity.this, null, MainActivity.class);
                finish();
            }
        }, 1500);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            return true;//super.onKeyDown(keyCode, event)
        }
        return super.onKeyDown(keyCode, event);
    }
}
