package com.yorkyang2333.claudwecho

import android.app.Application
import com.yorkyang2333.claudwecho.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

import com.yorkyang2333.claudwecho.di.playerModule

import org.koin.android.ext.android.inject

import com.yorkyang2333.claudwecho.ui.utils.SongInfoTag
import com.yorkyang2333.claudwecho.ui.utils.toLowResImageUrl

class LowResImageInterceptor : coil.intercept.Interceptor {
    override suspend fun intercept(chain: coil.intercept.Interceptor.Chain): coil.request.ImageResult {
        val request = chain.request
        val data = request.data
        if (data is String && request.tags.tag(SongInfoTag::class.java) == null) {
            val lowResUrl = toLowResImageUrl(data)
            val newRequest = request.newBuilder()
                .data(lowResUrl)
                .memoryCacheKey(lowResUrl)
                .diskCacheKey(lowResUrl)
                .build()
            return chain.proceed(newRequest)
        }
        return chain.proceed(request)
    }
}

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
            .components {
                add(LowResImageInterceptor())
            }
            .build()
    }
}
