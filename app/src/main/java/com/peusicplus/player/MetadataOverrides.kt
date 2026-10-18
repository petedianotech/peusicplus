package com.peusicplus.player

import android.content.Context
import android.net.Uri
import org.json.JSONObject

/**
 * Stores user-facing metadata and artwork overrides locally.
 * Original audio files are never modified; this keeps the app safe and avoids
 * requiring write access to user music.
 */
object MetadataOverrides {

    private const val PREFS = "peusicplus_metadata"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    data class Override(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val lyrics: String? = null,
        val artworkUri: String? = null
    )

    fun get(context: Context, mediaId: String): Override {
        val raw = prefs(context).getString(mediaId, null) ?: return Override()
        return runCatching {
            val json = JSONObject(raw)
            Override(
                title = json.optString("title", "").ifEmpty { null },
                artist = json.optString("artist", "").ifEmpty { null },
                album = json.optString("album", "").ifEmpty { null },
                lyrics = json.optString("lyrics", "").ifEmpty { null },
                artworkUri = json.optString("artworkUri", "").ifEmpty { null }
            )
        }.getOrDefault(Override())
    }

    fun save(context: Context, mediaId: String, override: Override) {
        val json = JSONObject()
            .put("title", override.title ?: "")
            .put("artist", override.artist ?: "")
            .put("album", override.album ?: "")
            .put("lyrics", override.lyrics ?: "")
            .put("artworkUri", override.artworkUri ?: "")

        prefs(context).edit().putString(mediaId, json.toString()).apply()
    }

    fun applyToTrack(context: Context, track: Track): Track {
        val override = get(context, track.id.toString())
        return track.copy(
            title = override.title ?: track.title,
            artist = override.artist ?: track.artist,
            album = override.album ?: track.album
        )
    }

    fun artworkUri(context: Context, track: Track): Uri? {
        return get(context, track.id.toString()).artworkUri?.let(Uri::parse)
    }
}