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
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION &&
                    !playbackStateManager.isPersonalFmMode.value &&
                    player.repeatMode == Player.REPEAT_MODE_OFF &&
                    !player.shuffleModeEnabled
                ) {
                    player.pause()
                    player.seekTo(oldPosition.mediaItemIndex, 0L)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED &&
                    !playbackStateManager.isPersonalFmMode.value &&
                    player.repeatMode == Player.REPEAT_MODE_OFF &&
                    !player.shuffleModeEnabled
                ) {
                    player.pause()
                    player.seekTo(0L)
                }
            }
        })
        
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(object : MediaSession.Callback {
                private val addNextCommand = SessionCommand(
                    PlaybackCommands.ACTION_ADD_NEXT,
                    android.os.Bundle.EMPTY
                )
                private val playPlaylistCommand = SessionCommand(
                    PlaybackCommands.ACTION_PLAY_PLAYLIST,
                    android.os.Bundle.EMPTY
                )

                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                        .buildUpon()
                        .add(addNextCommand)
                        .add(playPlaylistCommand)
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
                    when (customCommand.customAction) {
                        PlaybackCommands.ACTION_ADD_NEXT -> {
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
                            prioritizeShuffleOrder(player.currentMediaItemIndex, insertIndex)
                            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                        }
                        PlaybackCommands.ACTION_PLAY_PLAYLIST -> {
                            val itemBundles = args.getParcelableArrayList<android.os.Bundle>(
                                PlaybackCommands.EXTRA_MEDIA_ITEMS
                            ) ?: return Futures.immediateFuture(
                                SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE)
                            )
                            val mediaItems = try {
                                itemBundles.map(MediaItem::fromBundle)
                            } catch (_: Exception) {
                                return Futures.immediateFuture(
                                    SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE)
                                )
                            }
                            if (mediaItems.isEmpty()) {
                                return Futures.immediateFuture(
                                    SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE)
                                )
                            }
                            val startIndex = args
                                .getInt(PlaybackCommands.EXTRA_START_INDEX, 0)
                                .coerceIn(0, mediaItems.lastIndex)
                            player.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
                            player.repeatMode = args.getInt(
                                PlaybackCommands.EXTRA_REPEAT_MODE,
                                Player.REPEAT_MODE_ALL
                            )
                            player.shuffleModeEnabled = args.getBoolean(
                                PlaybackCommands.EXTRA_SHUFFLE_ENABLED,
                                false
                            )
                            prioritizeShuffleOrder(startIndex)
                            player.prepare()
                            player.play()
                            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                        }
                        else -> return super.onCustomCommand(session, controller, customCommand, args)
                    }
                }

                private fun prioritizeShuffleOrder(vararg priorityIndices: Int) {
                    if (!player.shuffleModeEnabled || player.mediaItemCount == 0) return

                    val priority = priorityIndices
                        .filter { it in 0 until player.mediaItemCount }
                        .distinct()
                    val shuffleIndices = priority + (0 until player.mediaItemCount)
                        .filter { it !in priority }
                        .shuffled()
                    player.setShuffleOrder(
                        DefaultShuffleOrder(shuffleIndices.toIntArray(), System.nanoTime())
                    )
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
