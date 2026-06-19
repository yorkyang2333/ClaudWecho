package com.example.claudwecho.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.claudwecho.data.MainRepository
import com.example.claudwecho.data.api.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(private val repository: MainRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    fun loadPlaylist(id: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            _songs.value = repository.getPlaylistTracks(id)
            _isLoading.value = false
        }
    }

    fun loadAlbum(id: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            _songs.value = repository.getAlbumTracks(id)
            _isLoading.value = false
        }
    }

    fun loadDjRadio(id: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            _songs.value = repository.getDjRadioPrograms(id)
            _isLoading.value = false
        }
    }
}
