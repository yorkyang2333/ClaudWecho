package com.yorkyang2333.claudwecho

import com.yorkyang2333.claudwecho.data.api.DjProgramResponse
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    val json = Json { ignoreUnknownKeys = true; isLenient = true }
    val content = File("/tmp/dj.json").readText()
    try {
        val response = json.decodeFromString<DjProgramResponse>(content)
        println("Success: ${response.programs?.size} programs")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
