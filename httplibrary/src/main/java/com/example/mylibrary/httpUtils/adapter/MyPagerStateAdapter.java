package com.example.mylibrary.httpUtils.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * 不使用懒加载的viewpager使用的adapter
 */
public class MyPagerStateAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragments;

    public MyPagerStateAdapter(FragmentManager fm, List<Fragment> fragments) {
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
