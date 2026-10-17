package com.yorkyang2333.claudwecho.data.recognition

import com.yorkyang2333.claudwecho.data.api.NeteaseApi
import com.yorkyang2333.claudwecho.data.api.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun interface RecognitionRepository {
    suspend fun recognize(fingerprint: String): List<Song>
}

class ApiRecognitionRepository(private val api: NeteaseApi) : RecognitionRepository {
    override suspend fun recognize(fingerprint: String): List<Song> = withContext(Dispatchers.IO) {
        val match = api.matchAudio(RECOGNITION_DURATION_SECONDS, fingerprint)
        check(match.code == 200) { "识别服务暂时不可用，请重试" }
        val ids = match.data?.result.orEmpty().mapNotNull { it.song?.id?.takeIf { id -> id > 0 } }.distinct()
        if (ids.isEmpty()) return@withContext emptyList()
        val details = api.getSongDetails(ids.joinToString(","))
        check(details.code == 200) { "无法加载识别结果，请重试" }
        val songs = details.songs.orEmpty().associateBy { it.id }
        val results = ids.mapNotNull { id ->
            songs[id]?.let { Song(id = it.id, name = it.name, ar = it.ar.orEmpty(), al = it.al, fee = it.fee ?: 0) }
        }
        check(results.isNotEmpty()) { "无法加载识别结果，请重试" }
        results
    }
}
