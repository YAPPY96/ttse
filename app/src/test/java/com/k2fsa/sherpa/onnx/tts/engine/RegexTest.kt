package com.k2fsa.sherpa.onnx.tts.engine

import org.junit.Test
import org.junit.Assert.*

class RegexTest {
    fun processText(text: String): String {
        val regex = Regex("(?<=[\\u4e00-\\u9fa5\\u3000-\\u30ff\\uff00-\\uffef])|(?=[\\u4e00-\\u9fa5\\u3000-\\u30ff\\uff00-\\uffef])")
        return text.replace(regex, " ").replace(Regex("\\s+"), " ").trim()
    }

    @Test
    fun testRegex() {
        assertEquals("同 じ エ ラ ー で す", processText("同じエラーです"))
        assertEquals("こ ん に ち は 。", processText("こんにちは。"))
        assertEquals("こ ん に ち は ！", processText("こんにちは！"))
        assertEquals("Hello", processText("Hello"))
    }
}
