package com.example.claudwecho.data

import com.example.claudwecho.data.api.NeteaseApi
import com.example.claudwecho.data.api.Playlist
import com.example.claudwecho.data.api.Song
import com.example.claudwecho.data.api.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainRepository(
    private val api: NeteaseApi,
    private val localRecentPlaysManager: LocalRecentPlaysManager
) {
    private var cachedProfile: UserProfile? = null
    private var cachedDailyRecommend: List<Song>? = null
    private var cachedUserPlaylists: List<Playlist>? = null
    private var cachedHotSongs: List<Song>? = null
    private var cachedAlbums: List<com.example.claudwecho.data.api.Album>? = null
    private var cachedDjRadios: List<com.example.claudwecho.data.api.DjRadio>? = null

    suspend fun getLoginStatus(forceRefresh: Boolean = false): UserProfile? = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedProfile != null) return@withContext cachedProfile
        try {
            val response = api.getLoginStatus()
            cachedProfile = response.data.profile
            cachedProfile
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getDailyRecommendSongs(forceRefresh: Boolean = false): List<Song> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedDailyRecommend != null) return@withContext cachedDailyRecommend!!
        try {
            val response = api.getRecommendSongs()
            val list = if (response.code == 200) response.data.dailySongs else emptyList()
            cachedDailyRecommend = list
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserPlaylists(uid: Long, forceRefresh: Boolean = false): List<Playlist> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedUserPlaylists != null) return@withContext cachedUserPlaylists!!
        try {
            val response = api.getUserPlaylists(uid)
            val list = if (response.code == 200) response.playlist else emptyList()
            cachedUserPlaylists = list
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getHotSongs(forceRefresh: Boolean = false): List<Song> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedHotSongs != null) return@withContext cachedHotSongs!!
        val list = getPlaylistTracks(3778678L) // 热歌榜 ID
        cachedHotSongs = list
        list
    }

    suspend fun getPlaylistTracks(id: Long): List<Song> = withContext(Dispatchers.IO) {
        // We usually don't cache individual playlist tracks heavily in memory here,
        // but could add it later if needed.
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

    suspend fun getSubscribedAlbums(forceRefresh: Boolean = false): List<com.example.claudwecho.data.api.Album> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedAlbums != null) return@withContext cachedAlbums!!
        try {
            val response = api.getSubscribedAlbums()
            val list = if (response.code == 200) response.data else emptyList()
            cachedAlbums = list
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSubscribedDjRadios(forceRefresh: Boolean = false): List<com.example.claudwecho.data.api.DjRadio> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedDjRadios != null) return@withContext cachedDjRadios!!
        try {
            val response = api.getSubscribedDjRadios()
            val list = if (response.code == 200) response.djRadios else emptyList()
            cachedDjRadios = list
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAlbumTracks(id: Long): List<Song> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAlbumDetail(id)
            if (response.code == 200) response.songs ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDjRadioPrograms(rid: Long): List<Song> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDjPrograms(rid = rid)
            if (response.code == 200 && response.programs != null) {
                response.programs.map { program ->
                    val djSong = program.mainSong
                    // Map DjSong to Song for unified playback
                    Song(
                        id = djSong.id,
                        name = djSong.name,
                        ar = djSong.artists ?: emptyList(),
                        al = djSong.album,
                        fee = 0
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRecentSongs(): List<Song> = withContext(Dispatchers.IO) {
        localRecentPlaysManager.getRecentSongs()
    }

    suspend fun recordRecentPlay(song: Song) = withContext(Dispatchers.IO) {
        localRecentPlaysManager.addSong(song)
    }

    suspend fun likeSong(id: Long, like: Boolean = true): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.likeSong(id, like)
            response.code == 200
        } catch (e: Exception) {
            false
        }
    }
}
