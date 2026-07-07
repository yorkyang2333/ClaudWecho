package com.yorkyang2333.claudwecho

import android.app.Application
import com.yorkyang2333.claudwecho.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

import com.yorkyang2333.claudwecho.di.playerModule

import org.koin.android.ext.android.inject

class ClaudWechoApp : Application(), coil.ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@ClaudWechoApp)
            modules(networkModule, playerModule)
        }
    }

    override fun newImageLoader(): coil.ImageLoader {
        val okHttpClient: okhttp3.OkHttpClient by inject()
        return coil.ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .build()
    }
}
