package com.yorkyang2333.claudwecho.ui.utils

object SongInfoTag

fun toLowResImageUrl(url: String?, size: Int = 200): String {
    if (url.isNullOrBlank()) return ""
    if (!url.startsWith("http://") && !url.startsWith("https://")) return url
    if (url.contains("param=")) {
        return url.replace(Regex("param=\\d+y\\d+"), "param=${size}y${size}")
    }
    return if (url.contains("?")) {
        "$url&param=${size}y${size}"
    } else {
        "$url?param=${size}y${size}"
    }
}

fun toOriginalImageUrl(url: String?): String {
    if (url.isNullOrBlank()) return ""
    var result = url.replace(Regex("[?&]param=\\d+y\\d+"), "")
    if (result.endsWith("?")) {
        result = result.substring(0, result.length - 1)
    }
    return result
}
