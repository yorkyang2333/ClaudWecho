package com.yorkyang2333.claudwecho.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorkyang2333.claudwecho.data.MainRepository
import com.yorkyang2333.claudwecho.data.api.Playlist
import com.yorkyang2333.claudwecho.data.api.Song
import com.yorkyang2333.claudwecho.data.api.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MainRepository) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _dailySongs = MutableStateFlow<List<Song>>(emptyList())
    val dailySongs: StateFlow<List<Song>> = _dailySongs.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var isInitialized = false

    fun loadData(forceRefresh: Boolean = false) {
        if (isInitialized && !forceRefresh) return
        
        viewModelScope.launch {
            _isLoading.value = true
            val profile = repository.getLoginStatus(forceRefresh)
            _userProfile.value = profile
            _isLoading.value = false // UI can show the menu now!
            
            // Fetch heavy data in background without blocking the UI
            launch {
                if (profile != null) {
                    _dailySongs.value = repository.getDailyRecommendSongs(forceRefresh)
                    _playlists.value = repository.getUserPlaylists(profile.userId, forceRefresh)
                } else {
                    _dailySongs.value = repository.getHotSongs(forceRefresh)
                }
            }
            isInitialized = true
        }
    }

    fun getLikedPlaylistId(onResult: (Long?) -> Unit) {
        viewModelScope.launch {
            if (_playlists.value.isNotEmpty()) {
                onResult(_playlists.value.firstOrNull()?.id)
                return@launch
            }
            
            val profile = _userProfile.value
            if (profile != null) {
                // If not loaded yet, fetch now
                val pl = repository.getUserPlaylists(profile.userId)
                _playlists.value = pl
                onResult(pl.firstOrNull()?.id)
            } else {
                onResult(null)
            }
        }
    }
}
