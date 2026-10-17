package com.yorkyang2333.claudwecho.ui.player

import androidx.compose.ui.geometry.Rect
import com.yorkyang2333.claudwecho.data.LyricWord
import org.junit.Assert.assertEquals
import org.junit.Test

class VerbatimWordBoundsTest {
    @Test
    fun singleLineWordsUseOneSweepRectanglePerWord() {
        val words = listOf(LyricWord("ab", 0, 100), LyricWord("c", 100, 100))
        val bounds = buildVerbatimWordBounds(words, 3) { index ->
            Rect(index * 10f, 0f, (index + 1) * 10f, 20f)
        }

        assertEquals(
            listOf(listOf(Rect(0f, 0f, 20f, 20f)), listOf(Rect(20f, 0f, 30f, 20f))),
            bounds
        )
    }

    @Test
    fun wrappedWordsKeepCharacterBoundsForContinuousSweepAcrossLines() {
        val boxes = listOf(Rect(10f, 0f, 20f, 20f), Rect(0f, 20f, 10f, 40f))
        val bounds = buildVerbatimWordBounds(listOf(LyricWord("ab", 0, 100)), 2, boxes::get)

        assertEquals(listOf(boxes), bounds)
    }

    @Test
    fun emptyAndTruncatedWordsNeverQueryOutsideTheText() {
        val offsets = mutableListOf<Int>()
        val words = listOf(
            LyricWord("", 0, 100),
            LyricWord("ab", 100, 100),
            LyricWord("c", 200, 100)
        )
        val bounds = buildVerbatimWordBounds(words, 1) { index ->
            offsets.add(index)
            Rect(0f, 0f, 10f, 20f)
        }

        assertEquals(listOf(0), offsets)
        assertEquals(listOf(emptyList(), listOf(Rect(0f, 0f, 10f, 20f)), emptyList()), bounds)
    }

    @Test
    fun emptyLayoutDoesNotRequestBoundingBoxes() {
        val bounds = buildVerbatimWordBounds(listOf(LyricWord("a", 0, 100)), 0) {
            error("An empty layout has no character bounds")
        }

        assertEquals(listOf(emptyList<Rect>()), bounds)
    }
}
