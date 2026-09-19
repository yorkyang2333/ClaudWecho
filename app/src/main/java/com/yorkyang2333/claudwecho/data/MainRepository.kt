package com.yorkyang2333.claudwecho.data

import com.yorkyang2333.claudwecho.data.api.NeteaseApi
import com.yorkyang2333.claudwecho.data.api.Playlist
import com.yorkyang2333.claudwecho.data.api.Song
import com.yorkyang2333.claudwecho.data.api.SongDetail
import com.yorkyang2333.claudwecho.data.api.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class MainRepository(
    private val api: NeteaseApi,
    private val localRecentPlaysManager: LocalRecentPlaysManager
) {
    private val _collectionUpdates = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val collectionUpdates: SharedFlow<Unit> = _collectionUpdates.asSharedFlow()

    private val songIdToPodcastId = ConcurrentHashMap<Long, Long>()

    fun notifyCollectionChanged() {
        _collectionUpdates.tryEmit(Unit)
    }

    fun getPodcastIdForSong(songId: Long): Long? = songIdToPodcastId[songId]

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
    private val cachedPlaylistDetails = mutableMapOf<Long, com.yorkyang2333.claudwecho.data.api.PlaylistDetail>()
    private val cachedAlbumTitles = mutableMapOf<Long, String>()
    private val cachedAlbumTracks = mutableMapOf<Long, List<Song>>()
    private val cachedAlbumDetails = mutableMapOf<Long, com.yorkyang2333.claudwecho.data.api.Album>()
    private val cachedDjRadioTracks = mutableMapOf<Long, List<Song>>()
    private val cachedDjRadioTitles = mutableMapOf<Long, String>()
    private val cachedDjRadioDetails = mutableMapOf<Long, com.yorkyang2333.claudwecho.data.api.DjRadio>()

    fun getCachedPlaylistTitle(id: Long): String? = cachedPlaylistTitles[id]
    fun getCachedAlbumTitle(id: Long): String? = cachedAlbumTitles[id]
    fun getCachedDjRadioTitle(id: Long): String? = cachedDjRadioTitles[id]

    suspend fun getPlaylistDetail(id: Long, forceRefresh: Boolean = false): com.yorkyang2333.claudwecho.data.api.PlaylistDetail? = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedPlaylistDetails.containsKey(id)) {
            return@withContext cachedPlaylistDetails[id]
        }
        try {
            val response = api.getPlaylistDetail(id)
            if (response.code == 200) {
                val detail = response.playlist
                cachedPlaylistDetails[id] = detail
                if (detail.name != null) {
                    cachedPlaylistTitles[id] = detail.name
                }
                if (detail.tracks.isNotEmpty() || forceRefresh) {
                    cachedPlaylistTracks[id] = detail.tracks
                }
                detail
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPlaylistTracks(id: Long, forceRefresh: Boolean = false): List<Song> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedPlaylistTracks.containsKey(id)) {
            return@withContext cachedPlaylistTracks[id]!!
        }
        val detail = getPlaylistDetail(id, forceRefresh)
        detail?.tracks ?: emptyList()
    }

    suspend fun getLyrics(id: Long): LyricDataResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getLyricNew(id)
            if (response.code == 200 && (response.yrc?.lyric != null || response.lrc?.lyric != null)) {
                LyricDataResult(
                    lrc = response.lrc?.lyric,
                    tlyric = response.tlyric?.lyric,
                    yrc = response.yrc?.lyric,
                    ytlrc = response.ytlrc?.lyric
                )
            } else {
                fallbackLyrics(id)
            }
        } catch (e: Exception) {
            fallbackLyrics(id)
        }
    }

    private suspend fun fallbackLyrics(id: Long): LyricDataResult {
        return try {
            val response = api.getLyric(id)
            if (response.code == 200) {
                LyricDataResult(
                    lrc = response.lrc?.lyric,
                    tlyric = response.tlyric?.lyric,
                    yrc = response.yrc?.lyric,
                    ytlrc = response.ytlrc?.lyric
                )
            } else {
                LyricDataResult()
            }
        } catch (e: Exception) {
            LyricDataResult()
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

    suspend fun search(
        keywords: String,
        type: Int = 1,
        limit: Int = 30,
        offset: Int = 0
    ): com.yorkyang2333.claudwecho.data.api.SearchResult? = withContext(Dispatchers.IO) {
        try {
            val response = api.search(keywords = keywords, limit = limit, offset = offset, type = type)
            if (response.code == 200) response.result else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchSongs(keywords: String, limit: Int = 30, offset: Int = 0): List<Song> = withContext(Dispatchers.IO) {
        search(keywords = keywords, type = 1, limit = limit, offset = offset)?.songs ?: emptyList()
    }

    fun invalidatePlaylistCaches(playlistId: Long? = null) {
        if (playlistId != null) {
            cachedPlaylistTracks.remove(playlistId)
            cachedAlbumTracks.remove(playlistId)
            cachedDjRadioTracks.remove(playlistId)
            cachedPlaylistDetails.remove(playlistId)
            cachedAlbumDetails.remove(playlistId)
            cachedDjRadioDetails.remove(playlistId)
        } else {
            cachedPlaylistTracks.clear()
            cachedAlbumTracks.clear()
            cachedDjRadioTracks.clear()
            cachedPlaylistDetails.clear()
            cachedAlbumDetails.clear()
            cachedDjRadioDetails.clear()
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

    suspend fun getAlbumDetail(id: Long, forceRefresh: Boolean = false): com.yorkyang2333.claudwecho.data.api.Album? = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedAlbumDetails.containsKey(id)) {
            return@withContext cachedAlbumDetails[id]
        }
        try {
            val response = api.getAlbumDetail(id)
            if (response.code == 200) {
                val album = response.album
                if (album != null) {
                    cachedAlbumDetails[id] = album
                    if (album.name != null) {
                        cachedAlbumTitles[id] = album.name
                    }
                }
                val list = response.songs ?: emptyList()
                if (list.isNotEmpty() || forceRefresh) {
                    cachedAlbumTracks[id] = list
                }
                album
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAlbumTracks(id: Long, forceRefresh: Boolean = false): List<Song> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedAlbumTracks.containsKey(id)) {
            return@withContext cachedAlbumTracks[id]!!
        }
        getAlbumDetail(id, forceRefresh)
        cachedAlbumTracks[id] ?: emptyList()
    }

    suspend fun getDjRadioDetail(rid: Long, forceRefresh: Boolean = false): com.yorkyang2333.claudwecho.data.api.DjRadio? = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedDjRadioDetails.containsKey(rid)) {
            return@withContext cachedDjRadioDetails[rid]
        }
        try {
            val detailResp = api.getDjRadioDetail(rid)
            val dj = if (detailResp.code == 200) detailResp.radio else null
            if (dj != null) {
                cachedDjRadioDetails[rid] = dj
                cachedDjRadioTitles[rid] = dj.name
            }
            dj
        } catch (e: Exception) {
            null
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
                    val podcastId = program.radio?.id ?: rid
                    songIdToPodcastId[djSong.id] = podcastId
                    // Map DjSong to Song for unified playback
                    Song(
                        id = djSong.id,
                        name = djSong.name,
                        ar = djSong.artists ?: emptyList(),
                        al = djSong.album,
                        fee = 0,
                        isPodcast = true,
                        podcastId = podcastId
                    )
                }
            } else {
                emptyList()
            }
            getDjRadioDetail(rid, forceRefresh)
            if (list.isNotEmpty() || forceRefresh) {
                cachedDjRadioTracks[rid] = list
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun subscribePlaylist(id: Long, subscribe: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.subscribePlaylist(id = id, t = if (subscribe) 1 else 2)
            val success = response.isSuccess
            if (success) {
                cachedPlaylistDetails[id]?.let {
                    cachedPlaylistDetails[id] = it.copy(subscribed = subscribe)
                }
                cachedUserPlaylists = null
                notifyCollectionChanged()
            }
            success
        } catch (e: Exception) {
            android.util.Log.e("MainRepository", "subscribePlaylist error: ${e.message}", e)
            false
        }
    }

    suspend fun subscribeAlbum(id: Long, subscribe: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.subscribeAlbum(id = id, t = if (subscribe) 1 else 0)
            val success = response.isSuccess
            if (success) {
                cachedAlbumDetails[id]?.let {
                    // Update cache state if present
                }
                cachedAlbums = null
                notifyCollectionChanged()
            }
            success
        } catch (e: Exception) {
            android.util.Log.e("MainRepository", "subscribeAlbum error: ${e.message}", e)
            false
        }
    }

    suspend fun isAlbumSubscribed(id: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            getSubscribedAlbums().any { it.id == id }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun subscribeDjRadio(rid: Long, subscribe: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.subscribeDjRadio(rid = rid, t = if (subscribe) 1 else 0)
            val success = response.isSuccess
            if (success) {
                cachedDjRadioDetails[rid]?.let {
                    cachedDjRadioDetails[rid] = it.copy(subed = subscribe)
                }
                cachedDjRadios = null
                notifyCollectionChanged()
            }
            success
        } catch (e: Exception) {
            android.util.Log.e("MainRepository", "subscribeDjRadio error: ${e.message}", e)
            false
        }
    }

    suspend fun isDjRadioSubscribed(id: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val detail = getDjRadioDetail(id)
            if (detail?.subed != null) {
                return@withContext detail.subed
            }
            getSubscribedDjRadios().any { it.id == id }
        } catch (e: Exception) {
            false
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
                notifyCollectionChanged()
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

    suspend fun getMusicComments(id: Long, limit: Int = 20, offset: Int = 0): com.yorkyang2333.claudwecho.data.api.CommentResponse? = withContext(Dispatchers.IO) {
        try {
            val response = api.getMusicComments(id = id, limit = limit, offset = offset)
            if (response.code == 200) response else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getHotComments(id: Long, limit: Int = 20, offset: Int = 0): com.yorkyang2333.claudwecho.data.api.HotCommentResponse? = withContext(Dispatchers.IO) {
        try {
            val response = api.getHotComments(id = id, type = 0, limit = limit, offset = offset)
            if (response.code == 200) response else null
        } catch (e: Exception) {
            null
        }
    }
}

data class LyricDataResult(
    val lrc: String? = null,
    val tlyric: String? = null,
    val yrc: String? = null,
    val ytlrc: String? = null
)
