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

class SearchViewModel(
    private val repository: MainRepository,
    private val historyManager: LocalSearchHistoryManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        _searchHistory.value = historyManager.getHistory().take(5)
    }

    fun performSearch(query: String) {
        if (query.isBlank()) return
        
        historyManager.addQuery(query)
        loadHistory()

        _searchQuery.value = query
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val results = repository.searchSongs(query, limit = 30)
                _searchResults.value = results
                if (results.isEmpty()) {
                    _error.value = "未找到相关歌曲"
                }
            } catch (e: Exception) {
                _error.value = "搜索失败，请重试"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _error.value = null
        loadHistory()
    }

    fun clearHistory() {
        historyManager.clearHistory()
        loadHistory()
    }
}
