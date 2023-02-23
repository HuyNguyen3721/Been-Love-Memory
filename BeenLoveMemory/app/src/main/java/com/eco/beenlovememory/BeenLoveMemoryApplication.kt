package com.eco.beenlovememory
import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

class BeenLoveMemoryApplication : Application() {




    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}