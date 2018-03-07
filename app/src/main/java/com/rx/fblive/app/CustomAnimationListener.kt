package com.rx.fblive.app

import android.view.animation.Animation

/**
 * Created by Albert-IM on 07/03/2018.
 */
class CustomAnimationListener: Animation.AnimationListener {
    override fun onAnimationRepeat(animation: Animation?) {

    }

    override fun onAnimationEnd(animation: Animation?) {
        System.out.print("On Animation End")
    }

    override fun onAnimationStart(animation: Animation?) {
        System.out.print("On Animation Start")
    }
}