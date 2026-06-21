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

class PlaylistDetailViewModel(private val repository: MainRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _originalSongs = MutableStateFlow<List<Song>>(emptyList())
    
    private val _sortMode = MutableStateFlow(SortMode.DEFAULT)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()
    
    private val _sortOrder = MutableStateFlow(SortOrder.ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val songs: StateFlow<List<Song>> = combine(
        _originalSongs, _sortMode, _sortOrder
    ) { original, mode, order ->
        if (mode == SortMode.DEFAULT) {
            if (order == SortOrder.ASC) original else original.reversed()
        } else {
            val sorted = original.sortedBy { song ->
                val text = when (mode) {
                    SortMode.TITLE -> song.name
                    SortMode.ALBUM -> song.displayAlbum?.name ?: ""
                    SortMode.ARTIST -> song.displayArtists.joinToString { it.name }
                    else -> ""
                }
                getPinyinKey(text)
            }
            if (order == SortOrder.ASC) sorted else sorted.reversed()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _title = MutableStateFlow<String?>(null)
    val title: StateFlow<String?> = _title.asStateFlow()

    private val _isOwnedPlaylist = MutableStateFlow(false)
    val isOwnedPlaylist: StateFlow<Boolean> = _isOwnedPlaylist.asStateFlow()

    private var currentPlaylistId: Long = -1

    fun setSort(mode: SortMode, order: SortOrder) {
        _sortMode.value = mode
        _sortOrder.value = order
    }

    private suspend fun checkOwnership(playlistId: Long) {
        val profile = repository.getLoginStatus()
        if (profile != null) {
            val pl = repository.getUserPlaylists(profile.userId)
            val isOwned = pl.any { it.id == playlistId }
            _isOwnedPlaylist.value = isOwned
        }
    }

    fun loadPlaylist(id: Long, forceRefresh: Boolean = false) {
        currentPlaylistId = id
        _isLoading.value = true
        viewModelScope.launch {
            checkOwnership(id)
            _originalSongs.value = repository.getPlaylistTracks(id, forceRefresh)
            _title.value = repository.getCachedPlaylistTitle(id)
            _isLoading.value = false
        }
    }

    fun loadAlbum(id: Long, forceRefresh: Boolean = false) {
        currentPlaylistId = id
        _isLoading.value = true
        _isOwnedPlaylist.value = false
        viewModelScope.launch {
            _originalSongs.value = repository.getAlbumTracks(id)
            _title.value = repository.getCachedAlbumTitle(id)
            _isLoading.value = false
        }
    }

    fun loadDjRadio(id: Long, forceRefresh: Boolean = false) {
        currentPlaylistId = id
        _isLoading.value = true
        _isOwnedPlaylist.value = false
        viewModelScope.launch {
            _originalSongs.value = repository.getDjRadioPrograms(id)
            _title.value = "播客"
            _isLoading.value = false
        }
    }

    fun loadLiked(forceRefresh: Boolean = false) {
        _isLoading.value = true
        _isOwnedPlaylist.value = true
        viewModelScope.launch {
            val profile = repository.getLoginStatus()
            if (profile != null) {
                val pl = repository.getUserPlaylists(profile.userId)
                val likedId = pl.firstOrNull()?.id
                if (likedId != null) {
                    currentPlaylistId = likedId
                    _originalSongs.value = repository.getPlaylistTracks(likedId, forceRefresh)
                    _title.value = repository.getCachedPlaylistTitle(likedId) ?: "我喜欢"
                }
            }
            _isLoading.value = false
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

    companion object {
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
            val key = getPinyinKey(text)
            if (key.isBlank()) return "#"
            val firstChar = key[0]
            return if (firstChar in 'A'..'Z') firstChar.toString() else "#"
        }
    }
}
