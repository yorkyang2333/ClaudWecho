package com.example.claudwecho.data

import android.content.Context
import android.content.SharedPreferences
import com.example.claudwecho.data.api.Song
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PlaybackStateManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("playback_state", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val currentTrackId = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)

    fun saveState(songs: List<Song>, index: Int) {
        try {
            val jsonStr = json.encodeToString(songs)
            prefs.edit()
                .putString("last_playlist", jsonStr)
                .putInt("last_index", index)
                .apply()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun getLastPlaylist(): List<Song>? {
        val jsonStr = prefs.getString("last_playlist", null) ?: return null
        return try {
            json.decodeFromString<List<Song>>(jsonStr)
        } catch (e: Exception) {
            null
        }
    }

    fun getLastIndex(): Int {
        return prefs.getInt("last_index", 0)
    }
}
