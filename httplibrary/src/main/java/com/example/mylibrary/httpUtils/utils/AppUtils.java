package com.example.mylibrary.httpUtils.utils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class AppUtils {

    //changeAct
    public static void changeAct(Activity activity, Bundle bundle, Class<?> toClass) {
        Intent intent = new Intent();
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        intent.setClass(activity, toClass);
        activity.startActivity(intent);
    }

    //底部间距
    public static void setMarginBottom(ViewGroup rl_chat_layout, int px) {
        ViewGroup.LayoutParams vp = rl_chat_layout.getLayoutParams();
        if (vp instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) vp).bottomMargin = px;
            rl_chat_layout.setLayoutParams(vp);
        }
    }

    //字体不受系统显示大小影响
    public static void setDefaultDisplay(Context context) {
        if (Build.VERSION.SDK_INT > 23) {
            Configuration origConfig = context.getResources().getConfiguration();
            origConfig.densityDpi = getDefaultDisplayDensity();//获取手机出厂时默认的densityDpi
            context.getResources().updateConfiguration(origConfig, context.getResources().getDisplayMetrics());
        }
    }

    //获取手机出厂时默认的densityDpi
    public static int getDefaultDisplayDensity() {
        try {
            Class<?> aClass = Class.forName("android.view.WindowManagerGlobal");
            Method method = aClass.getMethod("getWindowManagerService");
            method.setAccessible(true);
            Object iwm = method.invoke(aClass);
            Method getInitialDisplayDensity = iwm.getClass().getMethod("getInitialDisplayDensity", int.class);
            getInitialDisplayDensity.setAccessible(true);
            Object densityDpi = getInitialDisplayDensity.invoke(iwm, Display.DEFAULT_DISPLAY);
            return (int) densityDpi;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    //152****6488
    public static String getPhoneHide(String phone) {
        if (phone.equals("")) {
            return "";
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    //身份证隐藏
    public static String getSfzHide(String sfz) {
        if (sfz.equals("")) {
            return "暂未认证";
        }
        return sfz.replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1****$2");
    }

    //0000 **** **** 0000
    public static String getBankNumHide(String bankNum) {
        String bankNumReturn = "";
        if (bankNum.length() == 0 || bankNum.length() < 4) {
            return "";
        }
        bankNumReturn = bankNum.substring(0, 4) + " **** **** " + bankNum.substring(bankNum.length() - 4);
        return bankNumReturn;
    }

    //123123123****
    public static String getCarHide(String car) {
        if (car.equals("") && car.length() < 4) {
            return "0000";
        }
        return car.substring(car.length() - 4);
    }

    //使用高德线路
    public static Uri jumpGaoDe(String la, String lo, String name) {
//        Uri uri = Uri.parse("amapuri://openFeature?featureName=OnRideNavi&rideType=elebike&sourceApplication="
//                + "酷跑" + "&lat=" + la + "&lon=" + lo + "&dev=0");
        Uri uri = Uri.parse("amapuri://route/plan/?dlat=" + la + "&dlon=" + lo + "&dname=" + name + "&dev=0&t=3");
        //t = 0（驾车）= 1（公交）= 2（步行）= 3（骑行）= 4（火车）= 5（长途客车）
        return uri;
    }

    //使用百度线路
    public static Uri jumpBaidu(String la, String lo, String name) {
        //transit、driving、navigation、walking，riding分别表示公交、驾车、导航、步行和骑行
        Uri uri = Uri.parse("baidumap://map/direction?destination=latlng:" + la + "," + lo + "|name:" + name + "&mode=riding");
        return uri;
    }

    //验证手机格式
    public static boolean isMobileNO(String mobiles) {
        /*
         第二位：3 4 5 7 8
         * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
         */
        String telRegex = "[1][3456789]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (mobiles.equals(""))
            return false;
        else
            return mobiles.matches(telRegex);
    }

    //必须同时包含大小写字母及数字
    public static boolean isContainAll(String str) {
        boolean isDigit = false;//定义一个boolean值，用来表示是否包含数字
        boolean isLowerCase = false;//定义一个boolean值，用来表示是否包含字母
        boolean isUpperCase = false;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {   //用char包装类中的判断数字的方法判断每一个字符
                isDigit = true;
            } else if (Character.isLowerCase(str.charAt(i))) {  //用char包装类中的判断字母的方法判断每一个字符
                isLowerCase = true;
            } else if (Character.isUpperCase(str.charAt(i))) {
                isUpperCase = true;
            }
        }
        String regex = "^[a-zA-Z0-9]+$";
        boolean isRight = isDigit && (isLowerCase || isUpperCase) && str.matches(regex);
        return isRight;
    }

    //获取屏幕宽度(px)
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    //获取屏幕高度(px)
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    //对key进行md5加密
    public static String getMD5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    //处理数字，大于10000，显示1W
    public static String dealNum(String n) {
        int number = Integer.parseInt(n);
        String str = "";
        if (number <= 0) {
            str = "0";
        } else if (number < 10000) {
            str = number + "";
        } else {
            double d = (double) number;
            double num = d / 10000;//1.将数字转换成以万为单位的数字
            BigDecimal b = new BigDecimal(num);
            double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();//2.转换后的数字四舍五入保留小数点后一位;
            str = f1 + "万";
        }
        return str;
    }

    //打印数组
    public static void logArray(Object[] array, String logTex) {
        for (int i = 0; i < array.length; i++) {
            LogUtils.logE(logTex, "数据" + i + "=" + array[i]);
        }
    }

    //打印集合
    public static void logList(List<String> list, String logTex) {
        for (int i = 0; i < list.size(); i++) {
            LogUtils.logE(logTex, "数据" + i + "=" + list.get(i));
        }
    }

    //获取版本名称
    public static String getVersionName(Context context) {
        //获取包管理器
        PackageManager pm = context.getPackageManager();
        //获取包信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            //返回版本号
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取版本号
    public static int getVersionCode(Context context) {
        //获取包管理器
        PackageManager pm = context.getPackageManager();
        //获取包信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            //返回版本号
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //获取App的名称
    public static String getAppName(Context context) {
        PackageManager pm = context.getPackageManager();
        //获取包信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            //获取应用 信息
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            //获取albelRes
            int labelRes = applicationInfo.labelRes;
            //返回App的名称
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isHuaWei_Vivo() {
        String manufacturer = Build.MANUFACTURER;
        if ("huawei".equalsIgnoreCase(manufacturer)) {
            return true;
        }
        return false;
    }

    public static boolean isMEIZU() {
        String manufacturer = Build.MANUFACTURER;
        if ("meizu".equalsIgnoreCase(manufacturer)) {
            return true;
        }
        return false;
    }
}
