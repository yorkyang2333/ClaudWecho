package com.yorkyang2333.claudwecho.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorkyang2333.claudwecho.data.MainRepository
import com.yorkyang2333.claudwecho.data.api.Album
import com.yorkyang2333.claudwecho.data.api.DjRadio
import com.yorkyang2333.claudwecho.data.api.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyCollectionViewModel(private val repository: MainRepository) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _djRadios = MutableStateFlow<List<DjRadio>>(emptyList())
    val djRadios: StateFlow<List<DjRadio>> = _djRadios.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val userProfile = repository.getLoginStatus(forceRefresh)
            if (userProfile != null) {
                // Fetch playlists
                val pl = repository.getUserPlaylists(userProfile.userId, forceRefresh)
                _playlists.value = pl
            }
            
            // Fetch albums
            val al = repository.getSubscribedAlbums(forceRefresh)
            _albums.value = al
            
            // Fetch djRadios
            val dj = repository.getSubscribedDjRadios(forceRefresh)
            _djRadios.value = dj

            _isLoading.value = false
        }
    }
}
