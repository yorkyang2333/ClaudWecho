package com.yorkyang2333.claudwecho.data

import com.yorkyang2333.claudwecho.data.api.NeteaseApi
import com.yorkyang2333.claudwecho.data.api.Playlist
import com.yorkyang2333.claudwecho.data.api.Song
import com.yorkyang2333.claudwecho.data.api.SongDetail
import com.yorkyang2333.claudwecho.data.api.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainRepository(
    private val api: NeteaseApi,
    private val localRecentPlaysManager: LocalRecentPlaysManager
) {
    private var cachedProfile: UserProfile? = null
    private var hasCheckedLogin: Boolean = false
    private var cachedDailyRecommend: List<Song>? = null
    private var cachedUserPlaylists: List<Playlist>? = null
    private var cachedHotSongs: List<Song>? = null
    private var cachedAlbums: List<com.yorkyang2333.claudwecho.data.api.Album>? = null
    private var cachedDjRadios: List<com.yorkyang2333.claudwecho.data.api.DjRadio>? = null

    suspend fun getLoginStatus(forceRefresh: Boolean = false): UserProfile? = withContext(Dispatchers.IO) {
        if (!forceRefresh && hasCheckedLogin) return@withContext cachedProfile
        try {
            val response = api.getLoginStatus()
            cachedProfile = response.data.profile
            hasCheckedLogin = true
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

    private val cachedPlaylistTracks = mutableMapOf<Long, List<Song>>()
    private val cachedPlaylistTitles = mutableMapOf<Long, String>()
    private val cachedAlbumTitles = mutableMapOf<Long, String>()
    private val cachedAlbumTracks = mutableMapOf<Long, List<Song>>()
    private val cachedDjRadioTracks = mutableMapOf<Long, List<Song>>()
    private val cachedDjRadioTitles = mutableMapOf<Long, String>()

    fun getCachedPlaylistTitle(id: Long): String? = cachedPlaylistTitles[id]
    fun getCachedAlbumTitle(id: Long): String? = cachedAlbumTitles[id]
    fun getCachedDjRadioTitle(id: Long): String? = cachedDjRadioTitles[id]

    suspend fun getPlaylistTracks(id: Long, forceRefresh: Boolean = false): List<Song> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedPlaylistTracks.containsKey(id)) {
            return@withContext cachedPlaylistTracks[id]!!
        }
        try {
            val response = api.getPlaylistDetail(id)
            val list = if (response.code == 200) response.playlist.tracks else emptyList()
            if (response.code == 200 && response.playlist.name != null) {
                cachedPlaylistTitles[id] = response.playlist.name
            }
            if (list.isNotEmpty() || forceRefresh) {
                cachedPlaylistTracks[id] = list
            }
            list
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

    suspend fun getSongDetail(id: Long): SongDetail? = withContext(Dispatchers.IO) {
        try {
            val response = api.getSongDetail(id)
            val detail = if (response.code == 200) response.songs?.firstOrNull() else null
            detail ?: findFallbackSongDetail(id)
        } catch (e: Exception) {
            findFallbackSongDetail(id)
        }
    }

    private fun findFallbackSongDetail(id: Long): SongDetail? {
        val song = cachedPlaylistTracks.values.flatten().firstOrNull { it.id == id }
            ?: cachedAlbumTracks.values.flatten().firstOrNull { it.id == id }
            ?: cachedDjRadioTracks.values.flatten().firstOrNull { it.id == id }
            ?: cachedDailyRecommend?.firstOrNull { it.id == id }
            ?: cachedHotSongs?.firstOrNull { it.id == id }
            ?: localRecentPlaysManager.getRecentSongs().firstOrNull { it.id == id }
            ?: return null

        return SongDetail(
            id = song.id,
            name = song.name,
            alia = emptyList(),
            ar = song.displayArtists,
            al = song.displayAlbum,
            dt = null,
            cd = null,
            no = null,
            publishTime = null,
            mv = null,
            fee = song.fee
        )
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

    suspend fun searchSongs(keywords: String, limit: Int = 30): List<Song> = withContext(Dispatchers.IO) {
        try {
            val response = api.search(keywords = keywords, limit = limit)
            if (response.code == 200) response.result?.songs ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun invalidatePlaylistCaches(playlistId: Long? = null) {
        if (playlistId != null) {
            cachedPlaylistTracks.remove(playlistId)
            cachedAlbumTracks.remove(playlistId)
            cachedDjRadioTracks.remove(playlistId)
        } else {
            cachedPlaylistTracks.clear()
            cachedAlbumTracks.clear()
            cachedDjRadioTracks.clear()
        }
        cachedUserPlaylists = null
    }

    suspend fun removeTracksFromPlaylist(playlistId: Long, trackIds: List<Long>): Boolean = withContext(Dispatchers.IO) {
        try {
            val tracksStr = trackIds.joinToString(",")
            val response = api.updatePlaylistTracks(op = "del", pid = playlistId, tracks = tracksStr)
            val success = response.isSuccess
            if (success) {
                invalidatePlaylistCaches(playlistId)
            }
            success
        } catch (e: Exception) {
            android.util.Log.e("MainRepository", "removeTracksFromPlaylist error: ${e.message}", e)
            false
        }
    }

    suspend fun addTracksToPlaylist(playlistId: Long, trackIds: List<Long>): Boolean = withContext(Dispatchers.IO) {
        try {
            val tracksStr = trackIds.joinToString(",")
            val response = api.updatePlaylistTracks(op = "add", pid = playlistId, tracks = tracksStr)
            val success = response.isSuccess
            if (success) {
                invalidatePlaylistCaches(playlistId)
            }
            success
        } catch (e: Exception) {
            android.util.Log.e("MainRepository", "addTracksToPlaylist error: ${e.message}", e)
            false
        }
    }

    suspend fun getSubscribedAlbums(forceRefresh: Boolean = false): List<com.yorkyang2333.claudwecho.data.api.Album> = withContext(Dispatchers.IO) {
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

    suspend fun getSubscribedDjRadios(forceRefresh: Boolean = false): List<com.yorkyang2333.claudwecho.data.api.DjRadio> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedDjRadios != null) return@withContext cachedDjRadios!!
        try {
            val response = api.getSubscribedDjRadios()
            val list = if (response.code == 200) response.djRadios else emptyList()
            if (response.code == 200) {
                list.forEach { dj ->
                    cachedDjRadioTitles[dj.id] = dj.name
                }
            }
            cachedDjRadios = list
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAlbumTracks(id: Long, forceRefresh: Boolean = false): List<Song> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedAlbumTracks.containsKey(id)) {
            return@withContext cachedAlbumTracks[id]!!
        }
        try {
            val response = api.getAlbumDetail(id)
            val list = if (response.code == 200) response.songs ?: emptyList() else emptyList()
            if (response.code == 200 && response.album?.name != null) {
                cachedAlbumTitles[id] = response.album.name
            }
            if (list.isNotEmpty() || forceRefresh) {
                cachedAlbumTracks[id] = list
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDjRadioPrograms(rid: Long, forceRefresh: Boolean = false): List<Song> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedDjRadioTracks.containsKey(rid)) {
            return@withContext cachedDjRadioTracks[rid]!!
        }
        try {
            val response = api.getDjPrograms(rid = rid)
            val list = if (response.code == 200 && response.programs != null) {
                response.programs.map { program ->
                    val djSong = program.mainSong
                    // Map DjSong to Song for unified playback
                    Song(
                        id = djSong.id,
                        name = djSong.name,
                        ar = djSong.artists ?: emptyList(),
                        al = djSong.album,
                        fee = 0,
                        isPodcast = true
                    )
                }
            } else {
                emptyList()
            }
            try {
                if (!cachedDjRadioTitles.containsKey(rid) || forceRefresh) {
                    val detailResp = api.getDjRadioDetail(rid)
                    val radioName = detailResp.radio?.name
                    if (detailResp.code == 200 && radioName != null) {
                        cachedDjRadioTitles[rid] = radioName
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
            if (list.isNotEmpty() || forceRefresh) {
                cachedDjRadioTracks[rid] = list
            }
            list
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
            val success = response.code == 200 || response.code == 502
            if (success) {
                invalidatePlaylistCaches()
                cachedDailyRecommend = null
            }
            success
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getLikeList(uid: Long): Set<Long> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLikeList(uid)
            if (response.code == 200) response.ids.toSet() else emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun getPersonalFm(): List<Song> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPersonalFm()
            if (response.code == 200) response.data else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun trashPersonalFm(id: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.fmTrash(id)
            response.code == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getVipInfo(): com.yorkyang2333.claudwecho.data.api.VipInfoData? = withContext(Dispatchers.IO) {
        try {
            val response = api.getVipInfo()
            if (response.code == 200) response.data else null
        } catch (e: Exception) {
            null
        }
    }
}
