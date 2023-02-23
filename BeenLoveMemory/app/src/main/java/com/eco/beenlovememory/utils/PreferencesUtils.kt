package com.eco.beenlovememory.utils

import android.content.Context
import android.content.SharedPreferences

object  PreferencesUtils {
    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    private fun editor(): SharedPreferences.Editor {
        return sharedPreferences!!.edit()
    }

    fun putBoolean(key: String?, value: Boolean) {
        editor().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String?, defaultvalue: Boolean): Boolean {
        return sharedPreferences!!.getBoolean(key, defaultvalue)
    }

    fun getBoolean(key: String?): Boolean {
        return getBoolean(key, false)
    }

    fun putString(key: String?, value: String?) {
        editor().putString(key, value).apply()
    }

    fun getString(key: String?): String? {
        return getString(key, "")
    }

    fun getString(key: String?, defaultValue: String?): String? {
        return sharedPreferences!!.getString(key, defaultValue)
    }

    fun putInteger(key: String?, value: Int) {
        editor().putInt(key, value).apply()
    }

    fun getInteger(key: String?): Int {
        return sharedPreferences!!.getInt(key, 0)
    }

    fun putFloat(key: String?, value: Float) {
        editor().putFloat(key, value).apply()
    }

    fun getFloat(key: String?): Float {
        return sharedPreferences!!.getFloat(key, 0f)
    }


    fun getInteger(key: String?, defaultValue: Int): Int {
        return sharedPreferences!!.getInt(key, defaultValue)
    }

    fun putLong(key: String?, value: Long) {
        editor().putLong(key, value).apply()
    }

    fun getLong(key: String?): Long {
        return sharedPreferences!!.getLong(key, 0)
    }

    fun getLong(key: String?, defaultValue: Long): Long {
        return sharedPreferences!!.getLong(key, defaultValue)
    }
}