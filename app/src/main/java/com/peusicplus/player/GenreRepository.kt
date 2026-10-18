package com.peusicplus.player

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

data class Genre(
    val id: Long,
    val name: String,
    val trackCount: Int
)

object GenreRepository {

    fun loadGenres(context: Context): List<Genre> {
        val genres = mutableListOf<Genre>()
        val collection = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Genres._ID,
            MediaStore.Audio.Genres.NAME
        )

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Audio.Genres.NAME} COLLATE NOCASE ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown genre"
                val count = countGenreTracks(context, id)
                if (count > 0) {
                    genres.add(Genre(id, name, count))
                }
            }
        }

        return genres
    }

    fun tracksForGenre(context: Context, genreId: Long): List<Track> {
        val genreUri = ContentUris.withAppendedId(
            MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
            genreId
        )
        val trackUri = Uri.withAppendedPath(genreUri, MediaStore.Audio.Genres.Members.CONTENT_DIRECTORY)

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

        context.contentResolver.query(
            trackUri,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
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
                tracks.add(
                    Track(
                        id = id,
                        title = cursor.getString(titleColumn) ?: "Unknown title",
                        artist = cursor.getString(artistColumn) ?: "Unknown artist",
                        album = cursor.getString(albumColumn) ?: "Unknown album",
                        durationMs = cursor.getLong(durationColumn),
                        albumId = cursor.getLong(albumIdColumn),
                        folderPath = (cursor.getString(dataColumn) ?: "").substringBeforeLast("/"),
                        uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                    )
                )
            }
        }

        return tracks
    }

    private fun countGenreTracks(context: Context, genreId: Long): Int {
        val genreUri = ContentUris.withAppendedId(
            MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
            genreId
        )
        val membersUri = Uri.withAppendedPath(genreUri, MediaStore.Audio.Genres.Members.CONTENT_DIRECTORY)

        return context.contentResolver.query(
            membersUri,
            arrayOf(MediaStore.Audio.Media._ID),
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            null
        )?.use { it.count } ?: 0
    }
}