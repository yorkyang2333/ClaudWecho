package com.yorkyang2333.claudwecho.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricParserTest {

    @Test
    fun testParseYrc_chineseLine() {
        val yrc = "[500,2000](500,500,0)我(1000,500,0)爱(1500,500,0)音(2000,500,0)乐(2500,0,0)"
        val lines = LyricParser.parseYrc(yrc)

        assertEquals(1, lines.size)
        val line = lines[0]
        assertEquals(500L, line.timeMs)
        assertEquals(2000L, line.durationMs)
        assertEquals("我爱音乐", line.text)
        assertEquals(4, line.words.size)

        assertEquals("我", line.words[0].text)
        assertEquals(500L, line.words[0].startTimeMs)
        assertEquals(500L, line.words[0].durationMs)
        assertEquals(1000L, line.words[0].endTimeMs)

        assertEquals("爱", line.words[1].text)
        assertEquals(1000L, line.words[1].startTimeMs)
        assertEquals(500L, line.words[1].durationMs)

        assertEquals("音", line.words[2].text)
        assertEquals(1500L, line.words[2].startTimeMs)
        assertEquals(500L, line.words[2].durationMs)

        assertEquals("乐", line.words[3].text)
        assertEquals(2000L, line.words[3].startTimeMs)
        assertEquals(500L, line.words[3].durationMs)
    }

    @Test
    fun testParseYrc_englishWordsWithSpaces() {
        val yrc = "[0,2000](0,500,0)Hello (500,500,0)World"
        val lines = LyricParser.parseYrc(yrc)

        assertEquals(1, lines.size)
        val line = lines[0]
        assertEquals("Hello World", line.text)
        assertEquals(2, line.words.size)
        assertEquals("Hello ", line.words[0].text)
        assertEquals(0L, line.words[0].startTimeMs)
        assertEquals("World", line.words[1].text)
        assertEquals(500L, line.words[1].startTimeMs)
    }

    @Test
    fun testParseYrc_skipsMetadataJsonAndTags() {
        val yrc = """
            [0,0]{"t":0,"c":[{"tx":"作词 : 方文山"},{"tx":"作曲 : 周杰伦"}]}
            [ti:晴天]
            [ar:周杰伦]
            [100,1000](100,500,0)A(600,500,0)B
        """.trimIndent()

        val lines = LyricParser.parseYrc(yrc)
        assertEquals(1, lines.size)
        assertEquals(100L, lines[0].timeMs)
        assertEquals(1000L, lines[0].durationMs)
        assertEquals("AB", lines[0].text)
        assertEquals(2, lines[0].words.size)
    }

    @Test
    fun testParseYrc_offsetSupport() {
        val yrc = """
            [offset:200]
            [100,1000](100,500,0)A(600,500,0)B
        """.trimIndent()

        val lines = LyricParser.parseYrc(yrc)
        assertEquals(1, lines.size)
        assertEquals(300L, lines[0].timeMs)
        assertEquals(300L, lines[0].words[0].startTimeMs)
        assertEquals(800L, lines[0].words[1].startTimeMs)
    }

    @Test
    fun testParseLrc_standardFormat() {
        val lrc = """
            [00:01.50]Hello
            [00:03.200]World
        """.trimIndent()

        val lines = LyricParser.parseLrc(lrc)
        assertEquals(2, lines.size)
        assertEquals(1500L, lines[0].timeMs)
        assertEquals("Hello", lines[0].text)
        assertEquals(3200L, lines[1].timeMs)
        assertEquals("World", lines[1].text)
    }

    @Test
    fun testAlignTranslations() {
        val yrc = """
            [1000,2000](1000,1000,0)Hello (2000,1000,0)World
            [3050,2000](3050,1000,0)Good (4050,1000,0)Morning
        """.trimIndent()

        val tLrc = """
            [00:01.00]你好 世界
            [00:03.00]早上好
        """.trimIndent()

        val lines = LyricParser.parseYrc(yrc)
        LyricParser.alignTranslations(lines, tLrc, toleranceMs = 1500L)

        assertEquals("你好 世界", lines[0].tText)
        assertEquals("早上好", lines[1].tText)
    }
}

