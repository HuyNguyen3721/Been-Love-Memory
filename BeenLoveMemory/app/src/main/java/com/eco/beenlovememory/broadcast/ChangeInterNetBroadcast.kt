package com.eco.beenlovememory.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager



class ChangeInterNetBroadcast(
    private var context: Context,
    private val listener: ChangeInternetListener?
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        if (isNetworkAvailable(context)) {
            listener?.onInternetConnected()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    interface ChangeInternetListener {
        fun onInternetConnected()
    }
}
