package com.yorkyang2333.claudwecho.data.recognition

import com.yorkyang2333.claudwecho.data.api.NeteaseApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class RecognitionRepositoryTest {
    @Test fun postsFingerprintAndHydratesCandidatesInMatchOrder() = runBlocking {
        val requests = mutableListOf<Request>()
        val repository = repository(requests) { request ->
            if (request.url.encodedPath == "/audio/match") {
                """{"code":200,"data":{"result":[{"song":{"id":2}},{"song":{"id":1}},{"song":{"id":2}},{"song":{"id":-1}},{"song":null}]}}"""
            } else {
                """{"code":200,"songs":[{"id":1,"name":"One","fee":1},{"id":2,"name":"Two","ar":[{"id":3,"name":"Artist"}]}]}"""
            }
        }
        val songs = repository.recognize("a+b/c==")
        assertEquals(listOf(2L, 1L), songs.map { it.id })
        assertEquals("Artist", songs.first().displayArtists.first().name)
        assertEquals(1, songs.last().fee)
        assertEquals("POST", requests.first().method)
        assertEquals("3", requests.first().url.queryParameter("duration"))
        assertEquals("a+b/c==", requests.first().url.queryParameter("audioFP"))
        assertNotNull(requests.first().url.queryParameter("timestamp"))
        assertEquals("2,1", requests.last().url.queryParameter("ids"))
    }

    @Test fun newRecordingDoesNotReuseCachedNoMatch() = runBlocking {
        val requests = mutableListOf<Request>()
        val cache = mutableMapOf<String, String>()
        val repository = repository(requests) { request ->
            if (request.url.encodedPath == "/audio/match") {
                // The upstream middleware caches by URL and cookies, ignoring the body.
                cache.getOrPut(request.url.toString()) {
                    val form = request.body as? FormBody
                    val bodyFingerprint = form?.let { body ->
                        (0 until body.size).firstOrNull { body.name(it) == "audioFP" }?.let(body::value)
                    }
                    if ((request.url.queryParameter("audioFP") ?: bodyFingerprint) == "silence") {
                        """{"code":200,"data":{"result":[]}}"""
                    } else {
                        """{"code":200,"data":{"result":[{"song":{"id":1}}]}}"""
                    }
                }
            } else {
                """{"code":200,"songs":[{"id":1,"name":"Song"}]}"""
            }
        }
        assertTrue(repository.recognize("silence").isEmpty())
        assertEquals(listOf(1L), repository.recognize("music").map { it.id })
        assertEquals(2, cache.size)
    }

    @Test fun absentOrEmptyMatchesDoNotRequestSongDetails() = runBlocking {
        for (body in listOf("""{"code":200,"data":null}""", """{"code":200,"data":{"result":[]}}""", """{"code":200,"data":{"result":null}}""")) {
            val requests = mutableListOf<Request>()
            assertTrue(repository(requests) { body }.recognize("fp").isEmpty())
            assertEquals(1, requests.size)
        }
    }

    @Test fun serviceFailureIsNotReportedAsNoMatch() = runBlocking {
        val requests = mutableListOf<Request>()
        try {
            repository(requests) { """{"code":500}""" }.recognize("fp")
            fail("Expected service failure")
        } catch (_: IllegalStateException) {
            assertEquals(1, requests.size)
        }
    }

    private fun repository(requests: MutableList<Request>, body: (Request) -> String): RecognitionRepository {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            requests.add(request)
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body(body(request).toResponseBody("application/json".toMediaType())).build()
        }.build()
        val api = Retrofit.Builder().baseUrl("https://example.invalid/").client(client)
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
            .build().create(NeteaseApi::class.java)
        return ApiRecognitionRepository(api)
    }
}
