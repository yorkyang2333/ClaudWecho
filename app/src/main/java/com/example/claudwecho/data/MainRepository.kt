package com.example.claudwecho.data

import com.example.claudwecho.data.api.NeteaseApi
import com.example.claudwecho.data.api.Playlist
import com.example.claudwecho.data.api.Song
import com.example.claudwecho.data.api.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainRepository(private val api: NeteaseApi) {
    suspend fun getLoginStatus(): UserProfile? = withContext(Dispatchers.IO) {
        try {
            val response = api.getLoginStatus()
            response.data.profile
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getDailyRecommendSongs(): List<Song> = withContext(Dispatchers.IO) {
        try {
            val response = api.getRecommendSongs()
            if (response.code == 200) response.data.dailySongs else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserPlaylists(uid: Long): List<Playlist> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUserPlaylists(uid)
            if (response.code == 200) response.playlist else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getHotSongs(): List<Song> = withContext(Dispatchers.IO) {
        try {
            // 热歌榜 ID: 3778678
            val response = api.getPlaylistDetail(3778678L)
            if (response.code == 200) response.playlist.tracks else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
