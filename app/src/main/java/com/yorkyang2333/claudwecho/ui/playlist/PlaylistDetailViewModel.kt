package com.yorkyang2333.claudwecho.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorkyang2333.claudwecho.data.MainRepository
import com.yorkyang2333.claudwecho.data.api.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.yorkyang2333.claudwecho.utils.PinyinUtil.getPinyinKey

enum class SortMode { DEFAULT, TITLE, ALBUM, ARTIST }
enum class SortOrder { ASC, DESC }

data class ResourceDetailInfo(
    val id: Long,
    val title: String,
    val coverUrl: String?,
    val subtitle: String?,
    val trackCount: Int,
    val description: String?,
    val tags: List<String> = emptyList(),
    val publishDate: String? = null,
    val type: String
)

class PlaylistDetailViewModel(
    private val repository: MainRepository,
    context: android.content.Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("playlist_sort_prefs", android.content.Context.MODE_PRIVATE)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _originalSongs = MutableStateFlow<List<Song>>(emptyList())
    
    private val _sortMode = MutableStateFlow(SortMode.DEFAULT)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()
    
    private val _sortOrder = MutableStateFlow(SortOrder.ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allSortedSongs: StateFlow<List<Song>> = combine(
        _originalSongs, _sortMode, _sortOrder
    ) { original, mode, order ->
        withContext(Dispatchers.Default) {
            if (mode == SortMode.DEFAULT) {
                if (order == SortOrder.ASC) original else original.reversed()
            } else {
                val decorated = original.map { song ->
                    val text = when (mode) {
                        SortMode.TITLE -> song.name
                        SortMode.ALBUM -> song.displayAlbum?.name ?: ""
                        SortMode.ARTIST -> song.displayArtists.joinToString { it.name }
                        SortMode.DEFAULT -> ""
                    }
                    val pinyinKey = getCachedPinyinKey(text)
                    Pair(song, pinyinKey)
                }
                val sortedDecorated = decorated.sortedBy { it.second }
                val sorted = sortedDecorated.map { it.first }
                if (order == SortOrder.ASC) sorted else sorted.reversed()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val songs: StateFlow<List<Song>> = combine(
        allSortedSongs, _searchQuery
    ) { sorted, query ->
        withContext(Dispatchers.Default) {
            if (query.isBlank()) {
                sorted
            } else {
                sorted.filter { song ->
                    song.name.contains(query, ignoreCase = true) ||
                    song.displayArtists.any { it.name.contains(query, ignoreCase = true) } ||
                    (song.displayAlbum?.name?.contains(query, ignoreCase = true) == true)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    private val _title = MutableStateFlow<String?>(null)
    val title: StateFlow<String?> = _title.asStateFlow()

    private val _isOwnedPlaylist = MutableStateFlow(false)
    val isOwnedPlaylist: StateFlow<Boolean> = _isOwnedPlaylist.asStateFlow()

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    private val _resourceDetail = MutableStateFlow<ResourceDetailInfo?>(null)
    val resourceDetail: StateFlow<ResourceDetailInfo?> = _resourceDetail.asStateFlow()

    private var currentPlaylistId: Long = -1

    private fun loadSortPrefs(id: Long) {
        val modeName = prefs.getString("sort_mode_$id", SortMode.DEFAULT.name) ?: SortMode.DEFAULT.name
        val orderName = prefs.getString("sort_order_$id", SortOrder.ASC.name) ?: SortOrder.ASC.name
        _sortMode.value = SortMode.valueOf(modeName)
        _sortOrder.value = SortOrder.valueOf(orderName)
    }

    fun setSort(mode: SortMode, order: SortOrder) {
        _sortMode.value = mode
        _sortOrder.value = order
        val id = currentPlaylistId
        if (id != -1L) {
            prefs.edit()
                .putString("sort_mode_$id", mode.name)
                .putString("sort_order_$id", order.name)
                .apply()
        }
    }

    private suspend fun checkOwnership(playlistId: Long) {
        val profile = repository.getLoginStatus()
        if (profile != null) {
            val pl = repository.getUserPlaylists(profile.userId)
            val playlist = pl.find { it.id == playlistId }
            val isOwned = playlist?.isCreatedBy(profile.userId) ?: false
            _isOwnedPlaylist.value = isOwned
        }
    }

    fun loadPlaylist(id: Long, forceRefresh: Boolean = false) {
        currentPlaylistId = id
        loadSortPrefs(id)
        if (forceRefresh || _originalSongs.value.isEmpty()) {
            _isLoading.value = true
        }
        viewModelScope.launch {
            checkOwnership(id)
            val detail = repository.getPlaylistDetail(id, forceRefresh)
            val tracks = detail?.tracks ?: emptyList()
            _originalSongs.value = tracks
            val titleStr = detail?.name ?: repository.getCachedPlaylistTitle(id)
            _title.value = titleStr
            _isSubscribed.value = detail?.subscribed ?: false
            _resourceDetail.value = ResourceDetailInfo(
                id = id,
                title = titleStr ?: "歌单",
                coverUrl = detail?.coverImgUrl ?: tracks.firstOrNull()?.displayAlbum?.picUrl,
                subtitle = detail?.creator?.nickname?.let { "创建者: $it" },
                trackCount = detail?.trackCount ?: tracks.size,
                description = detail?.description,
                tags = detail?.tags ?: emptyList(),
                publishDate = detail?.createTime?.let { formatDate(it) },
                type = "playlist"
            )
            _isLoading.value = false
        }
    }

    fun loadAlbum(id: Long, forceRefresh: Boolean = false) {
        currentPlaylistId = id
        loadSortPrefs(id)
        if (forceRefresh || _originalSongs.value.isEmpty()) {
            _isLoading.value = true
        }
        _isOwnedPlaylist.value = false
        viewModelScope.launch {
            val album = repository.getAlbumDetail(id, forceRefresh)
            val tracks = repository.getAlbumTracks(id, forceRefresh)
            _originalSongs.value = tracks
            val titleStr = album?.name ?: repository.getCachedAlbumTitle(id)
            _title.value = titleStr
            _isSubscribed.value = repository.isAlbumSubscribed(id)
            val artistName = album?.artist?.name ?: album?.artists?.joinToString(" / ") { it.name } ?: tracks.firstOrNull()?.displayArtists?.joinToString(" / ") { it.name }
            _resourceDetail.value = ResourceDetailInfo(
                id = id,
                title = titleStr ?: "专辑",
                coverUrl = album?.picUrl ?: tracks.firstOrNull()?.displayAlbum?.picUrl,
                subtitle = artistName?.let { "歌手: $it" },
                trackCount = album?.size ?: tracks.size,
                description = album?.description,
                publishDate = album?.publishTime?.let { formatDate(it) },
                type = "album"
            )
            _isLoading.value = false
        }
    }

    fun loadDjRadio(id: Long, forceRefresh: Boolean = false) {
        currentPlaylistId = id
        loadSortPrefs(id)
        if (forceRefresh || _originalSongs.value.isEmpty()) {
            _isLoading.value = true
        }
        _isOwnedPlaylist.value = false
        viewModelScope.launch {
            val programs = repository.getDjRadioPrograms(id, forceRefresh)
            val dj = repository.getDjRadioDetail(id, forceRefresh)
            _originalSongs.value = programs
            val titleStr = dj?.name ?: repository.getCachedDjRadioTitle(id) ?: "播客"
            _title.value = titleStr
            _isSubscribed.value = repository.isDjRadioSubscribed(id)
            _resourceDetail.value = ResourceDetailInfo(
                id = id,
                title = titleStr,
                coverUrl = dj?.picUrl ?: programs.firstOrNull()?.displayAlbum?.picUrl,
                subtitle = dj?.dj?.nickname?.let { "主播: $it" },
                trackCount = dj?.programCount ?: programs.size,
                description = dj?.desc,
                type = "djradio"
            )
            _isLoading.value = false
        }
    }

    fun loadLiked(forceRefresh: Boolean = false) {
        if (forceRefresh || _originalSongs.value.isEmpty()) {
            _isLoading.value = true
        }
        _isOwnedPlaylist.value = true
        _isSubscribed.value = true
        viewModelScope.launch {
            val profile = repository.getLoginStatus()
            if (profile != null) {
                val pl = repository.getUserPlaylists(profile.userId)
                val likedId = pl.firstOrNull()?.id
                if (likedId != null) {
                    currentPlaylistId = likedId
                    loadSortPrefs(likedId)
                    val tracks = repository.getPlaylistTracks(likedId, forceRefresh)
                    _originalSongs.value = tracks
                    val titleStr = repository.getCachedPlaylistTitle(likedId) ?: "我喜欢"
                    _title.value = titleStr
                    _resourceDetail.value = ResourceDetailInfo(
                        id = likedId,
                        title = titleStr,
                        coverUrl = tracks.firstOrNull()?.displayAlbum?.picUrl,
                        subtitle = profile.nickname.let { "创建者: $it" },
                        trackCount = tracks.size,
                        description = "我喜欢的音乐",
                        type = "liked"
                    )
                }
            }
            _isLoading.value = false
        }
    }

    fun toggleSubscribe(type: String) {
        viewModelScope.launch {
            val target = !_isSubscribed.value
            val success = when (type) {
                "album" -> repository.subscribeAlbum(currentPlaylistId, target)
                "djradio" -> repository.subscribeDjRadio(currentPlaylistId, target)
                else -> repository.subscribePlaylist(currentPlaylistId, target)
            }
            if (success) {
                _isSubscribed.value = target
            }
        }
    }

    fun removeSongs(songIds: List<Long>, isLikedPlaylist: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            var success = true
            if (isLikedPlaylist) {
                for (id in songIds) {
                    val res = repository.likeSong(id, false)
                    if (!res) success = false
                }
            } else {
                success = repository.removeTracksFromPlaylist(currentPlaylistId, songIds)
            }
            if (success) {
                val newSongs = _originalSongs.value.filter { it.id !in songIds }
                _originalSongs.value = newSongs
            }
            _isLoading.value = false
        }
    }

    fun getFirstItemIndexByLetter(letter: String): Int {
        val list = songs.value
        val mode = _sortMode.value
        for (i in list.indices) {
            val song = list[i]
            val text = when (mode) {
                SortMode.TITLE -> song.name
                SortMode.ALBUM -> song.displayAlbum?.name ?: ""
                SortMode.ARTIST -> song.displayArtists.joinToString { it.name }
                else -> song.name
            }
            if (getFirstLetter(text) == letter) {
                return i
            }
        }
        return -1
    }

    private fun formatDate(timestamp: Long): String =
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(timestamp))

    companion object {
        private val pinyinCache = java.util.concurrent.ConcurrentHashMap<String, String>()

        fun getCachedPinyinKey(text: String?): String {
            if (text.isNullOrBlank()) return "#"
            return pinyinCache.getOrPut(text) { getPinyinKey(text) }
        }

        fun getPinyinKey(text: String?): String {
            if (text.isNullOrBlank()) return "#"
            val sb = StringBuilder()
            for (c in text) {
                val pinyins = net.sourceforge.pinyin4j.PinyinHelper.toHanyuPinyinStringArray(c)
                if (pinyins != null && pinyins.isNotEmpty()) {
                    sb.append(pinyins[0].replace(Regex("\\d"), ""))
                } else {
                    sb.append(c)
                }
            }
            return sb.toString().uppercase()
        }

        fun getFirstLetter(text: String?): String {
            val key = getCachedPinyinKey(text)
            if (key.isBlank()) return "#"
            val firstChar = key[0]
            return if (firstChar in 'A'..'Z') firstChar.toString() else "#"
        }
    }
}
