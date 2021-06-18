package com.example.mylibrary.httpUtils.view;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mylibrary.R;

public class CustomDialog extends Dialog {

    private String content;
    private ImageView img_roat;
    private RotateAnimation rotate;

    public CustomDialog(Context context, String content) {
        super(context, R.style.CustomDialog);
        this.content = content;
        initView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (CustomDialog.this.isShowing())
                    CustomDialog.this.dismiss();
                break;
        }
        return true;
    }

    public void setContent(String content) {
        findViewById(R.id.tvcontent).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.tvcontent)).setText(content);
        if(rotate==null){
            initAnim();
        }
        img_roat.setAnimation(rotate);
    }

    private void initView() {
        setContentView(R.layout.dialog_view);
        img_roat = findViewById(R.id.img_roat);
        if (this.content == null) {
            findViewById(R.id.tvcontent).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.tvcontent)).setText(content);
        }
        setCanceledOnTouchOutside(true);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.alpha = 0.8f;
        getWindow().setAttributes(attributes);
        setCancelable(false);
        //初始动画
        initAnim();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(rotate!=null&&img_roat!=null){
            rotate.cancel();
            rotate = null;
        }
    }

    private void initAnim(){
        rotate  = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(2000);//设置动画持续周期
        rotate.setRepeatCount(-1);//设置重复次数
        rotate.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        rotate.setStartOffset(10);//执行前的等待时间
    }
}
