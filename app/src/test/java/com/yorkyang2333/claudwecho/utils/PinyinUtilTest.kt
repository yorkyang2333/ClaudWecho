package com.yorkyang2333.claudwecho.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PinyinUtilTest {

    @Test
    fun testPinyinKey_chineseCharacters() {
        assertEquals("Z", PinyinUtil.getPinyinKey("周杰伦"))
        assertEquals("C", PinyinUtil.getPinyinKey("陈奕迅"))
        assertEquals("W", PinyinUtil.getPinyinKey("王力宏"))
        assertEquals("L", PinyinUtil.getPinyinKey("林俊杰"))
    }

    @Test
    fun testPinyinKey_englishCharacters() {
        assertEquals("T", PinyinUtil.getPinyinKey("Taylor Swift"))
        assertEquals("A", PinyinUtil.getPinyinKey("adele"))
        assertEquals("B", PinyinUtil.getPinyinKey("  Beatles"))
    }

    @Test
    fun testPinyinKey_specialCharactersAndNumbers() {
        assertEquals("#", PinyinUtil.getPinyinKey("123"))
        assertEquals("#", PinyinUtil.getPinyinKey("!!!"))
        assertEquals("#", PinyinUtil.getPinyinKey(null))
        assertEquals("#", PinyinUtil.getPinyinKey(""))
        assertEquals("#", PinyinUtil.getPinyinKey("   "))
    }
}
