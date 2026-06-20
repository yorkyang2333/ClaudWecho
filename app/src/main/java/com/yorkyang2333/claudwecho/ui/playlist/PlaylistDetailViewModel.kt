package com.yorkyang2333.claudwecho.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorkyang2333.claudwecho.data.MainRepository
import com.yorkyang2333.claudwecho.data.api.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(private val repository: MainRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()
    
    private val _title = MutableStateFlow<String?>(null)
    val title: StateFlow<String?> = _title.asStateFlow()

    fun loadPlaylist(id: Long, forceRefresh: Boolean = false) {
        _isLoading.value = true
        viewModelScope.launch {
            _songs.value = repository.getPlaylistTracks(id, forceRefresh)
            _title.value = repository.getCachedPlaylistTitle(id)
            _isLoading.value = false
        }
    }

    fun loadAlbum(id: Long, forceRefresh: Boolean = false) {
        _isLoading.value = true
        viewModelScope.launch {
            _songs.value = repository.getAlbumTracks(id) // Not cached yet, but can be
            _title.value = repository.getCachedAlbumTitle(id)
            _isLoading.value = false
        }
    }

    fun loadDjRadio(id: Long, forceRefresh: Boolean = false) {
        _isLoading.value = true
        viewModelScope.launch {
            _songs.value = repository.getDjRadioPrograms(id) // Not cached yet
            _title.value = "播客" // Default for DjRadio, as we haven't cached its title
            _isLoading.value = false
        }
    }

    fun loadLiked(forceRefresh: Boolean = false) {
        _isLoading.value = true
        viewModelScope.launch {
            val profile = repository.getLoginStatus()
            if (profile != null) {
                val pl = repository.getUserPlaylists(profile.userId)
                val likedId = pl.firstOrNull()?.id
                if (likedId != null) {
                    _songs.value = repository.getPlaylistTracks(likedId, forceRefresh)
                    _title.value = repository.getCachedPlaylistTitle(likedId) ?: "我喜欢"
                }
            }
            _isLoading.value = false
        }
    }
}
