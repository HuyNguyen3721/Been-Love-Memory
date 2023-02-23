package com.eco.beenlovememory.startup
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.startup.Initializer
import com.eco.beenlovememory.di.listModule
import com.eco.beenlovememory.utils.PreferencesUtils
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WorkManagerInitializer : Initializer<String> {
    override fun create(context: Context): String {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        //koin
        setupKoin(context)
        //app flyer
//        initToStartAppsFlyer(context)
//        FirebaseApp.initializeApp(context)
//        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        //ad revenue
        //tracker
//        AnalyticsManager.getInstance().setFireBaseEnable(context)
        //
        PreferencesUtils.init(context)
        //Hawk
//        MobileAds.initialize(this) { }
        //set up fan
//        AudienceNetworkAds.initialize(context)
//        if (BuildConfig.DEBUG) {
//            AdsDeviceIdUtils.instance.generateDeviceId(context)
//            StrictMode.setThreadPolicy(
//                StrictMode.ThreadPolicy.Builder()
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectNetwork() // or .detectAll() for all detectable problems
//                    .penaltyLog()
//                    .build()
//            )
//        }
        return ""
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
    private fun setupKoin(context: Context) {
        startKoin {
            androidContext(context)
            modules(listModule)
        }
    }
}
