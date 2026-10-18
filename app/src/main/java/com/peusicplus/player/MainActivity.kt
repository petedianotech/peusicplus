package com.peusicplus.player

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    private lateinit var trackAdapter: TrackAdapter
    private lateinit var titleText: TextView
    private lateinit var artistText: TextView
    private lateinit var currentTimeText: TextView
    private lateinit var durationText: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var playButton: ImageButton

    private var mediaController: MediaController? = null
    private var tracks: List<Track> = emptyList()
    private val handler = Handler(Looper.getMainLooper())

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            loadDeviceLibrary()
        } else {
            Toast.makeText(this, R.string.audio_permission_required, Toast.LENGTH_LONG).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val progressRunnable = object : Runnable {
        override fun run() {
            mediaController?.let { controller ->
                if (controller.isPlaying) {
                    seekBar.progress = controller.currentPosition.toInt()
                    currentTimeText.text = formatTime(controller.currentPosition)
                }
            }
            handler.postDelayed(this, 500)
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            playButton.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateNowPlaying()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                updateNowPlaying()
            }
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

        trackAdapter = TrackAdapter { position ->
            playFromLibrary(position)
        }

        findViewById<RecyclerView>(R.id.trackList).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = trackAdapter
        }

        playButton.setOnClickListener {
            val controller = mediaController ?: return@setOnClickListener
            if (controller.isPlaying) controller.pause() else controller.play()
        }

        findViewById<ImageButton>(R.id.previousButton).setOnClickListener {
            mediaController?.seekToPreviousMediaItem()
        }

        findViewById<ImageButton>(R.id.nextButton).setOnClickListener {
            mediaController?.seekToNextMediaItem()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaController?.seekTo(progress.toLong())
                    currentTimeText.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(bar: SeekBar?) = Unit
            override fun onStopTrackingTouch(bar: SeekBar?) = Unit
        })

        ensurePermissions()
        connectToPlaybackService()
        handler.post(progressRunnable)
    }

    private fun ensurePermissions() {
        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (
            ContextCompat.checkSelfPermission(this, audioPermission) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            loadDeviceLibrary()
        } else {
            audioPermissionLauncher.launch(audioPermission)
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun connectToPlaybackService() {
        val sessionToken = SessionToken(
            this,
            ComponentName(this, PlaybackService::class.java)
        )

        val future = MediaController.Builder(this, sessionToken).buildAsync()
        future.addListener({
            mediaController = future.get()
            mediaController?.addListener(playerListener)
            updateNowPlaying()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun loadDeviceLibrary() {
        tracks = MusicLibraryRepository.loadTracks(this)
        trackAdapter.submitTracks(tracks)

        if (tracks.isEmpty()) {
            titleText.text = getString(R.string.no_music_found)
            artistText.text = getString(R.string.peusicplus)
        }
    }

    private fun playFromLibrary(position: Int) {
        val controller = mediaController ?: return
        if (tracks.isEmpty()) return

        val mediaItems = tracks.map(PlaybackService::createMediaItem)
        controller.setMediaItems(mediaItems, position, 0L)
        controller.prepare()
        controller.play()
        trackAdapter.setSelectedPosition(position)
    }

    private fun updateNowPlaying() {
        val controller = mediaController ?: return
        val current = controller.currentMediaItem ?: return
        val metadata = current.mediaMetadata

        titleText.text = metadata.title ?: getString(R.string.select_track)
        artistText.text = buildString {
            append(metadata.artist ?: getString(R.string.unknown_artist))
            metadata.albumTitle?.let {
                append(" • ")
                append(it)
            }
        }

        durationText.text = formatTime(controller.duration)
        seekBar.max = controller.duration.toInt().coerceAtLeast(0)
        seekBar.progress = controller.currentPosition.toInt().coerceAtLeast(0)
        currentTimeText.text = formatTime(controller.currentPosition)

        playButton.setImageResource(
            if (controller.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )

        val position = tracks.indexOfFirst { it.id.toString() == current.mediaId }
        if (position >= 0) {
            trackAdapter.setSelectedPosition(position)
        }
    }

    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = (milliseconds / 1000).coerceAtLeast(0)
        return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }

    override fun onDestroy() {
        handler.removeCallbacks(progressRunnable)
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
        super.onDestroy()
    }
}