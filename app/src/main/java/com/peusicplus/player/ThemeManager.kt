package com.peusicplus.player

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    private const val PREFS = "peusicplus_appearance"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getTheme(context: Context): String =
        prefs(context).getString("theme", "dark") ?: "dark"

    fun setTheme(context: Context, theme: String) {
        prefs(context).edit().putString("theme", theme).apply()
        apply(context)
    }

    fun getAccent(context: Context): Int =
        prefs(context).getInt("accent", 0xFF7C5CFF.toInt())

    fun setAccent(context: Context, color: Int) {
        prefs(context).edit().putInt("accent", color).apply()
    }

    fun isVisualizerEnabled(context: Context): Boolean =
        prefs(context).getBoolean("visualizer", true)

    fun setVisualizerEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("visualizer", enabled).apply()
    }

    fun apply(context: Context) {
        when (getTheme(context)) {
            "light" -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )

            "amoled", "dark" -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        }
    }
}