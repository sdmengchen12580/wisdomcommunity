package com.example.mylibrary.httpUtils.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeUtils {

    public static final SimpleDateFormat formatYMD_HMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat formatYMD = new SimpleDateFormat("yy-MM-dd");
    public static final SimpleDateFormat formatYMD_CHINA = new SimpleDateFormat("yyyy年MM月");
    public static final SimpleDateFormat formatYMD_HM = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat formatHM = new SimpleDateFormat("HH:mm");

    //时间处理
    public static String getTimeStart(String startTime) {
        try {
            startTime = (startTime.length() == 7) ? startTime + "000" : startTime;
            long nowTime = System.currentTimeMillis();
            String forS = formatYMD_HMS.format(startTime);
            String forE = formatYMD_HMS.format(nowTime);
            Date dateS = formatYMD_HMS.parse(forS);
            Date dateE = formatYMD_HMS.parse(forE);
            long date = dateS.getTime() - dateE.getTime();
            long num = date / 1000;//总共多少s
            //0-60 s
            if (num < 60) {
                return num + "秒";
            }
            //0-60 min
            num = date / 1000 / 60;//总共多少m
            if (num < 60) {
                return num + "分钟";
            }
            num = date / 1000 / 60 / 60;//总共多少h
            if (num < 24) {
                return num + "小时";
            }
            num = date / 1000 / 60 / 60 / 24;//总共多少d
            if (num < 4) {
                return num + "天";
            } else {
                return formatYMD_CHINA.format(startTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return formatYMD_CHINA.format(startTime);
        }
    }

    //格式化字符串时间
    public static String fotmatStringTime(String timestamp, SimpleDateFormat formatType) {
        timestamp = (timestamp.length() == 7) ? timestamp + "000" : timestamp;
        return formatType.format(Long.parseLong(timestamp));
    }

    //今日时间格式化
    public static String getTimeToday(SimpleDateFormat formatType) {
        Date currentTime = new Date();
        return formatType.format(currentTime);
    }

    //今日时间格式化
    public static String getTimeToday_() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    public static List<String> getDateList(int maxShowDay) {
        //获取今天日期
        String today = getTimeToday_();
        //添加格式  2020-1-截至日期
        List<String> list = new ArrayList<>();
        //本月展示到多少号
        int year = Integer.parseInt(today.split("-")[0]);
        int month = Integer.parseInt(today.split("-")[1]);
        int day = Integer.parseInt(today.split("-")[2]);
        int de_curr_month_day = maxShowDay - day;
        list.add(today);
        log("结束日期" + today + ":剩余天数=" + de_curr_month_day);
        //计算上一个月展示日期的初始时间
        while (de_curr_month_day > 0) {
            month--;
            if (month == 0) {
                month = 12;
                year--;
            }
            String leftMonth = year + "-" + month + "-01";
            int leftMonthDay = getMontyDay(leftMonth);
            int leftDay = de_curr_month_day - leftMonthDay;
            if (leftDay >= 0) {
                de_curr_month_day = leftDay;
                log("本月全展示:" + leftMonth);
                list.add(leftMonth);
            } else {
                de_curr_month_day = -1;
                String StartDay = year + "-" + month + "-" + (Math.abs(leftDay) + 1);
                log("本月为初始月份:" + leftMonth + "    初始日期=" + StartDay);
                list.add(StartDay);
            }
        }
//        list=listOrder(list);
        for (int i = 0; i < list.size(); i++) {
            log("遍历集合" + i + "=" + list.get(i));
        }
        return list;
    }

    //获取每月多少天 getMontyDay("2018-06-07")
    public static int getMontyDay(String dateTime) {
        String data[] = dateTime.split("-");
        int day = 0;
        if (data.length == 3) {
            int year = Integer.parseInt(data[0]);
            int month = Integer.parseInt(data[1]);
            /*平年的2月是28天,闰年2月是29天.
            4月、6月、9月、11月各是30天..
            1月、3月、5月、7月、8月、10月、12月各是31天*/
            if (month == 4 || month == 6 || month == 9 || month == 11) {
                day = 30;
            } else if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
                day = 31;
            } else {
                if (year % 4 == 0) {
                    day = 29;
                } else {
                    day = 28;
                }
            }
        }
        return day;
    }

    //将集合元素倒置
    public static List<String> listOrder(List<String> list) {
        List<String> list1 = new ArrayList<>();
        if (list.size() > 1) {
            for (int i = list.size() - 1; i >= 0; i--) {
                list1.add(list.get(i));
            }
        } else {
            list1 = list;
        }
        return list1;
    }

    //获取指定日期是星期几  getDayofWeek("2018-06-07")   返回星期日
    public static int getDayofWeek(String dateTime) {
        Calendar cal = Calendar.getInstance();
        if (dateTime.equals("")) {
            cal.setTime(new Date(System.currentTimeMillis()));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date;
            try {
                date = sdf.parse(dateTime);
            } catch (ParseException e) {
                date = null;
                e.printStackTrace();
            }
            if (date != null) {
                cal.setTime(new Date(date.getTime()));
            }
        }
        return cal.get(Calendar.DAY_OF_WEEK) - 1;
    }

    private static void log(String tex) {
        LogUtils.logE("日期----------:", tex);
    }
}
