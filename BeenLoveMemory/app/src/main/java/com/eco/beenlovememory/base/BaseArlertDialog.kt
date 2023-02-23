package com.eco.beenlovememory.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import com.eco.beenlovememory.R

abstract class BaseAlertDialog(context: Context, style: Int) : AlertDialog(context, style) {
    private var lastTimeClicked: Long = 0

    open fun animTouch(view: View) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.anim_touch_view)
        view.startAnimation(anim)
    }

    open fun animDisTouch(view: View) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.anim_distouch_view)
        view.startAnimation(anim)
    }

    @SuppressLint("ClickableViewAccessibility")
    open fun animClickScaleView(view: View, endAmin: () -> Unit) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    animTouch(view)
                }
                MotionEvent.ACTION_UP -> {
                    animDisTouch(view)
                    endAmin.invoke()
                }
                MotionEvent.ACTION_MOVE -> {
                }
            }
            true
        }
    }

    val isDoubleClick: Boolean
        get() {
            if (SystemClock.elapsedRealtime() - lastTimeClicked < 500) {
                return true
            }
            lastTimeClicked = SystemClock.elapsedRealtime()
            return false
        }
}