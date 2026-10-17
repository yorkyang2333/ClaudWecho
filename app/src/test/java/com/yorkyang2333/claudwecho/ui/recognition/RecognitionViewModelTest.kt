package com.yorkyang2333.claudwecho.ui.recognition

import com.yorkyang2333.claudwecho.data.api.Song
import com.yorkyang2333.claudwecho.data.recognition.AudioFingerprint
import com.yorkyang2333.claudwecho.data.recognition.AudioRecorder
import com.yorkyang2333.claudwecho.data.recognition.RECOGNITION_SAMPLE_COUNT
import com.yorkyang2333.claudwecho.data.recognition.RecognitionRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RecognitionViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val recorder = FakeRecorder()
    private val fingerprint = AudioFingerprint { "fingerprint" }

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun completesOnceAndPausesBeforeRecording() = runTest(dispatcher) {
        var pauses = 0
        var requests = 0
        val songs = listOf(Song(1, "Song"))
        val vm = RecognitionViewModel(recorder, fingerprint, RecognitionRepository {
            assertEquals("fingerprint", it)
            requests++
            songs
        })
        vm.start { pauses++ }
        vm.start { pauses++ }
        advanceUntilIdle()
        assertEquals(1, pauses)
        assertEquals(1, recorder.recordings)
        assertEquals(1, requests)
        assertEquals(songs, vm.state.value.songs)
        assertEquals(RecognitionStage.RESULT, vm.state.value.stage)
    }

    @Test fun emptyResultIsDistinctFromNetworkFailureAndCanRetry() = runTest(dispatcher) {
        var fail = false
        val vm = RecognitionViewModel(recorder, fingerprint, RecognitionRepository {
            if (fail) throw IOException()
            emptyList()
        })
        vm.start {}
        advanceUntilIdle()
        assertEquals(RecognitionStage.RESULT, vm.state.value.stage)
        assertTrue(vm.state.value.songs.isEmpty())
        fail = true
        vm.start {}
        advanceUntilIdle()
        assertEquals(RecognitionStage.ERROR, vm.state.value.stage)
        assertTrue(vm.state.value.message!!.contains("网络"))
        fail = false
        vm.start {}
        advanceUntilIdle()
        assertEquals(RecognitionStage.RESULT, vm.state.value.stage)
    }

    @Test fun cancelStopsMicrophoneAndDoesNotSubmitAudio() = runTest(dispatcher) {
        recorder.waitForCancellation = true
        var requests = 0
        val vm = RecognitionViewModel(recorder, fingerprint, RecognitionRepository { requests++; emptyList() })
        vm.start {}
        runCurrent()
        vm.cancel()
        advanceUntilIdle()
        assertEquals(1, recorder.stops)
        assertEquals(0, requests)
        assertEquals(RecognitionStage.IDLE, vm.state.value.stage)
    }

    @Test fun lateNativeResultCannotOverwriteCancelledSession() = runTest(dispatcher) {
        val completed = CompletableDeferred<Unit>()
        var requests = 0
        val slowFingerprint = AudioFingerprint {
            withContext(NonCancellable) { completed.await(); "late" }
        }
        val vm = RecognitionViewModel(recorder, slowFingerprint, RecognitionRepository { requests++; emptyList() })
        vm.start {}
        runCurrent()
        assertEquals(RecognitionStage.FINGERPRINTING, vm.state.value.stage)
        vm.cancel()
        completed.complete(Unit)
        advanceUntilIdle()
        assertEquals(0, requests)
        assertEquals(RecognitionStage.IDLE, vm.state.value.stage)
    }

    @Test fun fingerprintTimeoutBecomesRetryableError() = runTest(dispatcher) {
        val vm = RecognitionViewModel(recorder, AudioFingerprint { awaitCancellation() }, RecognitionRepository { emptyList() })
        vm.start {}
        advanceUntilIdle()
        assertEquals(RecognitionStage.ERROR, vm.state.value.stage)
        assertTrue(vm.state.value.message!!.contains("超时"))
    }

    private class FakeRecorder : AudioRecorder {
        var recordings = 0
        var stops = 0
        var waitForCancellation = false
        override suspend fun record(onProgress: (Float) -> Unit): FloatArray {
            recordings++
            onProgress(0.5f)
            if (waitForCancellation) awaitCancellation()
            return FloatArray(RECOGNITION_SAMPLE_COUNT)
        }
        override fun stop() { stops++ }
    }
}
