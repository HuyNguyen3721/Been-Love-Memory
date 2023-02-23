package com.eco.beenlovememory.di

import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module


val appModule = module {
}
// Khoi tao "Module" cần add Module vào đây :
val listModule = listOf(
    appModule,
)