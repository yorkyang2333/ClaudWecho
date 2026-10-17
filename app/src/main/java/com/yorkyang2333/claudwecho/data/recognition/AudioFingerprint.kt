package com.yorkyang2333.claudwecho.data.recognition

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

fun interface AudioFingerprint {
    suspend fun generate(samples: FloatArray): String
}

object NativeAudioFingerprint {
    init { System.loadLibrary("audio_fingerprint") }
    external fun generate(samples: FloatArray): ByteArray
}

class LocalAudioFingerprint : AudioFingerprint {
    private val mutex = Mutex()

    override suspend fun generate(samples: FloatArray): String = mutex.withLock {
        withContext(Dispatchers.Default) {
            require(samples.size == RECOGNITION_SAMPLE_COUNT)
            currentCoroutineContext().ensureActive()
            val bytes = try {
                NativeAudioFingerprint.generate(samples)
            } catch (e: LinkageError) {
                throw IllegalStateException("当前设备暂不支持听歌识曲", e)
            } catch (e: IllegalStateException) {
                throw IllegalStateException("音频分析失败，请重试", e)
            }
            currentCoroutineContext().ensureActive()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
    }
}
