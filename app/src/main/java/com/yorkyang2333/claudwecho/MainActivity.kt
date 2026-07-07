package com.yorkyang2333.claudwecho

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.MaterialTheme
import com.yorkyang2333.claudwecho.theme.ClaudWechoTheme

class MainActivity : ComponentActivity() {
    private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

        fun updateKeepScreenOn() {
            val keepScreenOn = prefs.getBoolean("keep_screen_on", false)
            if (keepScreenOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        updateKeepScreenOn()
        prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "keep_screen_on") {
                updateKeepScreenOn()
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)

        setContent {
            ClaudWechoTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("keep_screen_on", false)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        prefsListener?.let {
            getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(it)
        }
    }
}
