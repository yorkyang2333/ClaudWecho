package com.yorkyang2333.claudwecho.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope

import com.yorkyang2333.claudwecho.di.DynamicLruCacheEvictor

class SettingsViewModel(
    private val context: Context,
    private val cacheEvictor: DynamicLruCacheEvictor? = null
) : ViewModel() {
    private val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _screenShape = MutableStateFlow(prefs.getString("screen_shape", "auto") ?: "auto")
    val screenShape: StateFlow<String> = _screenShape.asStateFlow()

    private val _keepScreenOn = MutableStateFlow(prefs.getBoolean("keep_screen_on", false))
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    private val _audioCacheLimitMb = MutableStateFlow(prefs.getInt("audio_cache_limit_mb", 500))
    val audioCacheLimitMb: StateFlow<Int> = _audioCacheLimitMb.asStateFlow()

    private val _cacheSize = MutableStateFlow("0 MB")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    private val _apiBaseUrl = MutableStateFlow(prefs.getString("api_base_url", com.yorkyang2333.claudwecho.BuildConfig.API_BASE_URL) ?: com.yorkyang2333.claudwecho.BuildConfig.API_BASE_URL)
    val apiBaseUrl: StateFlow<String> = _apiBaseUrl.asStateFlow()

    fun setApiBaseUrl(url: String) {
        var fixedUrl = url.trim()
        if (!fixedUrl.startsWith("http://") && !fixedUrl.startsWith("https://")) {
            fixedUrl = "https://$fixedUrl"
        }
        if (!fixedUrl.endsWith("/")) {
            fixedUrl = "$fixedUrl/"
        }
        prefs.edit().putString("api_base_url", fixedUrl).apply()
        _apiBaseUrl.value = fixedUrl
    }
    
    fun getApplicationContext(): Context = context.applicationContext

    init {
        calculateCacheSize()
    }

    private fun calculateCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val size = getFolderSize(context.cacheDir) + 
                       getFolderSize(context.externalCacheDir) + 
                       getFolderSize(context.codeCacheDir)
            val sizeInMb = size / (1024.0 * 1024.0)
            _cacheSize.value = String.format(java.util.Locale.US, "%.2f MB", sizeInMb)
        }
    }

    private fun getFolderSize(file: java.io.File?): Long {
        var size: Long = 0
        if (file != null && file.exists()) {
            if (file.isDirectory) {
                file.listFiles()?.forEach { child ->
                    size += getFolderSize(child)
                }
            } else {
                size = file.length()
            }
        }
        return size
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteFolder(context.cacheDir)
            deleteFolder(context.externalCacheDir)
            deleteFolder(context.codeCacheDir)
            calculateCacheSize()
        }
    }

    private fun deleteFolder(file: java.io.File?) {
        if (file != null && file.exists()) {
            if (file.isDirectory) {
                file.listFiles()?.forEach { child ->
                    deleteFolder(child)
                }
            }
            if (file != context.cacheDir && file != context.externalCacheDir && file != context.codeCacheDir) {
                file.delete()
            }
        }
    }

    fun toggleScreenShape() {
        val current = _screenShape.value
        val shapes = listOf("auto", "round", "square")
        val nextIndex = (shapes.indexOf(current) + 1) % shapes.size
        val nextShape = shapes[nextIndex]
        prefs.edit().putString("screen_shape", nextShape).apply()
        _screenShape.value = nextShape
    }

    fun toggleKeepScreenOn() {
        val next = !_keepScreenOn.value
        prefs.edit().putBoolean("keep_screen_on", next).apply()
        _keepScreenOn.value = next
    }

    fun toggleAudioCacheLimit() {
        val limits = listOf(200, 500, 1000, 2000)
        val current = _audioCacheLimitMb.value
        val currentIndex = limits.indexOf(current)
        val nextIndex = if (currentIndex != -1) (currentIndex + 1) % limits.size else 1
        val nextLimit = limits[nextIndex]
        prefs.edit().putInt("audio_cache_limit_mb", nextLimit).apply()
        _audioCacheLimitMb.value = nextLimit
        cacheEvictor?.maxBytes = nextLimit * 1024 * 1024L
    }
}
