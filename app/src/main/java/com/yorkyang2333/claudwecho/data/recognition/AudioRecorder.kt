package com.yorkyang2333.claudwecho.data.recognition

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

const val RECOGNITION_DURATION_SECONDS = 3
const val RECOGNITION_SAMPLE_RATE = 8000
const val RECOGNITION_SAMPLE_COUNT = RECOGNITION_DURATION_SECONDS * RECOGNITION_SAMPLE_RATE

interface AudioRecorder {
    suspend fun record(onProgress: (Float) -> Unit): FloatArray
    fun stop()
}

class MicrophoneAudioRecorder(context: Context) : AudioRecorder {
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val lock = Any()
    private var activeRecorder: AudioRecord? = null

    @SuppressLint("MissingPermission")
    override suspend fun record(onProgress: (Float) -> Unit): FloatArray = withContext(Dispatchers.IO) {
        val minimumBuffer = AudioRecord.getMinBufferSize(
            RECOGNITION_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        check(minimumBuffer > 0) { "当前设备无法采集音频" }
        val source = if (audioManager.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED) == "true") {
            MediaRecorder.AudioSource.UNPROCESSED
        } else {
            MediaRecorder.AudioSource.VOICE_RECOGNITION
        }
        val recorder = AudioRecord.Builder()
            .setAudioSource(source)
            .setAudioFormat(AudioFormat.Builder().setSampleRate(RECOGNITION_SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
            .setBufferSizeInBytes(maxOf(minimumBuffer, 4096))
            .build()
        try {
            check(recorder.state == AudioRecord.STATE_INITIALIZED) { "麦克风初始化失败，请重试" }
            val recordingContext = currentCoroutineContext()
            synchronized(lock) {
                check(activeRecorder == null) { "麦克风正在使用中，请重试" }
                recordingContext.ensureActive()
                activeRecorder = recorder
                recorder.startRecording()
            }
            check(recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) { "无法使用麦克风，请稍后重试" }
            withTimeout(6000) {
                val samples = FloatArray(RECOGNITION_SAMPLE_COUNT)
                val buffer = ShortArray(1024)
                var captured = 0
                while (captured < samples.size) {
                    currentCoroutineContext().ensureActive()
                    val count = recorder.read(buffer, 0, minOf(buffer.size, samples.size - captured), AudioRecord.READ_NON_BLOCKING)
                    currentCoroutineContext().ensureActive()
                    check(count >= 0) { "录音中断，请重试" }
                    for (i in 0 until count) samples[captured + i] = buffer[i] / 32768f
                    captured += count
                    onProgress(captured.toFloat() / samples.size)
                    if (count == 0) delay(10)
                }
                samples
            }
        } finally {
            synchronized(lock) {
                if (activeRecorder === recorder) activeRecorder = null
                try {
                    if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) runCatching { recorder.stop() }
                } finally {
                    recorder.release()
                }
            }
        }
    }

    override fun stop() {
        synchronized(lock) {
            activeRecorder?.let {
                if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) runCatching { it.stop() }
            }
        }
    }
}
