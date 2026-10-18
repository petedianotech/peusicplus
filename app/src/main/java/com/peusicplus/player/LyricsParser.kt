package com.peusicplus.player

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

object LyricsParser {

    data class LyricLine(
        val timeMs: Long,
        val text: String
    )

    fun parse(content: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()

        content.lines().forEach { raw ->
            val match = Regex("\\[(\\d+):(\\d+)(?:[.:](\\d+))?]\\s*(.*)").find(raw) ?: return@forEach
            val minutes = match.groupValues[1].toLongOrNull() ?: return@forEach
            val seconds = match.groupValues[2].toLongOrNull() ?: return@forEach
            val fraction = match.groupValues[3].ifEmpty { "0" }
            val fractionMs = when (fraction.length) {
                1 -> fraction.toLong() * 100
                2 -> fraction.toLong() * 10
                else -> fraction.take(3).toLong()
            }

            lines.add(
                LyricLine(
                    timeMs = minutes * 60_000 + seconds * 1000 + fractionMs,
                    text = match.groupValues[4]
                )
            )
        }

        return lines.sortedBy { it.timeMs }
    }

    fun currentLineIndex(lines: List<LyricLine>, positionMs: Long): Int {
        var index = -1
        lines.forEachIndexed { i, line ->
            if (line.timeMs <= positionMs) index = i else return index
        }
        return index
    }

    fun embeddedLyrics(context: Context, uri: Uri): String? {
        val retriever = MediaMetadataRetriever()
        return runCatching {
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LYRICS)
        }.getOrNull().also {
            runCatching { retriever.release() }
        }
    }
}