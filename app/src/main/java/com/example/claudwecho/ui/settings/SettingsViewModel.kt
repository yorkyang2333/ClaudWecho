package com.example.claudwecho.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(private val context: Context) : ViewModel() {
    private val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _screenShape = MutableStateFlow(prefs.getString("screen_shape", "auto") ?: "auto")
    val screenShape: StateFlow<String> = _screenShape.asStateFlow()

    fun setScreenShape(shape: String) {
        prefs.edit().putString("screen_shape", shape).apply()
        _screenShape.value = shape
    }
}
