package com.peusicplus.player

import android.content.Context

/** Persistent audio and playback settings. */
object AudioSettings {

    private const val PREFS = "peusicplus_audio"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean("eq_enabled", true)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("eq_enabled", enabled).apply()
    }

    fun getEqualizerBandLevels(context: Context): IntArray {
        val raw = prefs(context).getString("eq_bands", null) ?: return IntArray(0)
        return raw.split(",").mapNotNull { it.toIntOrNull() }.toIntArray()
    }

    fun setEqualizerBandLevels(context: Context, levels: IntArray) {
        prefs(context).edit().putString("eq_bands", levels.joinToString(",")).apply()
    }

    fun getEqualizerPreset(context: Context): Int =
        prefs(context).getInt("eq_preset", -1)

    fun setEqualizerPreset(context: Context, preset: Int) {
        prefs(context).edit().putInt("eq_preset", preset).apply()
    }

    fun getBassBoost(context: Context): Int =
        prefs(context).getInt("bass", 0)

    fun setBassBoost(context: Context, strength: Int) {
        prefs(context).edit().putInt("bass", strength).apply()
    }

    fun getVirtualizer(context: Context): Int =
        prefs(context).getInt("virtualizer", 0)

    fun setVirtualizer(context: Context, strength: Int) {
        prefs(context).edit().putInt("virtualizer", strength).apply()
    }

    fun getReverbPreset(context: Context): Short =
        prefs(context).getInt("reverb", 0).toShort()

    fun setReverbPreset(context: Context, preset: Short) {
        prefs(context).edit().putInt("reverb", preset.toInt()).apply()
    }

    fun getPlaybackSpeed(context: Context): Float =
        prefs(context).getFloat("speed", 1.0f)

    fun setPlaybackSpeed(context: Context, speed: Float) {
        prefs(context).edit().putFloat("speed", speed).apply()
    }

    fun getPitch(context: Context): Float =
        prefs(context).getFloat("pitch", 1.0f)

    fun setPitch(context: Context, pitch: Float) {
        prefs(context).edit().putFloat("pitch", pitch).apply()
    }

    fun isGaplessEnabled(context: Context): Boolean =
        prefs(context).getBoolean("gapless", true)

    fun setGaplessEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("gapless", enabled).apply()
    }

    fun getCrossfadeSeconds(context: Context): Int =
        prefs(context).getInt("crossfade", 0)

    fun setCrossfadeSeconds(context: Context, seconds: Int) {
        prefs(context).edit().putInt("crossfade", seconds).apply()
    }

    fun getReplayGainPreamp(context: Context): Float =
        prefs(context).getFloat("replaygain", 0f)

    fun setReplayGainPreamp(context: Context, db: Float) {
        prefs(context).edit().putFloat("replaygain", db).apply()
    }

    fun isReplayGainEnabled(context: Context): Boolean =
        prefs(context).getBoolean("replaygain_enabled", false)

    fun setReplayGainEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("replaygain_enabled", enabled).apply()
    }

    fun getBalance(context: Context): Float =
        prefs(context).getFloat("balance", 0f)

    fun setBalance(context: Context, balance: Float) {
        prefs(context).edit().putFloat("balance", balance).apply()
    }

    fun getOutputProfile(context: Context): String =
        prefs(context).getString("output_profile", "speaker") ?: "speaker"

    fun setOutputProfile(context: Context, profile: String) {
        prefs(context).edit().putString("output_profile", profile).apply()
    }

    fun getBookmarks(context: Context): MutableMap<String, Long> {
        val raw = prefs(context).getString("bookmarks", "{}") ?: return mutableMapOf()
        return runCatching {
            val json = org.json.JSONObject(raw)
            val result = mutableMapOf<String, Long>()
            json.keys().forEach { key -> result[key] = json.getLong(key) }
            result
        }.getOrDefault(mutableMapOf())
    }

    fun saveBookmark(context: Context, mediaId: String, positionMs: Long) {
        val bookmarks = getBookmarks(context)
        bookmarks[mediaId] = positionMs
        prefs(context).edit()
            .putString("bookmarks", org.json.JSONObject(bookmarks).toString())
            .apply()
    }

    fun removeBookmark(context: Context, mediaId: String) {
        val bookmarks = getBookmarks(context)
        bookmarks.remove(mediaId)
        prefs(context).edit()
            .putString("bookmarks", org.json.JSONObject(bookmarks).toString())
            .apply()
    }
}