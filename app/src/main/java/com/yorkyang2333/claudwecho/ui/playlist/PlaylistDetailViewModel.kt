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

    fun loadPlaylist(id: Long, forceRefresh: Boolean = false) {
        _isLoading.value = true
        viewModelScope.launch {
            _songs.value = repository.getPlaylistTracks(id, forceRefresh)
            _isLoading.value = false
        }
    }

    fun loadAlbum(id: Long, forceRefresh: Boolean = false) {
        _isLoading.value = true
        viewModelScope.launch {
            _songs.value = repository.getAlbumTracks(id) // Not cached yet, but can be
            _isLoading.value = false
        }
    }

    fun loadDjRadio(id: Long, forceRefresh: Boolean = false) {
        _isLoading.value = true
        viewModelScope.launch {
            _songs.value = repository.getDjRadioPrograms(id) // Not cached yet
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
                }
            }
            _isLoading.value = false
        }
    }
}
