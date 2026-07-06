package com.yorkyang2333.claudwecho.data

import android.content.Context
import android.content.SharedPreferences
import com.yorkyang2333.claudwecho.data.api.Song
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PlaybackStateManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("playback_state", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val currentTrackId = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)
    val isPersonalFmMode = kotlinx.coroutines.flow.MutableStateFlow(false)

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

    fun savePlaybackMode(repeatMode: Int, shuffleMode: Boolean) {
        prefs.edit()
            .putInt("repeat_mode", repeatMode)
            .putBoolean("shuffle_mode", shuffleMode)
            .apply()
    }

    fun getRepeatMode(): Int {
        return prefs.getInt("repeat_mode", androidx.media3.common.Player.REPEAT_MODE_ALL)
    }

    fun getShuffleMode(): Boolean {
        return prefs.getBoolean("shuffle_mode", false)
    }
}
