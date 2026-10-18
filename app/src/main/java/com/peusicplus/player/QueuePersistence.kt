package com.peusicplus.player

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists the playback queue and selected track so listening can resume after
 * the app or device restarts.
 */
object QueuePersistence {

    private const val PREFS_NAME = "peusicplus_playback"
    private const val KEY_QUEUE = "queue"
    private const val KEY_INDEX = "index"
    private const val KEY_POSITION = "position"

    fun save(context: Context, tracks: List<Track>, index: Int, positionMs: Long) {
        val array = JSONArray()
        tracks.forEach { track ->
            array.put(
                JSONObject()
                    .put("id", track.id)
                    .put("title", track.title)
                    .put("artist", track.artist)
                    .put("album", track.album)
                    .put("durationMs", track.durationMs)
                    .put("albumId", track.albumId)
                    .put("uri", track.uri.toString())
            )
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_QUEUE, array.toString())
            .putInt(KEY_INDEX, index)
            .putLong(KEY_POSITION, positionMs)
            .apply()
    }

    fun load(context: Context): SavedQueue? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_QUEUE, null) ?: return null

        return runCatching {
            val array = JSONArray(raw)
            val tracks = (0 until array.length()).map { i ->
                val item = array.getJSONObject(i)
                Track(
                    id = item.getLong("id"),
                    title = item.getString("title"),
                    artist = item.getString("artist"),
                    album = item.getString("album"),
                    durationMs = item.getLong("durationMs"),
                    albumId = item.getLong("albumId"),
                    uri = Uri.parse(item.getString("uri"))
                )
            }

            SavedQueue(
                tracks = tracks,
                index = prefs.getInt(KEY_INDEX, 0),
                positionMs = prefs.getLong(KEY_POSITION, 0L)
            )
        }.getOrNull()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}

data class SavedQueue(
    val tracks: List<Track>,
    val index: Int,
    val positionMs: Long
)