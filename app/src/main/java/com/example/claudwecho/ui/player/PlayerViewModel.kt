package com.example.claudwecho.ui.player

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.claudwecho.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.claudwecho.data.MainRepository
import kotlinx.coroutines.delay

data class LyricLine(val timeMs: Long, val text: String, var tText: String? = null)

class PlayerViewModel(
    private val context: Context,
    private val repository: MainRepository
) : ViewModel() {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    var player: MediaController? = null
        private set

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrackTitle = MutableStateFlow<String?>(null)
    val currentTrackTitle: StateFlow<String?> = _currentTrackTitle.asStateFlow()

    private val _lyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyrics: StateFlow<List<LyricLine>> = _lyrics.asStateFlow()

    private val _currentLyricIndex = MutableStateFlow(-1)
    val currentLyricIndex: StateFlow<Int> = _currentLyricIndex.asStateFlow()

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            player = controllerFuture?.get()
            setupPlayerListeners()
        }, MoreExecutors.directExecutor())
    }

    private var pendingUrl: String? = null
    private var pendingTitle: String? = null

    private fun setupPlayerListeners() {
        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentTrackTitle.value = mediaItem?.mediaMetadata?.title?.toString()
            }
        })
        
        // Play pending song if any
        pendingUrl?.let { url ->
            pendingTitle?.let { title ->
                playSong(url, title)
                pendingUrl = null
                pendingTitle = null
            }
        }

        // Poll current position for lyrics
        viewModelScope.launch {
            while (true) {
                if (player?.isPlaying == true) {
                    val pos = player?.currentPosition ?: 0L
                    val lrcList = _lyrics.value
                    if (lrcList.isNotEmpty()) {
                        val index = lrcList.indexOfLast { it.timeMs <= pos }
                        _currentLyricIndex.value = index
                    }
                }
                delay(200)
            }
        }
    }

    fun playOrPause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun skipToNext() {
        player?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        player?.seekToPreviousMediaItem()
    }

    fun playSong(url: String, title: String) {
        if (player == null) {
            pendingUrl = url
            pendingTitle = title
            return
        }
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(title)
                    .build()
            )
            .build()
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
        _currentTrackTitle.value = title // Optimistically set title
        
        _lyrics.value = emptyList()
        _currentLyricIndex.value = -1

        val idStr = url.substringAfter("id=").substringBefore(".mp3")
        val id = idStr.toLongOrNull()
        if (id != null) {
            viewModelScope.launch {
                val (lrc, tlyric) = repository.getLyrics(id)
                if (lrc != null) {
                    val lines = parseLyric(lrc)
                    if (tlyric != null) {
                        val tLines = parseLyric(tlyric)
                        tLines.forEach { tLine ->
                            val matchingLine = lines.find { it.timeMs == tLine.timeMs }
                            if (matchingLine != null) {
                                matchingLine.tText = tLine.text
                            }
                        }
                    }
                    _lyrics.value = lines
                }
            }
        }
    }

    private fun parseLyric(lrc: String): List<LyricLine> {
        val regex = Regex("\\[(\\d{2,}):(\\d{2})\\.(\\d{2,3})\\](.*)")
        return lrc.lines().mapNotNull { line ->
            val matchResult = regex.find(line)
            if (matchResult != null) {
                val (min, sec, ms, text) = matchResult.destructured
                val timeMs = min.toLong() * 60000 + sec.toLong() * 1000 + (if (ms.length == 2) ms.toLong() * 10 else ms.toLong())
                LyricLine(timeMs, text.trim())
            } else null
        }.filter { it.text.isNotEmpty() }
    }

    override fun onCleared() {
        super.onCleared()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
