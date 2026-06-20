package com.yorkyang2333.claudwecho.data

import android.content.Context
import android.content.SharedPreferences
import com.yorkyang2333.claudwecho.data.api.Song
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalRecentPlaysManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("recent_plays", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    
    companion object {
        private const val KEY_RECENT_SONGS = "recent_songs"
        private const val MAX_RECENT_SONGS = 100
    }

    fun addSong(song: Song) {
        val currentSongs = getRecentSongs().toMutableList()
        // Remove duplicate if it already exists
        currentSongs.removeAll { it.id == song.id }
        
        // Add to the front
        currentSongs.add(0, song)
        
        // Keep within limit
        if (currentSongs.size > MAX_RECENT_SONGS) {
            currentSongs.subList(MAX_RECENT_SONGS, currentSongs.size).clear()
        }
        
        saveSongs(currentSongs)
    }

    fun getRecentSongs(): List<Song> {
        val jsonStr = prefs.getString(KEY_RECENT_SONGS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<Song>>(jsonStr)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveSongs(songs: List<Song>) {
        try {
            val jsonStr = json.encodeToString(songs)
            prefs.edit().putString(KEY_RECENT_SONGS, jsonStr).apply()
        } catch (e: Exception) {
            // Error encoding songs
        }
    }
}
