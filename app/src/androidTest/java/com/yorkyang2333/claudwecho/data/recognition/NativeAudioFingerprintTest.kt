package com.yorkyang2333.claudwecho.data.recognition

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder

@RunWith(AndroidJUnit4::class)
class NativeAudioFingerprintTest {
    @Test
    fun fingerprintsMatchUpstreamAcrossRepeatedCalls() {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        repeat(3) {
            for (name in listOf("silence", "mixture")) {
                val input = assets.open("recognition/$name.f32").use { it.readBytes() }
                val samples = FloatArray(RECOGNITION_SAMPLE_COUNT)
                ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(samples)
                val expected = assets.open("recognition/$name.fp").use { it.readBytes() }
                assertArrayEquals(name, expected, NativeAudioFingerprint.generate(samples))
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsIncompleteAudio() {
        NativeAudioFingerprint.generate(FloatArray(100))
    }
}
