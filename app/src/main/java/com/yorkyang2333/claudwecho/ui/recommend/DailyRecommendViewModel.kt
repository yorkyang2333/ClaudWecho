package com.yorkyang2333.claudwecho.ui.recommend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorkyang2333.claudwecho.data.MainRepository
import com.yorkyang2333.claudwecho.data.api.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DailyRecommendViewModel(private val repository: MainRepository) : ViewModel() {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            val recommendSongs = repository.getDailyRecommendSongs(forceRefresh)
            _songs.value = recommendSongs
            _isLoading.value = false
        }
    }
}
