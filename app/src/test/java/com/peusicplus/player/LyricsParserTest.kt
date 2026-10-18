package com.peusicplus.player

import org.junit.Assert.assertEquals
import org.junit.Test

class LyricsParserTest {

    @Test
    fun `parses synchronized lyric timestamps`() {
        val lyrics = """
            [00:01.00] First line
            [00:03.50] Second line
            [00:10] Third line
        """.trimIndent()

        val parsed = LyricsParser.parse(lyrics)

        assertEquals(3, parsed.size)
        assertEquals(1000L, parsed[0].timeMs)
        assertEquals(3500L, parsed[1].timeMs)
        assertEquals(10000L, parsed[2].timeMs)
        assertEquals("Second line", parsed[1].text)
    }

    @Test
    fun `finds current lyric line for playback position`() {
        val parsed = LyricsParser.parse(
            """
            [00:01.00] First
            [00:05.00] Second
            [00:10.00] Third
            """.trimIndent()
        )

        assertEquals(-1, LyricsParser.currentLineIndex(parsed, 500))
        assertEquals(0, LyricsParser.currentLineIndex(parsed, 2000))
        assertEquals(1, LyricsParser.currentLineIndex(parsed, 6000))
        assertEquals(2, LyricsParser.currentLineIndex(parsed, 15000))
    }
}