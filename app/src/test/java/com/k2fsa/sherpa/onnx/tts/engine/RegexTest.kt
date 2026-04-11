package com.k2fsa.sherpa.onnx.tts.engine

import org.junit.Test
import org.junit.Assert.*

class RegexTest {
    fun processText(text: String): String {
        val regex = Regex("(?<=[\\u4e00-\\u9fa5\\u3040-\\u309f\\u30a0-\\u30ff])|(?=[\\u4e00-\\u9fa5\\u3040-\\u309f\\u30a0-\\u30ff])")
        return text.replace(regex, " ").replace(Regex("\\s+"), " ").trim()
    }

    @Test
    fun testRegex() {
        assertEquals("Hello こ ん に ち は World", processText("HelloこんにちはWorld"))
        assertEquals("こ れ は テ ス ト で す", processText("これはテストです"))
        assertEquals("Hello World", processText("Hello World"))
    }
}
