package com.yorkyang2333.claudwecho.service

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
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
        
        mediaSession = MediaSession.Builder(this, player).build()
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
