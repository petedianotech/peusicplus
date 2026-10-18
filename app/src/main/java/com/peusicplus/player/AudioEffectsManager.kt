package com.peusicplus.player

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer

/**
 * Attaches Android audio effects to the active ExoPlayer audio session.
 * Settings are persisted locally and reloaded whenever the service restarts
 * or receives an audio-settings change.
 */
object AudioEffectsManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var reverb: PresetReverb? = null

    fun apply(context: Context, audioSessionId: Int) {
        release()

        runCatching {
            equalizer = Equalizer(0, audioSessionId).also { eq ->
                eq.enabled = AudioSettings.isEnabled(context)

                val preset = AudioSettings.getEqualizerPreset(context)
                if (preset >= 0 && preset < eq.numberOfPresets) {
                    eq.usePreset(preset.toShort())
                }

                val savedLevels = AudioSettings.getEqualizerBandLevels(context)
                if (savedLevels.size == eq.numberOfBands) {
                    savedLevels.forEachIndexed { index, level ->
                        eq.setBandLevel(index.toShort(), level.toShort())
                    }
                }
            }

            bassBoost = BassBoost(0, audioSessionId).also {
                it.enabled = true
                it.setStrength(AudioSettings.getBassBoost(context).toShort())
            }

            virtualizer = Virtualizer(0, audioSessionId).also {
                it.enabled = true
                it.setStrength(AudioSettings.getVirtualizer(context).toShort())
            }

            reverb = PresetReverb(0, audioSessionId).also {
                it.enabled = AudioSettings.getReverbPreset(context) != PresetReverb.PRESET_NONE
                it.preset = AudioSettings.getReverbPreset(context)
            }
        }
    }

    fun release() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        runCatching { reverb?.release() }
        equalizer = null
        bassBoost = null
        virtualizer = null
        reverb = null
    }
}