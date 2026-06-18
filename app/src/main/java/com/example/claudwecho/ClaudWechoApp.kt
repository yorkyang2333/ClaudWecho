package com.example.claudwecho

import android.app.Application
import com.example.claudwecho.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ClaudWechoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@ClaudWechoApp)
            modules(networkModule)
        }
    }
}
