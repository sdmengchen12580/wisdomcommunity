package com.example.wisdomcommunity.ui;

import android.os.Bundle;
import com.example.mylibrary.httpUtils.base.BaseActivity;
import com.example.wisdomcommunity.R;

public class MainActivity extends BaseActivity {

    @Override
    public boolean isStatusBarTextBlackColor() {
        return false;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {

    }

    @Override
    public void initClick() {

    }
}
