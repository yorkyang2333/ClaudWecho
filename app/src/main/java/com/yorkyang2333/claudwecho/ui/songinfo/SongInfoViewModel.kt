package com.yorkyang2333.claudwecho.ui.songinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorkyang2333.claudwecho.data.MainRepository
import com.yorkyang2333.claudwecho.data.api.SongDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SongInfoUiState {
    data object Loading : SongInfoUiState
    data class Content(val song: SongDetail) : SongInfoUiState
    data object Error : SongInfoUiState
}

class SongInfoViewModel(private val repository: MainRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<SongInfoUiState>(SongInfoUiState.Loading)
    val uiState: StateFlow<SongInfoUiState> = _uiState.asStateFlow()

    fun load(songId: Long) {
        _uiState.value = SongInfoUiState.Loading
        viewModelScope.launch {
            _uiState.value = repository.getSongDetail(songId)
                ?.let(SongInfoUiState::Content)
                ?: SongInfoUiState.Error
        }
    }
}
