package com.peusicplus.player

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object LibrarySorter {

    fun sortTracks(context: Context, tracks: List<Track>): List<Track> {
        return when (PlaybackPreferences.getSortMode(context)) {
            "artist" -> tracks.sortedBy { it.artist.lowercase() }
            "album" -> tracks.sortedWith(
                compareBy({ it.album.lowercase() }, { it.title.lowercase() })
            )
            "duration" -> tracks.sortedBy { it.durationMs }
            "date_added" -> tracks.sortedByDescending { it.id }
            "path" -> tracks.sortedBy { it.folderPath }
            else -> tracks.sortedBy { it.title.lowercase() }
        }
    }

    fun sortEntries(entries: List<LibraryEntry>): List<LibraryEntry> {
        return entries.sortedBy { it.title.lowercase() }
    }

    fun recentlyAdded(tracks: List<Track>): List<Track> {
        return tracks.sortedByDescending { it.id }.take(100)
    }

    fun exportM3U(context: Context, tracks: List<Track>): String {
        val builder = StringBuilder("#EXTM3U\n")
        tracks.forEach { track ->
            builder.append("#EXTINF:${track.durationMs / 1000},${track.artist} - ${track.title}\n")
            builder.append(track.uri.toString()).append("\n")
        }
        return builder.toString()
    }

    fun parseM3U(content: String, library: List<Track>): List<Track> {
        val lines = content.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }

        return lines.mapNotNull { line ->
            val uri = Uri.parse(line)
            val id = uri.lastPathSegment?.toLongOrNull()

            library.firstOrNull { it.id == id }
                ?: library.firstOrNull { it.uri.toString() == line }
                ?: library.firstOrNull { track ->
                    val fileName = line.substringAfterLast('/')
                    track.title.contains(fileName.substringBeforeLast('.'), ignoreCase = true)
                }
        }
    }
}