package com.example.mylibrary.httpUtils.callback;

import android.view.animation.Animation;

public abstract class AnimEndCallback implements Animation.AnimationListener {

    public abstract void endAnim();

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        endAnim();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
