package com.yorkyang2333.claudwecho.data

data class LyricWord(
    val text: String,
    val startTimeMs: Long,
    val durationMs: Long
) {
    val endTimeMs: Long get() = startTimeMs + durationMs
}

data class LyricLine(
    val timeMs: Long,
    val text: String,
    var tText: String? = null,
    val durationMs: Long = 0L,
    val words: List<LyricWord> = emptyList()
) {
    val isVerbatim: Boolean get() = words.isNotEmpty()
}

object LyricParser {
    private val YRC_LINE_HEADER_REGEX = Regex("""^\[(\d+),(\d+)\]""")
    private val YRC_WORD_TAG_REGEX = Regex("""\((\d+),(\d+)(?:,\d+)?\)""")
    private val LRC_LINE_REGEX = Regex("""\[(\d+):(\d{2})(?:[.:](\d{1,3}))?\](.*)""")
    private val OFFSET_REGEX = Regex("""\[offset:\s*([+-]?\d+)\]""", RegexOption.IGNORE_CASE)

    /**
     * Parses YRC format lyrics into a list of [LyricLine] containing word-level timestamps.
     * Returns empty list if the input is not valid YRC.
     */
    fun parseYrc(yrcText: String): List<LyricLine> {
        val lines = yrcText.lines()
        var offset = 0L

        // Check for offset tag
        for (line in lines) {
            val offsetMatch = OFFSET_REGEX.find(line.trim())
            if (offsetMatch != null) {
                offset = offsetMatch.groupValues[1].toLongOrNull() ?: 0L
                break
            }
        }

        val result = mutableListOf<LyricLine>()

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) continue

            // Skip metadata JSON like [0,0]{"t":0,"c":[...]}
            if (trimmed.startsWith("[0,0]{") || (trimmed.startsWith("[") && trimmed.contains("\"tx\":"))) {
                continue
            }

            val headerMatch = YRC_LINE_HEADER_REGEX.find(trimmed) ?: continue
            val lineStart = headerMatch.groupValues[1].toLongOrNull() ?: continue
            val lineDur = headerMatch.groupValues[2].toLongOrNull() ?: 0L
            val rest = trimmed.substring(headerMatch.range.last + 1)

            val wordMatches = YRC_WORD_TAG_REGEX.findAll(rest).toList()
            if (wordMatches.isEmpty()) {
                val lineText = rest.trim()
                if (lineText.isNotEmpty()) {
                    result.add(
                        LyricLine(
                            timeMs = lineStart + offset,
                            text = lineText,
                            durationMs = lineDur,
                            words = emptyList()
                        )
                    )
                }
                continue
            }

            val words = mutableListOf<LyricWord>()
            for (i in wordMatches.indices) {
                val curr = wordMatches[i]
                val wStart = curr.groupValues[1].toLongOrNull() ?: continue
                val wDur = curr.groupValues[2].toLongOrNull() ?: 0L
                val textStart = curr.range.last + 1
                val textEnd = if (i + 1 < wordMatches.size) wordMatches[i + 1].range.first else rest.length
                val rawWord = rest.substring(textStart, textEnd)

                if (rawWord.isNotEmpty()) {
                    words.add(
                        LyricWord(
                            text = rawWord,
                            startTimeMs = wStart + offset,
                            durationMs = wDur.coerceAtLeast(10L)
                        )
                    )
                }
            }

            val fullText = words.joinToString("") { it.text }.trim()
            if (fullText.isNotEmpty() || words.isNotEmpty()) {
                val actualText = if (fullText.isNotEmpty()) fullText else words.joinToString("") { it.text }
                result.add(
                    LyricLine(
                        timeMs = lineStart + offset,
                        text = actualText,
                        durationMs = lineDur,
                        words = words
                    )
                )
            }
        }

        return result
    }

    /**
     * Parses standard LRC format lyrics into a list of [LyricLine].
     */
    fun parseLrc(lrcText: String): List<LyricLine> {
        val lines = lrcText.lines()
        var offset = 0L

        for (line in lines) {
            val offsetMatch = OFFSET_REGEX.find(line.trim())
            if (offsetMatch != null) {
                offset = offsetMatch.groupValues[1].toLongOrNull() ?: 0L
                break
            }
        }

        return lines.mapNotNull { line ->
            val matchResult = LRC_LINE_REGEX.find(line)
            if (matchResult != null) {
                val min = matchResult.groupValues[1].toLongOrNull() ?: 0L
                val sec = matchResult.groupValues[2].toLongOrNull() ?: 0L
                val msStr = matchResult.groupValues[3]
                val ms = when (msStr.length) {
                    1 -> (msStr.toLongOrNull() ?: 0L) * 100
                    2 -> (msStr.toLongOrNull() ?: 0L) * 10
                    3 -> msStr.toLongOrNull() ?: 0L
                    else -> 0L
                }
                val text = matchResult.groupValues[4].trim()
                val timeMs = min * 60000 + sec * 1000 + ms + offset
                if (text.isNotEmpty()) {
                    LyricLine(timeMs, text)
                } else null
            } else null
        }
    }

    /**
     * Parses either YRC (if available and valid) or falls back to LRC.
     */
    fun parseAuto(primaryText: String?, fallbackLrcText: String?): List<LyricLine> {
        if (!primaryText.isNullOrBlank()) {
            val yrcLines = parseYrc(primaryText)
            if (yrcLines.isNotEmpty()) {
                return yrcLines
            }
            val lrcLines = parseLrc(primaryText)
            if (lrcLines.isNotEmpty()) {
                return lrcLines
            }
        }
        if (!fallbackLrcText.isNullOrBlank()) {
            return parseLrc(fallbackLrcText)
        }
        return emptyList()
    }

    /**
     * Matches translation lines with primary lyric lines by closest timestamp within tolerance.
     */
    fun alignTranslations(
        lyrics: List<LyricLine>,
        translationText: String,
        toleranceMs: Long = 1500L
    ) {
        if (lyrics.isEmpty() || translationText.isBlank()) return

        // Translation can be either LRC format or YRC format line header
        val tLines = if (translationText.contains(YRC_LINE_HEADER_REGEX)) {
            val yrcParsed = parseYrc(translationText)
            if (yrcParsed.isNotEmpty()) yrcParsed else parseLrc(translationText)
        } else {
            parseLrc(translationText)
        }

        for (tLine in tLines) {
            val matchingLine = lyrics.minByOrNull { Math.abs(it.timeMs - tLine.timeMs) }
            if (matchingLine != null && Math.abs(matchingLine.timeMs - tLine.timeMs) <= toleranceMs) {
                matchingLine.tText = tLine.text
            }
        }
    }
}

