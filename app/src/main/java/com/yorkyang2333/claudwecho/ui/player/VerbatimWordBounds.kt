package com.yorkyang2333.claudwecho.ui.player

import androidx.compose.ui.geometry.Rect
import com.yorkyang2333.claudwecho.data.LyricWord

// Cache text geometry per layout; playback ticks only change the sweep fraction.
internal fun buildVerbatimWordBounds(
    words: List<LyricWord>,
    textLength: Int,
    boundingBox: (Int) -> Rect
): List<List<Rect>> {
    var offset = 0
    return words.map { word ->
        val start = offset.coerceAtMost(textLength)
        offset += word.text.length
        val end = offset.coerceAtMost(textLength)
        if (start >= end) {
            emptyList()
        } else {
            val boxes = (start until end).map(boundingBox)
            val first = boxes.first()
            val last = boxes.last()
            if (first.top == last.top) {
                listOf(Rect(first.left, first.top, last.right, first.bottom))
            } else {
                boxes
            }
        }
    }
}
