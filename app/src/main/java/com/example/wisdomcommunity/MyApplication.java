package com.example.wisdomcommunity;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.DisplayMetrics;
import com.example.mylibrary.httpUtils.utils.AppUtils;

public class MyApplication extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setDefaultDisplay(this);
    }

    //不受系统显示大小影响
    public void setDefaultDisplay(Context context) {
        if (Build.VERSION.SDK_INT > 23) {
            Configuration origConfig = context.getResources().getConfiguration();
            origConfig.densityDpi = AppUtils.getDefaultDisplayDensity();//获取手机出厂时默认的densityDpi
            context.getResources().updateConfiguration(origConfig, context.getResources().getDisplayMetrics());
        }
    }

    //不受系统显示大小影响
    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        Configuration newConfig = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (resources != null && newConfig.fontScale != 1) {
            newConfig.fontScale = 1;
            if (Build.VERSION.SDK_INT >= 17) {
                Context configurationContext = createConfigurationContext(newConfig);
                resources = configurationContext.getResources();
                displayMetrics.scaledDensity = displayMetrics.density * newConfig.fontScale;
            } else {
                resources.updateConfiguration(newConfig, displayMetrics);
            }
        }
        return resources;
    }
}
