package com.yorkyang2333.claudwecho.data

import android.content.Context
import android.content.SharedPreferences
import androidx.media3.exoplayer.ExoPlayer
import com.yorkyang2333.claudwecho.ClaudWechoApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SleepTimerManager(
    private val context: Context,
    private val playerProvider: () -> ExoPlayer?
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sleep_timer_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _durationMinutes = MutableStateFlow(prefs.getInt("duration_minutes", 15))
    val durationMinutes: StateFlow<Int> = _durationMinutes.asStateFlow()

    private val _finishCurrentSong = MutableStateFlow(prefs.getBoolean("finish_current_song", false))
    val finishCurrentSong: StateFlow<Boolean> = _finishCurrentSong.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _isWaitingForSongEnd = MutableStateFlow(false)
    val isWaitingForSongEnd: StateFlow<Boolean> = _isWaitingForSongEnd.asStateFlow()

    fun setDuration(minutes: Int) {
        val clamped = minutes.coerceIn(1, 1440)
        _durationMinutes.value = clamped
        prefs.edit().putInt("duration_minutes", clamped).apply()
        if (_isEnabled.value) {
            // Restart timer with new duration
            startCountdown()
        }
    }

    fun setFinishCurrentSong(finish: Boolean) {
        _finishCurrentSong.value = finish
        prefs.edit().putBoolean("finish_current_song", finish).apply()
    }

    fun setEnabled(enabled: Boolean) {
        if (enabled) {
            _isEnabled.value = true
            _isWaitingForSongEnd.value = false
            startCountdown()
        } else {
            _isEnabled.value = false
            _isWaitingForSongEnd.value = false
            _remainingSeconds.value = 0L
            timerJob?.cancel()
            timerJob = null
        }
    }

    private fun startCountdown() {
        timerJob?.cancel()
        var remainingMillis = _durationMinutes.value * 60 * 1000L
        _remainingSeconds.value = _durationMinutes.value * 60L

        timerJob = scope.launch {
            var lastTime = System.currentTimeMillis()
            while (isActive && _isEnabled.value) {
                delay(500)
                val now = System.currentTimeMillis()
                val delta = now - lastTime
                lastTime = now

                val player = playerProvider()
                val isPlaying = player?.isPlaying == true

                if (isPlaying) {
                    remainingMillis -= delta
                    if (remainingMillis <= 0L) {
                        _remainingSeconds.value = 0L
                        onTimerExpired()
                        break
                    } else {
                        _remainingSeconds.value = (remainingMillis + 999) / 1000
                    }
                }
            }
        }
    }

    private fun onTimerExpired() {
        val player = playerProvider()
        val isPlaying = player?.isPlaying == true

        if (_finishCurrentSong.value && isPlaying) {
            _isWaitingForSongEnd.value = true
        } else {
            triggerAppExit()
        }
    }

    fun onSongFinishedNaturally() {
        if (_isEnabled.value && _isWaitingForSongEnd.value) {
            triggerAppExit()
        }
    }

    fun onUserManualPauseOrTrackChange() {
        if (_isEnabled.value && _isWaitingForSongEnd.value) {
            // "期间手动暂停或切歌会取消定时关闭"
            setEnabled(false)
        }
    }

    fun triggerAppExit() {
        scope.launch {
            setEnabled(false)
            val player = playerProvider()
            try {
                player?.stop()
                player?.clearMediaItems()
            } catch (e: Exception) {
                // ignore
            }
            ClaudWechoApp.exitApplication(context)
        }
    }
}

