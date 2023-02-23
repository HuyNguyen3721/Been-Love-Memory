package com.eco.beenlovememory.base

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.eco.beenlovememory.R
import com.eco.beenlovememory.utils.FileUtils


abstract class BaseAdapter<T, VH : RecyclerView.ViewHolder>(
    context: Context,
    list: MutableList<T>
) :
    RecyclerView.Adapter<VH>() {
    var mContext: Context = context
    var list: MutableList<T> = mutableListOf()
    val fileUtils: FileUtils by lazy { FileUtils() }
    val layoutInflater: LayoutInflater by lazy {
        LayoutInflater.from(context)
    }
    var listenerDelete: ((Int) -> Unit)? = null
    var listenerRename: ((Int) -> Unit)? = null
    var listenerShare: ((Int) -> Unit)? = null

    init {
        this.list = list
    }

    override fun getItemCount(): Int {
        return list.size
    }

    abstract override fun onBindViewHolder(holder: VH, position: Int)

    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH

    open fun animTouch(view: View) {
        val anim = AnimationUtils.loadAnimation(mContext, R.anim.anim_touch_view)
        view.startAnimation(anim)
    }

    open fun animDisTouch(view: View, endAnim: () -> Unit) {
        val anim = AnimationUtils.loadAnimation(mContext, R.anim.anim_distouch_view)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                endAnim.invoke()
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

        })
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
                    animDisTouch(view) {
                        endAmin.invoke()
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    animDisTouch(view) {
                    }
                }
            }
            true
        }
    }

    open fun setMrgForItemEdit(
        position: Int,
        layoutItem: ConstraintLayout,
        isStart: Boolean,
        isEnd: Boolean
    ) {
//        if (position == 0 && !isStart) {
//            setLayoutParams(
//                layoutItem,
//                com.intuit.sdp.R.dimen._7sdp,
//                com.intuit.sdp.R.dimen._5sdp
//            )
//        } else if (position == list.size - 1 && !isEnd) {
//            setLayoutParams(
//                layoutItem, 0, com.intuit.sdp.R.dimen._7sdp
//            )
//        }
    }


    open fun setLayoutParams(view: View, start: Int, end: Int) {
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        layoutParams.setMargins(
            if (start == 0) 0 else mContext.resources.getDimensionPixelSize(start),
            0,
            if (end == 0) 0 else mContext.resources.getDimensionPixelSize(end),
            0
        )
        view.layoutParams = layoutParams
        view.invalidate()
    }

    fun animRotateCycle(view: View, duration: Long = 4000) {
        view.clearAnimation()
        val rotate = RotateAnimation(
            0F, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = duration
        rotate.interpolator = LinearInterpolator()
        rotate.repeatCount = Animation.INFINITE
        view.animation = rotate
    }

}