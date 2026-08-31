package com.yorkyang2333.claudwecho

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import com.yorkyang2333.claudwecho.di.networkModule
import com.yorkyang2333.claudwecho.di.playerModule
import com.yorkyang2333.claudwecho.service.PlaybackService
import com.yorkyang2333.claudwecho.ui.utils.SongInfoTag
import com.yorkyang2333.claudwecho.ui.utils.toLowResImageUrl
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LowResImageInterceptor : coil.intercept.Interceptor {
    override suspend fun intercept(chain: coil.intercept.Interceptor.Chain): coil.request.ImageResult {
        val request = chain.request
        val data = request.data
        if (data is String && request.tags.tag(SongInfoTag::class.java) == null) {
            val lowResUrl = toLowResImageUrl(data)
            val newRequest = request.newBuilder()
                .data(lowResUrl)
                .build()
            return chain.proceed(newRequest)
        }
        return chain.proceed(request)
    }
}

class ClaudWechoApp : Application(), coil.ImageLoaderFactory {
    companion object {
        private val activeActivities = mutableSetOf<Activity>()

        fun exitApplication(context: Context) {
            Handler(Looper.getMainLooper()).post {
                activeActivities.forEach { activity ->
                    try {
                        activity.finishAffinity()
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                activeActivities.clear()
                try {
                    context.stopService(Intent(context, PlaybackService::class.java))
                } catch (e: Exception) {
                    // ignore
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    Process.killProcess(Process.myPid())
                    kotlin.system.exitProcess(0)
                }, 150)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activeActivities.add(activity)
            }
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                activeActivities.remove(activity)
            }
        })

        startKoin {
            androidContext(this@ClaudWechoApp)
            modules(networkModule, playerModule)
        }
    }

    override fun newImageLoader(): coil.ImageLoader {
        val okHttpClient: okhttp3.OkHttpClient by inject()
        return coil.ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .components {
                add(LowResImageInterceptor())
            }
            .build()
    }
}
