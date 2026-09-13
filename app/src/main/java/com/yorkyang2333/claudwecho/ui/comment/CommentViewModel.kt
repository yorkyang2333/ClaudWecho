package com.yorkyang2333.claudwecho.ui.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorkyang2333.claudwecho.data.MainRepository
import com.yorkyang2333.claudwecho.data.api.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CommentTab {
    HOT, ALL
}

data class CommentUiState(
    val selectedTab: CommentTab = CommentTab.HOT,
    val hotComments: List<Comment> = emptyList(),
    val allComments: List<Comment> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isHotLoadingMore: Boolean = false,
    val isAllLoadingMore: Boolean = false,
    val hasMoreHot: Boolean = false,
    val hasMoreAll: Boolean = false,
    val totalCount: Int = 0,
    val error: String? = null
)

class CommentViewModel(private val repository: MainRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState: StateFlow<CommentUiState> = _uiState.asStateFlow()

    private var currentSongId: Long? = null

    fun load(songId: Long) {
        if (currentSongId == songId && !_uiState.value.isInitialLoading && _uiState.value.error == null) {
            return
        }
        currentSongId = songId
        _uiState.update { 
            it.copy(
                isInitialLoading = true,
                error = null,
                hotComments = emptyList(),
                allComments = emptyList()
            ) 
        }

        viewModelScope.launch {
            try {
                val response = repository.getMusicComments(id = songId, limit = 20, offset = 0)
                if (response != null) {
                    val initialHot = response.hotComments ?: emptyList()
                    val initialAll = response.comments ?: emptyList()
                    val total = response.total
                    val hasMoreAll = response.more
                    val hasMoreHot = response.moreHot || initialHot.size >= 15

                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            hotComments = initialHot,
                            allComments = initialAll,
                            totalCount = total,
                            hasMoreHot = hasMoreHot,
                            hasMoreAll = hasMoreAll,
                            // If hot comments is empty, default to ALL tab for better UX
                            selectedTab = if (initialHot.isEmpty() && initialAll.isNotEmpty()) CommentTab.ALL else CommentTab.HOT,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            error = "未能获取评论"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        error = "获取评论失败: ${e.localizedMessage ?: "未知错误"}"
                    )
                }
            }
        }
    }

    fun selectTab(tab: CommentTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun loadMore() {
        val songId = currentSongId ?: return
        val currentState = _uiState.value

        when (currentState.selectedTab) {
            CommentTab.HOT -> {
                if (!currentState.hasMoreHot || currentState.isHotLoadingMore || currentState.isInitialLoading) return
                _uiState.update { it.copy(isHotLoadingMore = true) }
                viewModelScope.launch {
                    try {
                        val offset = currentState.hotComments.size
                        val hotRes = repository.getHotComments(id = songId, limit = 20, offset = offset)
                        if (hotRes != null) {
                            val newHot = hotRes.hotComments ?: emptyList()
                            val existingIds = currentState.hotComments.map { it.commentId }.toSet()
                            val filteredNew = newHot.filter { it.commentId !in existingIds }
                            _uiState.update {
                                it.copy(
                                    isHotLoadingMore = false,
                                    hotComments = it.hotComments + filteredNew,
                                    hasMoreHot = hotRes.hasMore && filteredNew.isNotEmpty()
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isHotLoadingMore = false) }
                        }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isHotLoadingMore = false) }
                    }
                }
            }
            CommentTab.ALL -> {
                if (!currentState.hasMoreAll || currentState.isAllLoadingMore || currentState.isInitialLoading) return
                _uiState.update { it.copy(isAllLoadingMore = true) }
                viewModelScope.launch {
                    try {
                        val offset = currentState.allComments.size
                        val res = repository.getMusicComments(id = songId, limit = 20, offset = offset)
                        if (res != null) {
                            val newComments = res.comments ?: emptyList()
                            val existingIds = currentState.allComments.map { it.commentId }.toSet()
                            val filteredNew = newComments.filter { it.commentId !in existingIds }
                            _uiState.update {
                                it.copy(
                                    isAllLoadingMore = false,
                                    allComments = it.allComments + filteredNew,
                                    hasMoreAll = res.more && filteredNew.isNotEmpty()
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isAllLoadingMore = false) }
                        }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isAllLoadingMore = false) }
                    }
                }
            }
        }
    }
}
