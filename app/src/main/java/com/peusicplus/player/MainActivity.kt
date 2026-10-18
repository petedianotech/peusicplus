package com.peusicplus.player

import android.os.Bundle
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

    private var currentIndex = 0
    private var isPrepared = false

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

        trackAdapter = TrackAdapter(TrackRepository.tracks) { position ->
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
            playTrack((currentIndex - 1 + TrackRepository.tracks.size) % TrackRepository.tracks.size)
        }

        findViewById<ImageButton>(R.id.nextButton).setOnClickListener {
            playTrack((currentIndex + 1) % TrackRepository.tracks.size)
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
            playTrack((currentIndex + 1) % TrackRepository.tracks.size)
        }

        playTrack(0)
        handler.post(updateRunnable)
    }

    private fun playTrack(position: Int) {
        currentIndex = position
        val track = TrackRepository.tracks[position]

        mediaPlayer.reset()
        val resourceId = resources.getIdentifier(track.resourceName, "raw", packageName)
        mediaPlayer = MediaPlayer.create(this, resourceId) ?: return

        isPrepared = true
        titleText.text = track.title
        artistText.text = track.artist
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