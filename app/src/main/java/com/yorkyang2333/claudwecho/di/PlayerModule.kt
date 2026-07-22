package com.yorkyang2333.claudwecho.di

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheEvictor
import androidx.media3.datasource.cache.CacheSpan
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.File
import java.util.TreeSet

class DynamicLruCacheEvictor(
    @Volatile var maxBytes: Long
) : CacheEvictor {
    private val leastRecentlyUsed = TreeSet<CacheSpan> { a, b ->
        if (a.lastTouchTimestamp != b.lastTouchTimestamp) {
            if (a.lastTouchTimestamp < b.lastTouchTimestamp) -1 else 1
        } else {
            a.compareTo(b)
        }
    }
    private var currentSize: Long = 0

    override fun requiresCacheSpanTouches(): Boolean = true

    override fun onCacheInitialized() {}

    override fun onStartFile(cache: Cache, key: String, position: Long, length: Long) {
        if (length != -1L) {
            evictCache(cache, length)
        }
    }

    override fun onSpanAdded(cache: Cache, span: CacheSpan) {
        leastRecentlyUsed.add(span)
        currentSize += span.length
        evictCache(cache, 0)
    }

    override fun onSpanRemoved(cache: Cache, span: CacheSpan) {
        leastRecentlyUsed.remove(span)
        currentSize -= span.length
    }

    override fun onSpanTouched(cache: Cache, oldSpan: CacheSpan, newSpan: CacheSpan) {
        onSpanRemoved(cache, oldSpan)
        onSpanAdded(cache, newSpan)
    }

    private fun evictCache(cache: Cache, requiredBytes: Long) {
        while (currentSize + requiredBytes > maxBytes && leastRecentlyUsed.isNotEmpty()) {
            val item = leastRecentlyUsed.first()
            try {
                cache.removeSpan(item)
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}

val playerModule = module {
    single {
        val context: Context = androidContext()
        val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val limitMb = prefs.getInt("audio_cache_limit_mb", 500)
        DynamicLruCacheEvictor(limitMb * 1024 * 1024L)
    }

    single {
        val context: Context = androidContext()
        val cacheDir = File(context.cacheDir, "media_cache")
        val evictor: DynamicLruCacheEvictor = get()
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
