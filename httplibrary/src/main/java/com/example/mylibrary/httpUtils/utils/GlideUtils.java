package com.example.mylibrary.httpUtils.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

public class GlideUtils {

    //圆形预览头像  fixme 加载外部链接url，转义处理
    public static void glideImaNoDeal(Context context, String imgurl, final ImageView img) {
        imgurl = imgurl.replaceAll("amp;", "");//被转义了
        Glide.with(context.getApplicationContext())
                .load(imgurl)
                .into(img);
    }

    //带渐变的展示
    public static void glideBannerImg(Context context, String imgurl, ImageView img) {
        Glide.with(context.getApplicationContext())
                .load(imgurl)
                .transition(DrawableTransitionOptions.withCrossFade(500)) // 渐变
//                .placeholder(R.drawable.)
//                .error(R.mipmap.ic_launcher)
                .fitCenter()
                .into(img);
    }

    //带错误+预览
    public static void glideImgPreError(Context context, String imgurl, ImageView img) {
        Glide.with(context.getApplicationContext())
                .load(imgurl)
                .transition(DrawableTransitionOptions.withCrossFade(500)) // 渐变
//                .placeholder(R.drawable.)
//                .error(R.mipmap.ic_launcher)
                .fitCenter()
                .into(img);
    }
}
