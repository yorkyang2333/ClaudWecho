package com.yorkyang2333.claudwecho.utils

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import java.util.Locale

object PinyinUtil {
    private val format = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.UPPERCASE
        toneType = HanyuPinyinToneType.WITHOUT_TONE
    }

    /**
     * Get the first letter (A-Z) of the given string, converting Chinese characters to Pinyin.
     * If the string starts with an English letter, it returns the uppercase letter.
     * If it starts with a Chinese character, it returns the first letter of its Pinyin.
     * For other characters (e.g., Japanese, Korean, symbols, numbers), it returns "#".
     */
    fun getPinyinKey(text: String?): String {
        if (text.isNullOrBlank()) return "#"
        val firstChar = text.trim().first()

        // If it's already an English letter A-Z or a-z
        if (firstChar in 'A'..'Z' || firstChar in 'a'..'z') {
            return firstChar.uppercaseChar().toString()
        }

        // Try converting to Pinyin
        try {
            val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(firstChar, format)
            if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                val firstPinyinChar = pinyinArray[0].first()
                if (firstPinyinChar in 'A'..'Z') {
                    return firstPinyinChar.toString()
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        // Fallback for non-Chinese/English characters
        return "#"
    }
}
