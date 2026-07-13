package com.yorkyang2333.claudwecho.ui.player

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.yorkyang2333.claudwecho.service.PlaybackService
import com.yorkyang2333.claudwecho.service.PlaybackCommands
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.yorkyang2333.claudwecho.data.MainRepository
import com.yorkyang2333.claudwecho.data.api.Playlist
import kotlinx.coroutines.delay

data class LyricLine(val timeMs: Long, val text: String, var tText: String? = null)

class PlayerViewModel(
    private val context: Context,
    private val repository: MainRepository,
    private val playbackStateManager: com.yorkyang2333.claudwecho.data.PlaybackStateManager
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
    private var lastSeekTimestamp = 0L

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentArtworkUri = MutableStateFlow<String?>(null)
    val currentArtworkUri: StateFlow<String?> = _currentArtworkUri.asStateFlow()

    private val likedSongs = mutableSetOf<Long>()
    private val _isCurrentSongLiked = MutableStateFlow(false)
    val isCurrentSongLiked: StateFlow<Boolean> = _isCurrentSongLiked.asStateFlow()

    private val _isPersonalFmMode = playbackStateManager.isPersonalFmMode
    val isPersonalFmMode: StateFlow<Boolean> = _isPersonalFmMode.asStateFlow()

    private val _isCurrentSongPodcast = MutableStateFlow(false)
    val isCurrentSongPodcast: StateFlow<Boolean> = _isCurrentSongPodcast.asStateFlow()

    private val _isCurrentSongVip = MutableStateFlow(false)
    val isCurrentSongVip: StateFlow<Boolean> = _isCurrentSongVip.asStateFlow()

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

    private var pendingSongs: List<com.yorkyang2333.claudwecho.data.api.Song>? = null
    private var pendingIndex: Int = 0

    val repeatMode = MutableStateFlow(playbackStateManager.getRepeatMode())
    val shuffleModeEnabled = MutableStateFlow(playbackStateManager.getShuffleMode())

    private fun setupPlayerListeners() {
        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO &&
                    !_isPersonalFmMode.value &&
                    player?.repeatMode == Player.REPEAT_MODE_OFF &&
                    player?.shuffleModeEnabled == false
                ) {
                    player?.pause()
                }
                val currentOriginalIndex = player?.currentMediaItemIndex ?: 0
                _currentMediaItemIndex.value = currentOriginalIndex
                _displayCurrentIndex.value = displayToOriginalMap.indexOf(currentOriginalIndex).takeIf { it >= 0 } ?: 0
                _currentTrackTitle.value = mediaItem?.mediaMetadata?.title?.toString()
                _currentArtistName.value = mediaItem?.mediaMetadata?.artist?.toString()
                _currentArtworkUri.value = mediaItem?.mediaMetadata?.artworkUri?.toString()
                
                // Fetch lyrics for the new item
                val songId = mediaItem?.mediaId?.toLongOrNull()
                playbackStateManager.currentTrackId.value = songId
                updateLikeStatus()
                val currentIndex = player?.currentMediaItemIndex ?: 0
                _currentMediaItemIndex.value = currentIndex
                if (songId != null) {
                    val song = _currentPlaylist.value.find { it.id == songId }
                    if (song != null) {
                        _isCurrentSongPodcast.value = song.isPodcast
                        _isCurrentSongVip.value = (song.fee == 1)
                        viewModelScope.launch {
                            repository.recordRecentPlay(song)
                        }
                    } else {
                        _isCurrentSongPodcast.value = false
                        _isCurrentSongVip.value = false
                    }
                    playbackStateManager.saveState(_currentPlaylist.value, currentIndex)
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
                } else {
                    _isCurrentSongPodcast.value = false
                    _isCurrentSongVip.value = false
                }
            }
            override fun onRepeatModeChanged(mode: Int) {
                repeatMode.value = mode
                playbackStateManager.savePlaybackMode(mode, player?.shuffleModeEnabled ?: false)
            }
            override fun onShuffleModeEnabledChanged(isShuffleEnabled: Boolean) {
                this@PlayerViewModel.shuffleModeEnabled.value = isShuffleEnabled
                playbackStateManager.savePlaybackMode(player?.repeatMode ?: androidx.media3.common.Player.REPEAT_MODE_ALL, isShuffleEnabled)
                player?.currentTimeline?.let { updateDisplayPlaylist(it) }
            }
            override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                updateDisplayPlaylist(timeline)
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
                _currentPlaylist.value = lastPlaylist
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
                player?.repeatMode = playbackStateManager.getRepeatMode()
                player?.shuffleModeEnabled = playbackStateManager.getShuffleMode()
                player?.prepare()
            }
        }

        // Poll current position for lyrics
        viewModelScope.launch {
            while (true) {
                if (player?.isPlaying == true) {
                    val pos = player?.currentPosition ?: 0L
                    val timeSinceSeek = System.currentTimeMillis() - lastSeekTimestamp
                    if (timeSinceSeek > 4000L || Math.abs(pos - _currentPosition.value) < 1500L) {
                        _currentPosition.value = pos
                    }
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

    private val _currentPlaylist = MutableStateFlow<List<com.yorkyang2333.claudwecho.data.api.Song>>(emptyList())
    val currentPlaylist: StateFlow<List<com.yorkyang2333.claudwecho.data.api.Song>> = _currentPlaylist.asStateFlow()

    private val _displayPlaylist = MutableStateFlow<List<com.yorkyang2333.claudwecho.data.api.Song>>(emptyList())
    val displayPlaylist: StateFlow<List<com.yorkyang2333.claudwecho.data.api.Song>> = _displayPlaylist.asStateFlow()

    private val _currentMediaItemIndex = MutableStateFlow(0)
    val currentMediaItemIndex: StateFlow<Int> = _currentMediaItemIndex.asStateFlow()

    private val _displayCurrentIndex = MutableStateFlow(0)
    val displayCurrentIndex: StateFlow<Int> = _displayCurrentIndex.asStateFlow()
    
    private var displayToOriginalMap = listOf<Int>()
    
    private fun updateDisplayPlaylist(timeline: androidx.media3.common.Timeline) {
        val originalList = _currentPlaylist.value
        if (originalList.isEmpty() || timeline.isEmpty) {
            _displayPlaylist.value = emptyList()
            _displayCurrentIndex.value = 0
            displayToOriginalMap = emptyList()
            return
        }

        val isShuffle = player?.shuffleModeEnabled ?: false
        val displayList = mutableListOf<com.yorkyang2333.claudwecho.data.api.Song>()
        val indexMap = mutableListOf<Int>()

        var index = timeline.getFirstWindowIndex(isShuffle)
        while (index != androidx.media3.common.C.INDEX_UNSET) {
            if (index in originalList.indices) {
                displayList.add(originalList[index])
                indexMap.add(index)
            }
            index = timeline.getNextWindowIndex(index, androidx.media3.common.Player.REPEAT_MODE_OFF, isShuffle)
        }

        _displayPlaylist.value = displayList
        displayToOriginalMap = indexMap
        
        val currentOriginalIndex = player?.currentMediaItemIndex ?: 0
        _displayCurrentIndex.value = displayToOriginalMap.indexOf(currentOriginalIndex).takeIf { it >= 0 } ?: 0
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
    fun removeQueueItem(displayIndex: Int) {
        val player = player ?: return
        if (displayIndex in displayToOriginalMap.indices) {
            val originalIndex = displayToOriginalMap[displayIndex]
            player.removeMediaItem(originalIndex)
            val currentList = _currentPlaylist.value.toMutableList()
            if (originalIndex in currentList.indices) {
                currentList.removeAt(originalIndex)
                _currentPlaylist.value = currentList
                updateDisplayPlaylist(player.currentTimeline)
                playbackStateManager.saveState(currentList, player.currentMediaItemIndex)
            }
        }
    }
    
    fun playQueueItem(displayIndex: Int) {
        if (displayIndex in displayToOriginalMap.indices) {
            val originalIndex = displayToOriginalMap[displayIndex]
            player?.seekToDefaultPosition(originalIndex)
            player?.play()
        }
    }
    
    fun clearQueue() {
        player?.clearMediaItems()
        _currentPlaylist.value = emptyList()
        playbackStateManager.saveState(emptyList(), 0)
    }

    fun seekTo(positionMs: Long) {
        _currentPosition.value = positionMs
        lastSeekTimestamp = System.currentTimeMillis()
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

    fun currentSongId(): Long? = player?.currentMediaItem?.mediaId?.toLongOrNull()

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

    fun playNext(song: com.yorkyang2333.claudwecho.data.api.Song) {
        val player = player
        val currentList = _currentPlaylist.value.toMutableList()
        
        if (player == null || currentList.isEmpty()) {
            playPlaylist(listOf(song), 0)
            return
        }

        _isPersonalFmMode.value = false
        val currentIndex = player.currentMediaItemIndex
        val insertIndex = (currentIndex + 1).coerceIn(0, currentList.size)

        val artworkUri = song.displayAlbum?.picUrl?.let { android.net.Uri.parse(it) }
        val mediaItem = MediaItem.Builder()
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

        val command = SessionCommand(PlaybackCommands.ACTION_ADD_NEXT, android.os.Bundle.EMPTY)
        val args = android.os.Bundle().apply {
            putBundle(PlaybackCommands.EXTRA_MEDIA_ITEM, mediaItem.toBundleIncludeLocalConfiguration())
            putInt(PlaybackCommands.EXTRA_INSERT_INDEX, insertIndex)
        }

        // Queue mutations run in PlaybackService, which owns the ExoPlayer. This
        // avoids losing the command while a MediaController is reconnecting.
        val commandFuture = player.sendCustomCommand(command, args)
        commandFuture.addListener({
            try {
                val result = commandFuture.get()
                if (result.resultCode == androidx.media3.session.SessionResult.RESULT_SUCCESS) {
                    currentList.add(insertIndex, song)
                    _currentPlaylist.value = currentList
                    updateDisplayPlaylist(player.currentTimeline)
                    playbackStateManager.saveState(currentList, currentIndex)
                } else {
                    android.util.Log.e("PlayerViewModel", "playNext: service rejected queue insertion")
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayerViewModel", "playNext: service queue insertion failed", e)
            }
        }, MoreExecutors.directExecutor())
    }

    fun playPlaylist(songs: List<com.yorkyang2333.claudwecho.data.api.Song>, startIndex: Int) {
        _isPersonalFmMode.value = false
        _currentPlaylist.value = songs
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
        
        val command = SessionCommand(PlaybackCommands.ACTION_PLAY_PLAYLIST, android.os.Bundle.EMPTY)
        val args = android.os.Bundle().apply {
            putParcelableArrayList(
                PlaybackCommands.EXTRA_MEDIA_ITEMS,
                ArrayList(mediaItems.map { it.toBundleIncludeLocalConfiguration() })
            )
            putInt(PlaybackCommands.EXTRA_START_INDEX, startIndex)
            putInt(PlaybackCommands.EXTRA_REPEAT_MODE, playbackStateManager.getRepeatMode())
            putBoolean(PlaybackCommands.EXTRA_SHUFFLE_ENABLED, playbackStateManager.getShuffleMode())
        }
        player?.sendCustomCommand(command, args)
    }

    fun playPersonalFm() {
        _isPersonalFmMode.value = true
        player?.repeatMode = Player.REPEAT_MODE_OFF
        player?.shuffleModeEnabled = false
        
        viewModelScope.launch {
            val songs = repository.getPersonalFm()
            if (songs.isNotEmpty()) {
                _currentPlaylist.value = songs
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
                    _currentPlaylist.value = _currentPlaylist.value + newSongs
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

    private val _createdPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val createdPlaylists: StateFlow<List<Playlist>> = _createdPlaylists.asStateFlow()

    fun fetchCreatedPlaylists() {
        viewModelScope.launch {
            val profile = repository.getLoginStatus()
            if (profile != null) {
                val pl = repository.getUserPlaylists(profile.userId)
                val firstId = pl.firstOrNull()?.id
                _createdPlaylists.value = pl.filter {
                    it.isCreatedBy(profile.userId) && it.id != firstId
                }
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.addTracksToPlaylist(playlistId, listOf(songId))
            onResult(success)
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
