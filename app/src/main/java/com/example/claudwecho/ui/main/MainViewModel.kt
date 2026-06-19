package com.example.claudwecho.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.claudwecho.data.MainRepository
import com.example.claudwecho.data.api.Playlist
import com.example.claudwecho.data.api.Song
import com.example.claudwecho.data.api.UserProfile
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
            val profile = repository.getLoginStatus()
            _userProfile.value = profile
            
            if (profile != null) {
                // User is logged in
                _dailySongs.value = repository.getDailyRecommendSongs()
                _playlists.value = repository.getUserPlaylists(profile.userId)
            } else {
                // Not logged in, fetch Hot Songs instead
                _dailySongs.value = repository.getHotSongs()
            }
            isInitialized = true
            _isLoading.value = false
        }
    }
}
