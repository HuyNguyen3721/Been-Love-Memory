package com.eco.beenlovememory.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.*
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.eco.beenlovememory.R
import com.eco.beenlovememory.utils.FileUtils
import com.eco.beenlovememory.utils.PermissionUtils


abstract class BaseFragment<B : ViewBinding> : Fragment() {
    lateinit var binding: B
    private var lastTimeClicked: Long = 0
    private var countTimer: CountDownTimer? = null
    val fileUtils by lazy { FileUtils() }
    val permissionUtils: PermissionUtils by lazy { PermissionUtils() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, null)
        initData()
        initView()
        initListener()
        observable()
    }

    abstract fun observable()
    protected abstract fun initData()
    protected abstract fun initView()
    protected abstract fun initListener()
    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): B
    private val baseActivity: BaseActivity<*>?
        get() = activity as BaseActivity<*>?

    fun toast(content: String?) {
        baseActivity?.toast(content)
    }

    fun isDoubleClick(): Boolean {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < 500) {
            return true
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        return false
    }

    open fun animTouch(view: View) {
        val anim = AnimationUtils.loadAnimation(activity, R.anim.anim_touch_view)
        view.startAnimation(anim)
    }

    open fun animDisTouch(view: View) {
        val anim = AnimationUtils.loadAnimation(activity, R.anim.anim_distouch_view)
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

    fun showCountTimer(totalTime: Long, onTick: (String, Int) -> Unit, onFinish: () -> Unit) {
        countTimer?.cancel()
        countTimer = object : CountDownTimer(totalTime, (totalTime / 100)) {
            override fun onTick(millisUntilFinished: Long) {
                val percent = (((totalTime - millisUntilFinished) / totalTime.toFloat()) * 100).toInt()
                onTick.invoke("${percent}%", percent)
            }

            override fun onFinish() {
                onFinish.invoke()
            }
        }.start()
    }

    fun cancelCountTimer() {
        countTimer?.cancel()
    }
}