package com.yorkyang2333.claudwecho.ui.utils

object SongInfoTag

fun toOriginalImageUrl(url: String?): String {
    if (url.isNullOrBlank()) return ""
    if (!url.startsWith("http://") && !url.startsWith("https://")) return url
    var result = url.replace(Regex("([?&])param=[^&]*"), "$1")
    result = result.replace("?&", "?").replace(Regex("[?&]$"), "")
    return result
}

fun toLowResImageUrl(url: String?, size: Int = 200): String {
    if (url.isNullOrBlank()) return ""
    if (!url.startsWith("http://") && !url.startsWith("https://")) return url
    val baseUrl = toOriginalImageUrl(url)
    return if (baseUrl.contains("?")) {
        "$baseUrl&param=${size}y${size}"
    } else {
        "$baseUrl?param=${size}y${size}"
    }
}
