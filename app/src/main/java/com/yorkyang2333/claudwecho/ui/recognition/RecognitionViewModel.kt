package com.yorkyang2333.claudwecho.ui.recognition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorkyang2333.claudwecho.data.api.Song
import com.yorkyang2333.claudwecho.data.recognition.AudioFingerprint
import com.yorkyang2333.claudwecho.data.recognition.AudioRecorder
import com.yorkyang2333.claudwecho.data.recognition.RecognitionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

enum class RecognitionStage { IDLE, RECORDING, FINGERPRINTING, QUERYING, RESULT, ERROR }

data class RecognitionState(
    val stage: RecognitionStage = RecognitionStage.IDLE,
    val progress: Float = 0f,
    val songs: List<Song> = emptyList(),
    val message: String? = null
) {
    val isBusy get() = stage in setOf(RecognitionStage.RECORDING, RecognitionStage.FINGERPRINTING, RecognitionStage.QUERYING)
}

class RecognitionViewModel(
    private val recorder: AudioRecorder,
    private val fingerprint: AudioFingerprint,
    private val repository: RecognitionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(RecognitionState())
    val state = _state.asStateFlow()
    private var operation: Job? = null
    private val generation = AtomicLong()

    @Synchronized
    fun start(onPausePlayback: () -> Unit) {
        if (_state.value.isBusy) return
        val token = generation.incrementAndGet()
        val previous = operation
        _state.value = RecognitionState(stage = RecognitionStage.RECORDING)
        onPausePlayback()
        operation = viewModelScope.launch {
            try {
                previous?.join()
                val samples = recorder.record { progress ->
                    synchronized(this@RecognitionViewModel) {
                        if (token == generation.get() && _state.value.stage == RecognitionStage.RECORDING) {
                            _state.value = RecognitionState(RecognitionStage.RECORDING, progress)
                        }
                    }
                }
                if (token != generation.get()) return@launch
                _state.value = RecognitionState(RecognitionStage.FINGERPRINTING)
                val audioFP = withTimeout(20000) { fingerprint.generate(samples) }
                if (token != generation.get()) return@launch
                _state.value = RecognitionState(RecognitionStage.QUERYING)
                val songs = withTimeout(45000) { repository.recognize(audioFP) }
                if (token == generation.get()) _state.value = RecognitionState(RecognitionStage.RESULT, songs = songs)
            } catch (e: TimeoutCancellationException) {
                fail(token, "识别超时，请重试")
            } catch (e: CancellationException) {
                throw e
            } catch (e: SecurityException) {
                fail(token, "请允许麦克风权限后重试")
            } catch (e: IOException) {
                fail(token, "网络连接失败，请检查网络和后端地址")
            } catch (e: Exception) {
                fail(token, if (e is IllegalStateException) e.message ?: "识别失败，请重试" else "识别失败，请检查后端服务后重试")
            }
        }
    }

    private fun fail(token: Long, message: String) {
        if (token == generation.get()) _state.value = RecognitionState(RecognitionStage.ERROR, message = message)
    }

    @Synchronized
    fun cancel() {
        generation.incrementAndGet()
        operation?.cancel()
        recorder.stop()
        if (_state.value.isBusy) _state.value = RecognitionState()
    }

    override fun onCleared() { cancel() }
}
