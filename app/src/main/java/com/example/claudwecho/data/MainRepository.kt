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
        getPlaylistTracks(3778678L) // 热歌榜 ID
    }

    suspend fun getPlaylistTracks(id: Long): List<Song> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPlaylistDetail(id)
            if (response.code == 200) response.playlist.tracks else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLyrics(id: Long): Pair<String?, String?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLyric(id)
            if (response.code == 200) Pair(response.lrc?.lyric, response.tlyric?.lyric) else Pair(null, null)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }
    suspend fun getSongUrl(id: Long): String? = withContext(Dispatchers.IO) {
        try {
            val response = api.getSongUrl(id)
            if (response.code == 200 && response.data.isNotEmpty()) {
                response.data.first().url
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
