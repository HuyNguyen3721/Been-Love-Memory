package com.eco.beenlovememory.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtils {
    var REQUEST_CODE = 10101

    fun requestRuntimePermission(
        activity: Activity?, requestCode: Int = REQUEST_CODE,
        vararg permissions: String?
    ) {
        REQUEST_CODE = requestCode
        if (!checkPermissionAccept(activity, *permissions)) {
            ActivityCompat.requestPermissions(activity!!, permissions, requestCode)
        }
    }

    fun checkPermissionAccept(context: Context?, vararg permissions: String?): Boolean {
        for (permissionName in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    permissionName!!
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun checkGranted(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}