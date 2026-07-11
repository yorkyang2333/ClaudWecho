package com.yorkyang2333.claudwecho.service

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.yorkyang2333.claudwecho.data.PlaybackStateManager
import org.koin.android.ext.android.inject

class PlaybackService : MediaSessionService() {
    private val player: ExoPlayer by inject()
    private val playbackStateManager: PlaybackStateManager by inject()
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
            
        player.setAudioAttributes(audioAttributes, true)
        player.repeatMode = playbackStateManager.getRepeatMode()
        player.shuffleModeEnabled = playbackStateManager.getShuffleMode()
        
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO &&
                    !playbackStateManager.isPersonalFmMode.value &&
                    player.repeatMode == Player.REPEAT_MODE_OFF &&
                    !player.shuffleModeEnabled
                ) {
                    player.pause()
                }
            }
        })
        
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(object : MediaSession.Callback {
                private val addNextCommand = SessionCommand(
                    PlaybackCommands.ACTION_ADD_NEXT,
                    android.os.Bundle.EMPTY
                )

                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                        .buildUpon()
                        .add(addNextCommand)
                        .build()
                    return MediaSession.ConnectionResult.accept(
                        sessionCommands,
                        MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
                    )
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: android.os.Bundle
                ): com.google.common.util.concurrent.ListenableFuture<SessionResult> {
                    if (customCommand.customAction != PlaybackCommands.ACTION_ADD_NEXT) {
                        return super.onCustomCommand(session, controller, customCommand, args)
                    }

                    val mediaItemBundle = args.getBundle(PlaybackCommands.EXTRA_MEDIA_ITEM)
                        ?: return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE))
                    val mediaItem = try {
                        MediaItem.fromBundle(mediaItemBundle)
                    } catch (_: Exception) {
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE))
                    }
                    val insertIndex = args
                        .getInt(PlaybackCommands.EXTRA_INSERT_INDEX, player.currentMediaItemIndex + 1)
                        .coerceIn(0, player.mediaItemCount)
                    player.addMediaItem(insertIndex, mediaItem)
                    if (player.shuffleModeEnabled) {
                        val currentIndex = player.currentMediaItemIndex
                        val shuffleIndices = buildList {
                            // Keep the requested song immediately after the
                            // current one, then continue with a random order.
                            add(currentIndex)
                            add(insertIndex)
                            addAll(
                                (0 until player.mediaItemCount)
                                    .filter { it != currentIndex && it != insertIndex }
                                    .shuffled()
                            )
                        }
                        player.setShuffleOrder(
                            DefaultShuffleOrder(shuffleIndices.toIntArray(), System.nanoTime())
                        )
                    }
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            })
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
