package com.yorkyang2333.claudwecho.di

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.File

val playerModule = module {
    single {
        val context: Context = androidContext()
        val cacheDir = File(context.cacheDir, "media_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(200 * 1024 * 1024) // 200MB max cache
        val databaseProvider = StandaloneDatabaseProvider(context)
        
        SimpleCache(cacheDir, evictor, databaseProvider)
    }

    single {
        val context: Context = androidContext()
        val cache: SimpleCache = get()
        val okHttpClient: okhttp3.OkHttpClient = get()
        val httpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
            
        CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    single {
        val context: Context = androidContext()
        val cacheDataSourceFactory: CacheDataSource.Factory = get()
        val repository: com.yorkyang2333.claudwecho.data.MainRepository = get()
        
        val urlCache = java.util.concurrent.ConcurrentHashMap<Long, Pair<String, Long>>()
        val urlCacheTtlMs = 30 * 60 * 1000L // 30 minutes

        val resolvingDataSourceFactory = androidx.media3.datasource.ResolvingDataSource.Factory(
            cacheDataSourceFactory,
            androidx.media3.datasource.ResolvingDataSource.Resolver { dataSpec ->
                val uri = dataSpec.uri
                if (uri.scheme == "netease") {
                    val id = uri.lastPathSegment?.toLongOrNull()
                    if (id != null) {
                        val now = System.currentTimeMillis()
                        val cached = urlCache[id]
                        val finalUrl = if (cached != null && (now - cached.second) < urlCacheTtlMs) {
                            cached.first
                        } else {
                            var actualUrl: String? = null
                            kotlinx.coroutines.runBlocking {
                                actualUrl = repository.getSongUrl(id)
                            }
                            val resolved = actualUrl ?: "https://music.163.com/song/media/outer/url?id=${id}.mp3"
                            urlCache[id] = Pair(resolved, now)
                            android.util.Log.d("ResolvingDataSource", "Resolved URL for $id to $resolved")
                            resolved
                        }
                        return@Resolver dataSpec.buildUpon()
                            .setUri(android.net.Uri.parse(finalUrl))
                            .setKey(dataSpec.key ?: id.toString())
                            .build()
                    }
                }
                dataSpec
            }
        )
        
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(resolvingDataSourceFactory))
            .build()
    }
}
