package com.eco.videorecorder.screenrecorder.lite.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseDialog<T : ViewDataBinding?> {

    private var layoutBinding: T? = null
    private lateinit var viewRoot: View
    private var dialog: Dialog? = null


    open fun create() {
        if (dialog == null) {
            dialog = onCreate()
        }
        this.initView()
    }


     private fun onCreate(): Dialog {
        if (context() == null) {
            throw Exception("Context Null")
        }
        if (styleDialog() == null) {
            this.dialog = context()?.let { Dialog(it) }
        } else {
            this.dialog = context()?.let { Dialog(it, styleDialog()!!) }
        }

        val view = context()?.let {
            LayoutInflater.from(it)
        }?.inflate(this.initLayout(), null)

        this.layoutBinding = view?.let { DataBindingUtil.bind<T>(it) }

        this.viewRoot = this.layoutBinding!!.root

        val window: Window = this.dialog!!.window!!
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.requestFeature(Window.FEATURE_NO_TITLE)

        this.dialog!!.setContentView(this.viewRoot)

        this.dialog!!.setCanceledOnTouchOutside(true)

        if (disableBack()) {
            this.dialog!!.setCancelable(false)
        }
        if (fullDialog()) {
            window.setLayout(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
        } else {
            window.setLayout(
                getWidth(), getHeight()

            )
        }
        return this.dialog!!
    }

    protected abstract fun context(): Context?


    @LayoutRes
    protected abstract fun initLayout(): Int


    @StyleRes
  open  fun styleDialog(): Int? {
        return null
    }


    open fun fullDialog(): Boolean {
        return false
    }


    open  fun disableBack(): Boolean {
        return false
    }

    open   fun get(): Dialog? {
        return dialog
    }


    open fun initView() {

    }

    open fun showDialog() {
        if (dialog?.isShowing!!) {
            return
        }
        this.dialog?.show()
    }


    open  fun dismiss() {
        if (!dialog?.isShowing!!) {
            return
        }
        dialog!!.cancel()
    }

    open fun getWidth() = 0
    open fun getHeight() = 0

}