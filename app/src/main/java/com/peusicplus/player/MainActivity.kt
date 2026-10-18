package com.peusicplus.player

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
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
import com.google.android.material.chip.ChipGroup


class MainActivity : AppCompatActivity() {

    private enum class LibraryMode { SONGS, ALBUMS, ARTISTS, FOLDERS }

    private lateinit var trackAdapter: TrackAdapter
    private lateinit var libraryAdapter: LibraryAdapter
    private lateinit var trackList: RecyclerView

    private lateinit var titleText: TextView
    private lateinit var artistText: TextView
    private lateinit var currentTimeText: TextView
    private lateinit var durationText: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var playButton: ImageButton
    private lateinit var shuffleButton: ImageButton
    private lateinit var repeatButton: ImageButton
    private lateinit var backButton: ImageButton

    private var mediaController: MediaController? = null
    private var allTracks: List<Track> = emptyList()
    private var visibleTracks: List<Track> = emptyList()
    private var currentMode = LibraryMode.SONGS
    private var queueLoaded = false
    private val handler = Handler(Looper.getMainLooper())

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) loadDeviceLibrary() else {
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

                    val index = controller.currentMediaItemIndex
                    if (visibleTracks.isNotEmpty() && index in visibleTracks.indices) {
                        QueuePersistence.save(this@MainActivity, visibleTracks, index, controller.currentPosition)
                    }
                }
            }
            handler.postDelayed(this, 1000)
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
            if (playbackState == Player.STATE_READY) updateNowPlaying()
        }

        override fun onShuffleModeEnabledChanged(enabled: Boolean) {
            shuffleButton.alpha = if (enabled) 1f else 0.55f
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            repeatButton.alpha = if (repeatMode == Player.REPEAT_MODE_OFF) 0.55f else 1f
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
        shuffleButton = findViewById(R.id.shuffleButton)
        repeatButton = findViewById(R.id.repeatButton)
        backButton = findViewById(R.id.backButton)
        trackList = findViewById(R.id.trackList)

        trackAdapter = TrackAdapter { position -> playFromLibrary(position) }
        libraryAdapter = LibraryAdapter { entry -> openLibraryEntry(entry) }

        trackList.layoutManager = LinearLayoutManager(this)
        trackList.adapter = trackAdapter

        playButton.setOnClickListener {
            mediaController?.let { controller ->
                if (controller.isPlaying) controller.pause() else controller.play()
            }
        }

        findViewById<ImageButton>(R.id.previousButton).setOnClickListener {
            mediaController?.seekToPreviousMediaItem()
        }

        findViewById<ImageButton>(R.id.nextButton).setOnClickListener {
            mediaController?.seekToNextMediaItem()
        }

        shuffleButton.setOnClickListener {
            mediaController?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
        }

        repeatButton.setOnClickListener {
            mediaController?.let { controller ->
                controller.repeatMode = when (controller.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
            }
        }

        backButton.setOnClickListener { showSongs() }

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

        findViewById<ChipGroup>(R.id.libraryTabs).setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.albumsChip -> showAlbums()
                R.id.artistsChip -> showArtists()
                R.id.foldersChip -> showFolders()
                else -> showSongs()
            }
        }

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
            mediaController?.let { controller ->
                controller.addListener(playerListener)
                restoreSavedQueue(controller)
            }
            updateNowPlaying()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun loadDeviceLibrary() {
        allTracks = MusicLibraryRepository.loadTracks(this)
        showSongs()

        if (allTracks.isEmpty()) {
            titleText.text = getString(R.string.no_music_found)
            artistText.text = getString(R.string.peusicplus)
        }
    }

    private fun showSongs() {
        currentMode = LibraryMode.SONGS
        visibleTracks = allTracks
        trackList.adapter = trackAdapter
        trackAdapter.submitTracks(visibleTracks)
        backButton.visibility = View.GONE
    }

    private fun showAlbums() {
        currentMode = LibraryMode.ALBUMS
        trackList.adapter = libraryAdapter
        libraryAdapter.submitEntries(MusicLibraryRepository.loadAlbums(allTracks))
        backButton.visibility = View.GONE
    }

    private fun showArtists() {
        currentMode = LibraryMode.ARTISTS
        trackList.adapter = libraryAdapter
        libraryAdapter.submitEntries(MusicLibraryRepository.loadArtists(allTracks))
        backButton.visibility = View.GONE
    }

    private fun showFolders() {
        currentMode = LibraryMode.FOLDERS
        trackList.adapter = libraryAdapter
        libraryAdapter.submitEntries(MusicLibraryRepository.loadFolders(allTracks))
        backButton.visibility = View.GONE
    }

    private fun openLibraryEntry(entry: LibraryEntry) {
        visibleTracks = when (currentMode) {
            LibraryMode.ALBUMS -> MusicLibraryRepository.tracksForAlbum(allTracks, entry.id)
            LibraryMode.ARTISTS -> MusicLibraryRepository.tracksForArtist(allTracks, entry.title)
            LibraryMode.FOLDERS -> MusicLibraryRepository.tracksForFolder(allTracks, entry.path)
            else -> allTracks
        }

        trackList.adapter = trackAdapter
        trackAdapter.submitTracks(visibleTracks)
        backButton.visibility = View.VISIBLE
    }

    private fun restoreSavedQueue(controller: MediaController) {
        if (queueLoaded || controller.mediaItemCount > 0) return

        val saved = QueuePersistence.load(this) ?: return
        if (saved.tracks.isEmpty()) return

        queueLoaded = true
        allTracks = saved.tracks
        visibleTracks = saved.tracks
        trackAdapter.submitTracks(tracks)

        controller.setMediaItems(
            tracks.map(PlaybackService::createMediaItem),
            saved.index.coerceIn(0, tracks.lastIndex),
            saved.positionMs
        )
        controller.prepare()
    }

    private fun playFromLibrary(position: Int) {
        val controller = mediaController ?: return
        if (visibleTracks.isEmpty()) return

        queueLoaded = true
        controller.setMediaItems(
            visibleTracks.map(PlaybackService::createMediaItem),
            position,
            0L
        )
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
        shuffleButton.alpha = if (controller.shuffleModeEnabled) 1f else 0.55f
        repeatButton.alpha = if (controller.repeatMode == Player.REPEAT_MODE_OFF) 0.55f else 1f

        val position = visibleTracks.indexOfFirst { it.id.toString() == current.mediaId }
        if (position >= 0) trackAdapter.setSelectedPosition(position)
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