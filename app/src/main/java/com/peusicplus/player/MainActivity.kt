package com.peusicplus.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var trackAdapter: TrackAdapter
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var titleText: TextView
    private lateinit var artistText: TextView
    private lateinit var currentTimeText: TextView
    private lateinit var durationText: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var playButton: ImageButton

    private var tracks: List<Track> = emptyList()
    private var currentIndex = 0
    private var isPrepared = false

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            loadDeviceLibrary()
        } else {
            Toast.makeText(this, R.string.audio_permission_required, Toast.LENGTH_LONG).show()
        }
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isPrepared && mediaPlayer.isPlaying) {
                seekBar.progress = mediaPlayer.currentPosition
                currentTimeText.text = formatTime(mediaPlayer.currentPosition)
            }
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        titleText = findViewById(R.id.trackTitle)
        artistText = findViewById(R.id.trackArtist)
        currentTimeText = findViewById(R.id.currentTime)
        durationText = findViewById(R.id.duration)
        seekBar = findViewById(R.id.seekBar)
        playButton = findViewById(R.id.playButton)

        mediaPlayer = MediaPlayer()

        trackAdapter = TrackAdapter { position ->
            playTrack(position)
        }

        findViewById<RecyclerView>(R.id.trackList).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = trackAdapter
        }

        playButton.setOnClickListener {
            if (!isPrepared) {
                playTrack(currentIndex)
            } else if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playButton.setImageResource(R.drawable.ic_play)
            } else {
                mediaPlayer.start()
                playButton.setImageResource(R.drawable.ic_pause)
            }
        }

        findViewById<ImageButton>(R.id.previousButton).setOnClickListener {
            if (tracks.isNotEmpty()) {
                playTrack((currentIndex - 1 + tracks.size) % tracks.size)
            }
        }

        findViewById<ImageButton>(R.id.nextButton).setOnClickListener {
            if (tracks.isNotEmpty()) {
                playTrack((currentIndex + 1) % tracks.size)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && isPrepared) {
                    mediaPlayer.seekTo(progress)
                    currentTimeText.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(bar: SeekBar?) = Unit
            override fun onStopTrackingTouch(bar: SeekBar?) = Unit
        })

        mediaPlayer.setOnCompletionListener {
            if (tracks.isNotEmpty()) {
                playTrack((currentIndex + 1) % tracks.size)
            }
        }

        ensureAudioPermission()
        handler.post(updateRunnable)
    }

    private fun ensureAudioPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadDeviceLibrary()
        } else {
            audioPermissionLauncher.launch(permission)
        }
    }

    private fun loadDeviceLibrary() {
        tracks = MusicLibraryRepository.loadTracks(this)
        trackAdapter.submitTracks(tracks)

        if (tracks.isEmpty()) {
            titleText.text = getString(R.string.no_music_found)
            artistText.text = getString(R.string.peusicplus)
        } else {
            playTrack(0)
        }
    }

    private fun playTrack(position: Int) {
        if (tracks.isEmpty()) return

        currentIndex = position.coerceIn(0, tracks.lastIndex)
        val track = tracks[currentIndex]

        if (isPrepared) {
            mediaPlayer.release()
        }

        mediaPlayer = MediaPlayer.create(this, track.uri) ?: return
        isPrepared = true

        titleText.text = track.title
        artistText.text = "${track.artist} • ${track.album}"
        durationText.text = formatTime(mediaPlayer.duration)
        seekBar.max = mediaPlayer.duration
        seekBar.progress = 0
        currentTimeText.text = "0:00"

        mediaPlayer.start()
        playButton.setImageResource(R.drawable.ic_pause)
        trackAdapter.setSelectedPosition(position)
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateRunnable)
        if (isPrepared) {
            mediaPlayer.release()
        }
        super.onDestroy()
    }
}