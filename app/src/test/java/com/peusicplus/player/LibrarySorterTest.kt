package com.peusicplus.player

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test

class LibrarySorterTest {

    private fun track(id: Long, title: String, artist: String, album: String) = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = 1000,
        albumId = id,
        folderPath = "/music",
        uri = Uri.parse("content://media/external/audio/media/$id")
    )

    @Test
    fun `sorts tracks by title`() {
        val tracks = listOf(
            track(1, "Zebra", "A", "Album"),
            track(2, "Apple", "B", "Album")
        )

        val sorted = tracks.sortedBy { it.title.lowercase() }

        assertEquals("Apple", sorted.first().title)
    }

    @Test
    fun `parses m3u and matches library tracks by media id`() {
        val library = listOf(track(42, "Song", "Artist", "Album"))
        val m3u = "#EXTM3U\ncontent://media/external/audio/media/42"

        val parsed = LibrarySorter.parseM3U(m3u, library)

        assertEquals(1, parsed.size)
        assertEquals(42L, parsed.first().id)
    }
}