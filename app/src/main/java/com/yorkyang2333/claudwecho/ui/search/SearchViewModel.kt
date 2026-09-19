package com.yorkyang2333.claudwecho.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorkyang2333.claudwecho.data.LocalSearchHistoryManager
import com.yorkyang2333.claudwecho.data.MainRepository
import com.yorkyang2333.claudwecho.data.api.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SearchType(val typeCode: Int, val label: String) {
    SONG(1, "单曲"),
    PLAYLIST(1000, "歌单"),
    ALBUM(10, "专辑"),
    PODCAST(1009, "播客")
}

class SearchViewModel(
    private val repository: MainRepository,
    private val historyManager: LocalSearchHistoryManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow(SearchType.SONG)
    val selectedType: StateFlow<SearchType> = _selectedType.asStateFlow()

    private val _songResults = MutableStateFlow<List<Song>>(emptyList())
    val songResults: StateFlow<List<Song>> = _songResults.asStateFlow()

    // Backward compatibility if accessed anywhere
    val searchResults: StateFlow<List<Song>> = _songResults.asStateFlow()

    private val _playlistResults = MutableStateFlow<List<com.yorkyang2333.claudwecho.data.api.Playlist>>(emptyList())
    val playlistResults: StateFlow<List<com.yorkyang2333.claudwecho.data.api.Playlist>> = _playlistResults.asStateFlow()

    private val _albumResults = MutableStateFlow<List<com.yorkyang2333.claudwecho.data.api.Album>>(emptyList())
    val albumResults: StateFlow<List<com.yorkyang2333.claudwecho.data.api.Album>> = _albumResults.asStateFlow()

    private val _podcastResults = MutableStateFlow<List<com.yorkyang2333.claudwecho.data.api.DjRadio>>(emptyList())
    val podcastResults: StateFlow<List<com.yorkyang2333.claudwecho.data.api.DjRadio>> = _podcastResults.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val pageSize = 30

    init {
        loadHistory()
    }

    private fun loadHistory() {
        _searchHistory.value = historyManager.getHistory().take(5)
    }

    fun selectType(type: SearchType) {
        if (_selectedType.value == type) return
        _selectedType.value = type
        val query = _searchQuery.value
        if (query.isNotBlank()) {
            val hasItems = when (type) {
                SearchType.SONG -> _songResults.value.isNotEmpty()
                SearchType.PLAYLIST -> _playlistResults.value.isNotEmpty()
                SearchType.ALBUM -> _albumResults.value.isNotEmpty()
                SearchType.PODCAST -> _podcastResults.value.isNotEmpty()
            }
            if (!hasItems) {
                performSearchForType(query, type, isLoadMore = false)
            } else {
                _error.value = null
            }
        }
    }

    fun performSearch(query: String) {
        if (query.isBlank()) return
        
        historyManager.addQuery(query)
        loadHistory()

        _searchQuery.value = query
        // Reset results for new query
        _songResults.value = emptyList()
        _playlistResults.value = emptyList()
        _albumResults.value = emptyList()
        _podcastResults.value = emptyList()
        _hasMore.value = false

        performSearchForType(query, _selectedType.value, isLoadMore = false)
    }

    private fun performSearchForType(query: String, type: SearchType, isLoadMore: Boolean) {
        if (isLoadMore) {
            if (_isLoadingMore.value || !_hasMore.value) return
            _isLoadingMore.value = true
        } else {
            _isLoading.value = true
            _error.value = null
        }

        val currentCount = when (type) {
            SearchType.SONG -> _songResults.value.size
            SearchType.PLAYLIST -> _playlistResults.value.size
            SearchType.ALBUM -> _albumResults.value.size
            SearchType.PODCAST -> _podcastResults.value.size
        }
        val offset = if (isLoadMore) currentCount else 0

        viewModelScope.launch {
            try {
                val result = repository.search(keywords = query, type = type.typeCode, limit = pageSize, offset = offset)
                val newItemsCount: Int = when (type) {
                    SearchType.SONG -> {
                        val songs = result?.songs ?: emptyList()
                        _songResults.value = if (isLoadMore) _songResults.value + songs else songs
                        songs.size
                    }
                    SearchType.PLAYLIST -> {
                        val playlists = result?.playlists ?: emptyList()
                        _playlistResults.value = if (isLoadMore) _playlistResults.value + playlists else playlists
                        playlists.size
                    }
                    SearchType.ALBUM -> {
                        val albums = result?.albums ?: emptyList()
                        _albumResults.value = if (isLoadMore) _albumResults.value + albums else albums
                        albums.size
                    }
                    SearchType.PODCAST -> {
                        val podcasts = result?.djRadios ?: emptyList()
                        _podcastResults.value = if (isLoadMore) _podcastResults.value + podcasts else podcasts
                        podcasts.size
                    }
                }

                _hasMore.value = newItemsCount >= pageSize
                if (!isLoadMore && newItemsCount == 0) {
                    _error.value = "未找到相关${type.label}"
                }
            } catch (e: Exception) {
                if (!isLoadMore) {
                    _error.value = "搜索失败，请重试"
                }
            } finally {
                if (isLoadMore) {
                    _isLoadingMore.value = false
                } else {
                    _isLoading.value = false
                }
            }
        }
    }

    fun loadMore() {
        val query = _searchQuery.value
        if (query.isNotBlank()) {
            performSearchForType(query, _selectedType.value, isLoadMore = true)
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _songResults.value = emptyList()
        _playlistResults.value = emptyList()
        _albumResults.value = emptyList()
        _podcastResults.value = emptyList()
        _hasMore.value = false
        _error.value = null
        loadHistory()
    }

    fun clearHistory() {
        historyManager.clearHistory()
        loadHistory()
    }
}
