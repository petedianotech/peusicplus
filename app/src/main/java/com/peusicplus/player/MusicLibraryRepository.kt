package com.peusicplus.player

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val albumId: Long,
    val folderPath: String,
    val uri: Uri
) {
    val albumArtUri: Uri
        get() = Uri.parse("content://media/external/audio/albumart/$albumId")
}

data class LibraryEntry(
    val id: Long,
    val title: String,
    val subtitle: String,
    val albumId: Long = -1,
    val path: String = ""
)

object MusicLibraryRepository {

    private val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    fun loadTracks(context: Context): List<Track> {
        val tracks = mutableListOf<Track>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val path = cursor.getString(dataColumn) ?: ""
                tracks.add(
                    Track(
                        id = id,
                        title = cursor.getString(titleColumn) ?: "Unknown title",
                        artist = cursor.getString(artistColumn) ?: "Unknown artist",
                        album = cursor.getString(albumColumn) ?: "Unknown album",
                        durationMs = cursor.getLong(durationColumn),
                        albumId = cursor.getLong(albumIdColumn),
                        folderPath = path.substringBeforeLast("/"),
                        uri = ContentUris.withAppendedId(collection, id)
                    )
                )
            }
        }

        return tracks
    }

    fun loadAlbums(tracks: List<Track>): List<LibraryEntry> {
        return tracks
            .groupBy { it.albumId }
            .map { (albumId, albumTracks) ->
                LibraryEntry(
                    id = albumId,
                    title = albumTracks.first().album,
                    subtitle = "${albumTracks.first().artist} • ${albumTracks.size} songs",
                    albumId = albumId
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    fun loadArtists(tracks: List<Track>): List<LibraryEntry> {
        return tracks
            .groupBy { it.artist }
            .map { (artist, artistTracks) ->
                LibraryEntry(
                    id = artist.hashCode().toLong(),
                    title = artist,
                    subtitle = "${artistTracks.size} songs",
                    albumId = artistTracks.first().albumId
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    fun loadFolders(tracks: List<Track>): List<LibraryEntry> {
        return tracks
            .groupBy { it.folderPath }
            .map { (folder, folderTracks) ->
                LibraryEntry(
                    id = folder.hashCode().toLong(),
                    title = folder.substringAfterLast("/"),
                    subtitle = folder,
                    path = folder
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    fun tracksForAlbum(tracks: List<Track>, albumId: Long): List<Track> {
        return tracks.filter { it.albumId == albumId }
    }

    fun tracksForArtist(tracks: List<Track>, artist: String): List<Track> {
        return tracks.filter { it.artist == artist }
    }

    fun tracksForFolder(tracks: List<Track>, folderPath: String): List<Track> {
        return tracks.filter { it.folderPath == folderPath }
    }

    fun searchTracks(tracks: List<Track>, query: String): List<Track> {
        if (query.isBlank()) return tracks
        val normalized = query.trim().lowercase()
        return tracks.filter {
            it.title.lowercase().contains(normalized) ||
                it.artist.lowercase().contains(normalized) ||
                it.album.lowercase().contains(normalized)
        }
    }
}