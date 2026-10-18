package com.peusicplus.player

import android.content.ComponentName
import android.content.Intent
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.PlaybackParameters
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken

class SettingsActivity : AppCompatActivity() {

    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupPlaybackControls()
        setupAudioEffects()
        setupSleepTimer()
        setupBookmarks()
        setupOutputProfile()

        findViewById<Button>(R.id.openQueueButton).setOnClickListener {
            startActivity(Intent(this, QueueActivity::class.java))
        }
    }

    private fun setupPlaybackControls() {
        val speedBar = findViewById<SeekBar>(R.id.speedBar)
        val pitchBar = findViewById<SeekBar>(R.id.pitchBar)
        val speedText = findViewById<TextView>(R.id.speedText)
        val pitchText = findViewById<TextView>(R.id.pitchText)

        speedBar.progress = ((AudioSettings.getPlaybackSpeed(this) - 0.5f) * 100).toInt()
        pitchBar.progress = ((AudioSettings.getPitch(this) - 0.5f) * 100).toInt()
        speedText.text = formatMultiplier(AudioSettings.getPlaybackSpeed(this))
        pitchText.text = formatMultiplier(AudioSettings.getPitch(this))

        speedBar.setOnSeekBarChangeListener(simpleListener { progress ->
            val speed = 0.5f + progress / 100f
            AudioSettings.setPlaybackSpeed(this, speed)
            speedText.text = formatMultiplier(speed)
            applyPlaybackParameters()
        })

        pitchBar.setOnSeekBarChangeListener(simpleListener { progress ->
            val pitch = 0.5f + progress / 100f
            AudioSettings.setPitch(this, pitch)
            pitchText.text = formatMultiplier(pitch)
            applyPlaybackParameters()
        })

        findViewById<Switch>(R.id.gaplessSwitch).isChecked =
            AudioSettings.isGaplessEnabled(this)
        findViewById<Switch>(R.id.gaplessSwitch).setOnCheckedChangeListener { _, checked ->
            AudioSettings.setGaplessEnabled(this, checked)
            notifyService()
        }

        val crossfadeBar = findViewById<SeekBar>(R.id.crossfadeBar)
        val crossfadeText = findViewById<TextView>(R.id.crossfadeText)
        val crossfade = AudioSettings.getCrossfadeSeconds(this)
        crossfadeBar.progress = crossfade
        crossfadeText.text = getString(R.string.crossfade_seconds, crossfade)

        crossfadeBar.setOnSeekBarChangeListener(simpleListener { progress ->
            AudioSettings.setCrossfadeSeconds(this, progress)
            crossfadeText.text = getString(R.string.crossfade_seconds, progress)
            notifyService()
        })

        val replayGainSwitch = findViewById<Switch>(R.id.replayGainSwitch)
        val replayGainBar = findViewById<SeekBar>(R.id.replayGainBar)
        val replayGainText = findViewById<TextView>(R.id.replayGainText)

        replayGainSwitch.isChecked = AudioSettings.isReplayGainEnabled(this)
        replayGainBar.progress = (AudioSettings.getReplayGainPreamp(this) + 15).toInt()
        replayGainText.text = formatDb(AudioSettings.getReplayGainPreamp(this))

        replayGainSwitch.setOnCheckedChangeListener { _, checked ->
            AudioSettings.setReplayGainEnabled(this, checked)
            notifyService()
        }

        replayGainBar.setOnSeekBarChangeListener(simpleListener { progress ->
            val db = progress - 15f
            AudioSettings.setReplayGainPreamp(this, db)
            replayGainText.text = formatDb(db)
            notifyService()
        })
    }

    private fun setupAudioEffects() {
        val eqSwitch = findViewById<Switch>(R.id.equalizerSwitch)
        val eqBandsContainer = findViewById<LinearLayout>(R.id.equalizerBands)
        val presetSpinner = findViewById<Spinner>(R.id.equalizerPresets)

        eqSwitch.isChecked = AudioSettings.isEnabled(this)
        eqSwitch.setOnCheckedChangeListener { _, checked ->
            AudioSettings.setEnabled(this, checked)
            notifyService()
        }

        val presets = listOf(
            "Custom", "Normal", "Classical", "Dance", "Flat", "Folk",
            "Heavy Metal", "Hip Hop", "Jazz", "Pop", "Rock"
        )

        presetSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            presets
        )

        presetSpinner.setSelection(AudioSettings.getEqualizerPreset(this) + 1)
        presetSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                AudioSettings.setEqualizerPreset(this@SettingsActivity, position - 1)
                notifyService()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }

        val savedLevels = AudioSettings.getEqualizerBandLevels(this)
        val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

        bandLabels.forEachIndexed { band, label ->
            val labelView = TextView(this).apply {
                text = label
                setTextColor(ContextCompat.getColor(this@SettingsActivity, R.color.text_secondary))
            }

            val bar = SeekBar(this).apply {
                max = 30
                progress = savedLevels.getOrNull(band)?.plus(15) ?: 15
                setOnSeekBarChangeListener(simpleListener { progress ->
                    val level = progress - 15
                    val levels = AudioSettings.getEqualizerBandLevels(this@SettingsActivity)
                        .ifEmpty { IntArray(bandLabels.size) { 0 } }
                        .copyOf()
                    levels[band] = level
                    AudioSettings.setEqualizerBandLevels(this@SettingsActivity, levels)
                    AudioSettings.setEqualizerPreset(this@SettingsActivity, -1)
                    notifyService()
                })
            }

            eqBandsContainer.addView(labelView)
            eqBandsContainer.addView(bar)
        }

        val bassBar = findViewById<SeekBar>(R.id.bassBar)
        val virtualizerBar = findViewById<SeekBar>(R.id.virtualizerBar)
        val reverbSpinner = findViewById<Spinner>(R.id.reverbSpinner)

        bassBar.progress = AudioSettings.getBassBoost(this)
        bassBar.setOnSeekBarChangeListener(simpleListener { progress ->
            AudioSettings.setBassBoost(this, progress)
            notifyService()
        })

        virtualizerBar.progress = AudioSettings.getVirtualizer(this)
        virtualizerBar.setOnSeekBarChangeListener(simpleListener { progress ->
            AudioSettings.setVirtualizer(this, progress)
            notifyService()
        })

        val reverbs = listOf(
            "None" to PresetReverb.PRESET_NONE,
            "Small Room" to PresetReverb.PRESET_SMALLROOM,
            "Medium Room" to PresetReverb.PRESET_MEDIUMROOM,
            "Large Room" to PresetReverb.PRESET_LARGEROOM,
            "Medium Hall" to PresetReverb.PRESET_MEDIUMHALL,
            "Large Hall" to PresetReverb.PRESET_LARGEHALL,
            "Plate" to PresetReverb.PRESET_PLATE
        )

        reverbSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            reverbs.map { it.first }
        )

        val currentReverb = AudioSettings.getReverbPreset(this)
        reverbSpinner.setSelection(reverbs.indexOfFirst { it.second == currentReverb }.coerceAtLeast(0))
        reverbSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                AudioSettings.setReverbPreset(this@SettingsActivity, reverbs[position].second)
                notifyService()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }
    }

    private fun setupSleepTimer() {
        val buttons = listOf(
            R.id.sleep15Button to 15,
            R.id.sleep30Button to 30,
            R.id.sleep60Button to 60,
            R.id.sleepCancel to 0
        )

        buttons.forEach { (id, minutes) ->
            findViewById<Button>(id).setOnClickListener {
                val action = if (minutes == 0) {
                    PlaybackService.ACTION_CANCEL_SLEEP_TIMER
                } else {
                    PlaybackService.ACTION_START_SLEEP_TIMER
                }

                val extras = android.os.Bundle.EMPTY
                if (minutes > 0) {
                    val args = android.os.Bundle()
                    args.putInt("minutes", minutes)
                    sendCustomCommand(action, args)
                } else {
                    sendCustomCommand(action, extras)
                }

                Toast.makeText(
                    this,
                    if (minutes == 0) R.string.sleep_timer_cancelled
                    else getString(R.string.sleep_timer_set, minutes),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendCustomCommand(action: String, args: android.os.Bundle) {
        mediaController?.sendCustomCommand(SessionCommand(action, args), android.os.Bundle.EMPTY)
    }

    private fun setupBookmarks() {
        findViewById<Button>(R.id.addBookmarkButton).setOnClickListener {
            val controller = mediaController ?: return@setOnClickListener
            val item = controller.currentMediaItem ?: return@setOnClickListener
            AudioSettings.saveBookmark(this, item.mediaId, controller.currentPosition)
            Toast.makeText(this, R.string.bookmark_saved, Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.showBookmarksButton).setOnClickListener {
            val bookmarks = AudioSettings.getBookmarks(this)
            if (bookmarks.isEmpty()) {
                Toast.makeText(this, R.string.no_bookmarks, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val entries = bookmarks.entries.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle(R.string.bookmarks)
                .setItems(entries.map { "${it.key} — ${formatTime(it.value)}" }.toTypedArray()) { _, which ->
                    mediaController?.seekTo(entries[which].value)
                }
                .setNeutralButton(R.string.delete) { _, _ ->
                    entries.forEach { AudioSettings.removeBookmark(this, it.key) }
                }
                .show()
        }
    }

    private fun setupOutputProfile() {
        val spinner = findViewById<Spinner>(R.id.outputProfileSpinner)
        val profiles = listOf("speaker", "headphones", "bluetooth", "usb")

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            profiles
        )

        spinner.setSelection(profiles.indexOf(AudioSettings.getOutputProfile(this)).coerceAtLeast(0))
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                AudioSettings.setOutputProfile(this@SettingsActivity, profiles[position])
                notifyService()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }
    }

    private fun applyPlaybackParameters() {
        mediaController?.playbackParameters = PlaybackParameters(
            AudioSettings.getPlaybackSpeed(this),
            AudioSettings.getPitch(this)
        )
    }

    private fun notifyService() {
        sendBroadcast(Intent(PlaybackService.ACTION_AUDIO_SETTINGS_CHANGED))
    }

    private fun simpleListener(onChanged: (Int) -> Unit): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) onChanged(progress)
            }

            override fun onStartTrackingTouch(bar: SeekBar?) = Unit
            override fun onStopTrackingTouch(bar: SeekBar?) = Unit
        }
    }

    private fun formatMultiplier(value: Float): String = "%.2fx".format(value)
    private fun formatDb(value: Float): String = "%.1f dB".format(value)

    private fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        return "%d:%02d".format(seconds / 60, seconds % 60)
    }

    override fun onStart() {
        super.onStart()
        connect()
    }

    private fun connect() {
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        future.addListener({
            mediaController = future.get()
            applyPlaybackParameters()
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onStop() {
        mediaController?.release()
        mediaController = null
        super.onStop()
    }
}