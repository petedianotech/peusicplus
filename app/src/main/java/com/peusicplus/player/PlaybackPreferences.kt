package com.peusicplus.player

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Local, private storage for playlists, favorites, playback history, saved queues, and preferences. */
object PlaybackPreferences {

    private const val PREFS = "peusicplus_data"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // Playlists

    fun getPlaylists(context: Context): List<String> {
        val raw = prefs(context).getString("playlists", "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { array.getString(it) }
        }.getOrDefault(emptyList())
    }

    fun createPlaylist(context: Context, name: String) {
        val playlists = getPlaylists(context).toMutableList()
        if (name.isNotBlank() && !playlists.contains(name)) {
            playlists.add(name)
            prefs(context).edit().putString("playlists", JSONArray(playlists).toString()).apply()
            savePlaylistTracks(context, name, emptyList())
        }
    }

    fun renamePlaylist(context: Context, oldName: String, newName: String) {
        val playlists = getPlaylists(context).toMutableList()
        val index = playlists.indexOf(oldName)
        if (index >= 0 && newName.isNotBlank()) {
            playlists[index] = newName
            prefs(context).edit().putString("playlists", JSONArray(playlists).toString()).apply()
            savePlaylistTracks(context, newName, getPlaylistTracks(context, oldName))
            prefs(context).edit().remove("playlist_$oldName").apply()
        }
    }

    fun deletePlaylist(context: Context, name: String) {
        val playlists = getPlaylists(context).toMutableList()
        playlists.remove(name)
        prefs(context).edit()
            .putString("playlists", JSONArray(playlists).toString())
            .remove("playlist_$name")
            .apply()
    }

    fun getPlaylistTracks(context: Context, name: String): List<Track> {
        val raw = prefs(context).getString("playlist_$name", "[]") ?: "[]"
        return decodeTracks(raw)
    }

    fun savePlaylistTracks(context: Context, name: String, tracks: List<Track>) {
        prefs(context).edit()
            .putString("playlist_$name", encodeTracks(tracks))
            .apply()
    }

    // Favorites

    fun getFavorites(context: Context): List<Track> = decodeTracks(
        prefs(context).getString("favorites", "[]") ?: "[]"
    )

    fun toggleFavorite(context: Context, track: Track): Boolean {
        val favorites = getFavorites(context).toMutableList()
        val existing = favorites.firstOrNull { it.id == track.id }
        return if (existing == null) {
            favorites.add(track)
            prefs(context).edit().putString("favorites", encodeTracks(favorites)).apply()
            true
        } else {
            favorites.removeAll { it.id == track.id }
            prefs(context).edit().putString("favorites", encodeTracks(favorites)).apply()
            false
        }
    }

    fun isFavorite(context: Context, track: Track): Boolean {
        return getFavorites(context).any { it.id == track.id }
    }

    // Recently played

    fun addRecentlyPlayed(context: Context, track: Track) {
        val recent = getRecentlyPlayed(context).toMutableList()
        recent.removeAll { it.id == track.id }
        recent.add(0, track)
        if (recent.size > 100) {
            recent.subList(100, recent.size).clear()
        }
        prefs(context).edit().putString("recent", encodeTracks(recent)).apply()
    }

    fun getRecentlyPlayed(context: Context): List<Track> = decodeTracks(
        prefs(context).getString("recent", "[]") ?: "[]"
    )

    // Saved queues

    fun getSavedQueues(context: Context): List<String> {
        val raw = prefs(context).getString("queues", "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { array.getString(it) }
        }.getOrDefault(emptyList())
    }

    fun saveQueue(context: Context, name: String, tracks: List<Track>) {
        if (name.isBlank()) return
        val queues = getSavedQueues(context).toMutableList()
        if (!queues.contains(name)) queues.add(name)
        prefs(context).edit()
            .putString("queues", JSONArray(queues).toString())
            .putString("queue_$name", encodeTracks(tracks))
            .apply()
    }

    fun getSavedQueue(context: Context, name: String): List<Track> = decodeTracks(
        prefs(context).getString("queue_$name", "[]") ?: "[]"
    )

    fun deleteSavedQueue(context: Context, name: String) {
        val queues = getSavedQueues(context).toMutableList()
        queues.remove(name)
        prefs(context).edit()
            .putString("queues", JSONArray(queues).toString())
            .remove("queue_$name")
            .apply()
    }

    // Sorting

    fun getSortMode(context: Context): String =
        prefs(context).getString("sort", "title") ?: "title"

    fun setSortMode(context: Context, mode: String) {
        prefs(context).edit().putString("sort", mode).apply()
    }

    // Serialization

    fun encodeTracks(tracks: List<Track>): String {
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
                    .put("folderPath", track.folderPath)
                    .put("uri", track.uri.toString())
            )
        }
        return array.toString()
    }

    private fun decodeTracks(raw: String): List<Track> {
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val item = array.getJSONObject(i)
                Track(
                    id = item.getLong("id"),
                    title = item.getString("title"),
                    artist = item.getString("artist"),
                    album = item.getString("album"),
                    durationMs = item.getLong("durationMs"),
                    albumId = item.getLong("albumId"),
                    folderPath = item.optString("folderPath"),
                    uri = android.net.Uri.parse(item.getString("uri"))
                )
            }
        }.getOrDefault(emptyList())
    }
}