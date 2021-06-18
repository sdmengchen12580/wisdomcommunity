package com.example.mylibrary.httpUtils.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * 使用懒加载的viewpager使用的adapter
 */
public class MyAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments;

    public MyAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        int ret = 0;
        if (fragments != null && fragments.size() != 0) {
            ret = fragments.size();
        }
        return ret;
    }
}
