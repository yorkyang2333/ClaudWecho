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
    private val repository: MainRepository,
    private val playbackStateManager: com.example.claudwecho.data.PlaybackStateManager
) : ViewModel() {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    var player: MediaController? = null
        private set

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrackTitle = MutableStateFlow<String?>(null)
    val currentTrackTitle: StateFlow<String?> = _currentTrackTitle.asStateFlow()

    private val _currentArtistName = MutableStateFlow<String?>(null)
    val currentArtistName: StateFlow<String?> = _currentArtistName.asStateFlow()

    private val _lyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyrics: StateFlow<List<LyricLine>> = _lyrics.asStateFlow()

    private val _currentLyricIndex = MutableStateFlow(-1)
    val currentLyricIndex: StateFlow<Int> = _currentLyricIndex.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentArtworkUri = MutableStateFlow<String?>(null)
    val currentArtworkUri: StateFlow<String?> = _currentArtworkUri.asStateFlow()

    private val likedSongs = mutableSetOf<Long>()
    private val _isCurrentSongLiked = MutableStateFlow(false)
    val isCurrentSongLiked: StateFlow<Boolean> = _isCurrentSongLiked.asStateFlow()

    private val _isPersonalFmMode = MutableStateFlow(false)
    val isPersonalFmMode: StateFlow<Boolean> = _isPersonalFmMode.asStateFlow()

    init {
        initializeController()
        fetchLikedSongs()
    }

    private fun fetchLikedSongs() {
        viewModelScope.launch {
            val status = repository.getLoginStatus()
            if (status != null) {
                val list = repository.getLikeList(status.userId)
                likedSongs.clear()
                likedSongs.addAll(list)
                updateLikeStatus()
            }
        }
    }

    private fun updateLikeStatus() {
        val songId = player?.currentMediaItem?.mediaId?.toLongOrNull()
        if (songId != null) {
            _isCurrentSongLiked.value = likedSongs.contains(songId)
        }
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            player = controllerFuture?.get()
            setupPlayerListeners()
        }, MoreExecutors.directExecutor())
    }

    private var pendingSongs: List<com.example.claudwecho.data.api.Song>? = null
    private var pendingIndex: Int = 0
    private var currentPlaylist: List<com.example.claudwecho.data.api.Song> = emptyList()

    val repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val shuffleModeEnabled = MutableStateFlow(false)

    private fun setupPlayerListeners() {
        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentTrackTitle.value = mediaItem?.mediaMetadata?.title?.toString()
                _currentArtistName.value = mediaItem?.mediaMetadata?.artist?.toString()
                _currentArtworkUri.value = mediaItem?.mediaMetadata?.artworkUri?.toString()
                
                // Fetch lyrics for the new item
                val songId = mediaItem?.mediaId?.toLongOrNull()
                playbackStateManager.currentTrackId.value = songId
                updateLikeStatus()
                if (songId != null) {
                    val song = currentPlaylist.find { it.id == songId }
                    if (song != null) {
                        viewModelScope.launch {
                            repository.recordRecentPlay(song)
                        }
                    }
                    val currentIndex = player?.currentMediaItemIndex ?: 0
                    playbackStateManager.saveState(currentPlaylist, currentIndex)
                    fetchMoreFmIfNeeded()
                    _lyrics.value = emptyList()
                    _currentLyricIndex.value = -1
                    viewModelScope.launch {
                        val (lrc, tlyric) = repository.getLyrics(songId)
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
            override fun onRepeatModeChanged(mode: Int) {
                repeatMode.value = mode
            }
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                this@PlayerViewModel.shuffleModeEnabled.value = shuffleModeEnabled
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("PlayerViewModel", "Player error: ${error.message}", error)
            }
        })
        
        // Play pending playlist if any
        if (pendingSongs != null) {
            playPlaylist(pendingSongs!!, pendingIndex)
            pendingSongs = null
        } else {
            val lastPlaylist = playbackStateManager.getLastPlaylist()
            if (lastPlaylist != null && lastPlaylist.isNotEmpty()) {
                currentPlaylist = lastPlaylist
                val mediaItems = lastPlaylist.map { song ->
                    val artworkUri = song.displayAlbum?.picUrl?.let { android.net.Uri.parse(it) }
                    MediaItem.Builder()
                        .setMediaId(song.id.toString())
                        .setUri("netease://song/${song.id}")
                        .setMediaMetadata(
                            androidx.media3.common.MediaMetadata.Builder()
                                .setTitle(song.name)
                                .setArtist(song.displayArtists.joinToString { it.name })
                                .setArtworkUri(artworkUri)
                                .build()
                        )
                        .build()
                }
                val lastIndex = playbackStateManager.getLastIndex()
                player?.setMediaItems(mediaItems, lastIndex, androidx.media3.common.C.TIME_UNSET)
                player?.prepare()
            }
        }

        // Poll current position for lyrics
        viewModelScope.launch {
            while (true) {
                if (player?.isPlaying == true) {
                    val pos = player?.currentPosition ?: 0L
                    _currentPosition.value = pos
                    _duration.value = player?.duration?.coerceAtLeast(0L) ?: 0L
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

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    fun cyclePlaybackMode() {
        val player = player ?: return
        val currentRepeat = player.repeatMode
        val currentShuffle = player.shuffleModeEnabled

        if (!currentShuffle && currentRepeat == androidx.media3.common.Player.REPEAT_MODE_ALL) {
            // "列表循环" -> "单曲循环"
            player.repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
            player.shuffleModeEnabled = false
        } else if (!currentShuffle && currentRepeat == androidx.media3.common.Player.REPEAT_MODE_ONE) {
            // "单曲循环" -> "随机播放"
            player.repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
            player.shuffleModeEnabled = true
        } else if (currentShuffle) {
            // "随机播放" -> "播完即止"
            player.repeatMode = androidx.media3.common.Player.REPEAT_MODE_OFF
            player.shuffleModeEnabled = false
        } else {
            // "播完即止" or unknown -> "列表循环"
            player.repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
            player.shuffleModeEnabled = false
        }
    }

    fun toggleLikeCurrentSong() {
        val songId = player?.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        val currentLike = _isCurrentSongLiked.value
        val newLike = !currentLike
        
        // Optimistically update UI
        if (newLike) likedSongs.add(songId) else likedSongs.remove(songId)
        _isCurrentSongLiked.value = newLike

        viewModelScope.launch {
            val success = repository.likeSong(songId, newLike)
            if (!success) {
                // Revert on failure
                if (currentLike) likedSongs.add(songId) else likedSongs.remove(songId)
                _isCurrentSongLiked.value = currentLike
            }
        }
    }

    fun playPlaylist(songs: List<com.example.claudwecho.data.api.Song>, startIndex: Int) {
        _isPersonalFmMode.value = false
        currentPlaylist = songs
        if (player == null) {
            pendingSongs = songs
            pendingIndex = startIndex
            return
        }
        
        val mediaItems = songs.map { song ->
            val artworkUri = song.displayAlbum?.picUrl?.let { android.net.Uri.parse(it) }
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri("netease://song/${song.id}")
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.name)
                        .setArtist(song.displayArtists.joinToString { it.name })
                        .setArtworkUri(artworkUri)
                        .build()
                )
                .build()
        }
        
        player?.setMediaItems(mediaItems, startIndex, androidx.media3.common.C.TIME_UNSET)
        player?.prepare()
        player?.play()
    }

    fun playPersonalFm() {
        _isPersonalFmMode.value = true
        player?.repeatMode = Player.REPEAT_MODE_OFF
        player?.shuffleModeEnabled = false
        
        viewModelScope.launch {
            val songs = repository.getPersonalFm()
            if (songs.isNotEmpty()) {
                currentPlaylist = songs
                val mediaItems = songs.map { song ->
                    val artworkUri = song.displayAlbum?.picUrl?.let { android.net.Uri.parse(it) }
                    MediaItem.Builder()
                        .setMediaId(song.id.toString())
                        .setUri("netease://song/${song.id}")
                        .setMediaMetadata(
                            androidx.media3.common.MediaMetadata.Builder()
                                .setTitle(song.name)
                                .setArtist(song.displayArtists.joinToString { it.name })
                                .setArtworkUri(artworkUri)
                                .build()
                        )
                        .build()
                }
                player?.setMediaItems(mediaItems, 0, androidx.media3.common.C.TIME_UNSET)
                player?.prepare()
                player?.play()
            }
        }
    }

    fun trashCurrentFmSong() {
        if (!_isPersonalFmMode.value) return
        val player = player ?: return
        val currentSongId = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        
        viewModelScope.launch {
            repository.trashPersonalFm(currentSongId)
        }
        
        val currentIndex = player.currentMediaItemIndex
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        }
        // Remove the trashed item
        if (currentIndex in 0 until player.mediaItemCount) {
            player.removeMediaItem(currentIndex)
        }
        
        if (!player.isPlaying) {
            player.prepare()
            player.play()
        }
        
        fetchMoreFmIfNeeded()
    }

    private var isFetchingFm = false
    fun fetchMoreFmIfNeeded() {
        if (!_isPersonalFmMode.value || isFetchingFm) return
        val player = player ?: return
        
        // If we are at the last or second-to-last item, fetch more
        if (player.mediaItemCount - player.currentMediaItemIndex <= 2) {
            isFetchingFm = true
            viewModelScope.launch {
                val newSongs = repository.getPersonalFm()
                if (newSongs.isNotEmpty()) {
                    currentPlaylist = currentPlaylist + newSongs
                    val mediaItems = newSongs.map { song ->
                        val artworkUri = song.displayAlbum?.picUrl?.let { android.net.Uri.parse(it) }
                        MediaItem.Builder()
                            .setMediaId(song.id.toString())
                            .setUri("netease://song/${song.id}")
                            .setMediaMetadata(
                                androidx.media3.common.MediaMetadata.Builder()
                                    .setTitle(song.name)
                                    .setArtist(song.displayArtists.joinToString { it.name })
                                    .setArtworkUri(artworkUri)
                                    .build()
                            )
                            .build()
                    }
                    player.addMediaItems(mediaItems)
                    
                    if (player.playbackState == androidx.media3.common.Player.STATE_ENDED || player.playbackState == androidx.media3.common.Player.STATE_IDLE) {
                        player.prepare()
                        player.play()
                    }
                }
                isFetchingFm = false
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
