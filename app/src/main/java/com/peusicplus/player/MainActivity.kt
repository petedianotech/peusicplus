package com.peusicplus.player

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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

    private enum class LibraryMode {
        SONGS, ALBUMS, ARTISTS, FOLDERS, GENRES, PLAYLISTS, FAVORITES, RECENT, RECENT_ADDED
    }

    private lateinit var trackAdapter: TrackAdapter
    private lateinit var libraryAdapter: LibraryAdapter
    private lateinit var trackList: RecyclerView
    private lateinit var searchInput: EditText
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

    private val importPlaylistLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { importPlaylist(it) } }

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
        searchInput = findViewById(R.id.searchInput)

        trackAdapter = TrackAdapter(
            onTrackClick = { position -> playFromLibrary(position) },
            onTrackLongClick = { position -> showTrackActions(position) }
        )

        libraryAdapter = LibraryAdapter { entry -> openLibraryEntry(entry) }

        trackList.layoutManager = LinearLayoutManager(this)
        trackList.adapter = trackAdapter

        titleText.setOnClickListener { openNowPlaying() }

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

        findViewById<ImageButton>(R.id.queueButton).setOnClickListener {
            startActivity(Intent(this, QueueActivity::class.java))
        }

        findViewById<ImageButton>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<ImageButton>(R.id.sortButton).setOnClickListener { showSortMenu() }
        findViewById<ImageButton>(R.id.importButton).setOnClickListener {
            importPlaylistLauncher.launch(
                arrayOf("text/*", "audio/x-mpegurl", "application/vnd.apple.mpegurl")
            )
        }

        findViewById<ImageButton>(R.id.newPlaylistButton).setOnClickListener { createPlaylistDialog() }

        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun afterTextChanged(s: android.text.Editable?) {
                applySearchAndShowSongs()
            }
        })

        findViewById<ChipGroup>(R.id.libraryTabs).setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.albumsChip -> showAlbums()
                R.id.artistsChip -> showArtists()
                R.id.foldersChip -> showFolders()
                R.id.genresChip -> showGenres()
                R.id.playlistsChip -> showPlaylists()
                R.id.favoritesChip -> showFavorites()
                R.id.recentChip -> showRecentlyPlayed()
                R.id.recentAddedChip -> showRecentlyAdded()
                else -> showSongs()
            }
        }

        ensurePermissions()
        connectToPlaybackService()
        handler.post(progressRunnable)
    }

    private fun openNowPlaying() {
        startActivity(Intent(this, NowPlayingActivity::class.java))
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
        allTracks = LibrarySorter.sortTracks(this, MusicLibraryRepository.loadTracks(this))
        showSongs()

        if (allTracks.isEmpty()) {
            titleText.text = getString(R.string.no_music_found)
            artistText.text = getString(R.string.peusicplus)
        }
    }

    private fun applySearchAndShowSongs() {
        if (currentMode != LibraryMode.SONGS) return
        val query = searchInput.text.toString()
        visibleTracks = LibrarySorter.sortTracks(
            this,
            MusicLibraryRepository.searchTracks(allTracks, query)
        )
        trackAdapter.submitTracks(visibleTracks)

        if (visibleTracks.isEmpty() && query.isNotBlank()) {
            titleText.text = getString(R.string.no_results)
        }
    }

    private fun showSongs() {
        currentMode = LibraryMode.SONGS
        applySearchAndShowSongs()
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

    private fun showGenres() {
        currentMode = LibraryMode.GENRES
        trackList.adapter = libraryAdapter
        val genres = GenreRepository.loadGenres(this)
        libraryAdapter.submitEntries(
            genres.map { genre ->
                LibraryEntry(
                    id = genre.id,
                    title = genre.name,
                    subtitle = "${genre.trackCount} songs"
                )
            }
        )
        backButton.visibility = View.GONE
    }

    private fun showPlaylists() {
        currentMode = LibraryMode.PLAYLISTS
        trackList.adapter = libraryAdapter
        val playlists = PlaybackPreferences.getPlaylists(this)
        libraryAdapter.submitEntries(
            playlists.map { name ->
                val count = PlaybackPreferences.getPlaylistTracks(this, name).size
                LibraryEntry(id = name.hashCode().toLong(), title = name, subtitle = "$count tracks")
            }
        )
        backButton.visibility = View.GONE
    }

    private fun showFavorites() {
        currentMode = LibraryMode.FAVORITES
        visibleTracks = PlaybackPreferences.getFavorites(this)
        trackList.adapter = trackAdapter
        trackAdapter.submitTracks(visibleTracks)
        backButton.visibility = View.GONE
    }

    private fun showRecentlyPlayed() {
        currentMode = LibraryMode.RECENT
        visibleTracks = PlaybackPreferences.getRecentlyPlayed(this)
        trackList.adapter = trackAdapter
        trackAdapter.submitTracks(visibleTracks)
        backButton.visibility = View.GONE
    }

    private fun showRecentlyAdded() {
        currentMode = LibraryMode.RECENT_ADDED
        visibleTracks = LibrarySorter.recentlyAdded(allTracks)
        trackList.adapter = trackAdapter
        trackAdapter.submitTracks(visibleTracks)
        backButton.visibility = View.GONE
    }

    private fun openLibraryEntry(entry: LibraryEntry) {
        when (currentMode) {
            LibraryMode.ALBUMS -> {
                visibleTracks = MusicLibraryRepository.tracksForAlbum(allTracks, entry.id)
                trackList.adapter = trackAdapter
                trackAdapter.submitTracks(visibleTracks)
                backButton.visibility = View.VISIBLE
            }

            LibraryMode.ARTISTS -> {
                visibleTracks = MusicLibraryRepository.tracksForArtist(allTracks, entry.title)
                trackList.adapter = trackAdapter
                trackAdapter.submitTracks(visibleTracks)
                backButton.visibility = View.VISIBLE
            }

            LibraryMode.FOLDERS -> {
                visibleTracks = MusicLibraryRepository.tracksForFolder(allTracks, entry.path)
                trackList.adapter = trackAdapter
                trackAdapter.submitTracks(visibleTracks)
                backButton.visibility = View.VISIBLE
            }

            LibraryMode.GENRES -> {
                visibleTracks = GenreRepository.tracksForGenre(this, entry.id)
                trackList.adapter = trackAdapter
                trackAdapter.submitTracks(visibleTracks)
                backButton.visibility = View.VISIBLE
            }

            LibraryMode.PLAYLISTS -> {
                startActivity(
                    Intent(this, PlaylistActivity::class.java)
                        .putExtra(PlaylistActivity.EXTRA_PLAYLIST, entry.title)
                )
            }

            else -> showSongs()
        }
    }

    private fun showTrackActions(position: Int) {
        val track = visibleTracks.getOrNull(position) ?: return
        val isFavorite = PlaybackPreferences.isFavorite(this, track)

        val options = arrayOf(
            getString(R.string.play_next),
            getString(R.string.add_to_queue),
            getString(R.string.add_to_playlist),
            if (isFavorite) getString(R.string.unfavorite) else getString(R.string.favorite)
        )

        AlertDialog.Builder(this)
            .setTitle(track.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> playNext(track)
                    1 -> addToQueue(track)
                    2 -> addToPlaylistDialog(track)
                    3 -> {
                        val added = PlaybackPreferences.toggleFavorite(this, track)
                        Toast.makeText(
                            this,
                            if (added) R.string.favorite else R.string.unfavorite,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .show()
    }

    private fun playNext(track: Track) {
        val controller = mediaController ?: return
        controller.addMediaItem(
            controller.currentMediaItemIndex + 1,
            PlaybackService.createMediaItem(this, track)
        )
        Toast.makeText(this, R.string.play_next, Toast.LENGTH_SHORT).show()
    }

    private fun addToQueue(track: Track) {
        val controller = mediaController ?: return
        controller.addMediaItem(PlaybackService.createMediaItem(this, track))
        Toast.makeText(this, R.string.add_to_queue, Toast.LENGTH_SHORT).show()
    }

    private fun createPlaylistDialog() {
        val input = EditText(this)
        input.hint = getString(R.string.playlist_name)

        AlertDialog.Builder(this)
            .setTitle(R.string.new_playlist)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                PlaybackPreferences.createPlaylist(this, input.text.toString())
                showPlaylists()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun addToPlaylistDialog(track: Track) {
        val playlists = PlaybackPreferences.getPlaylists(this).toTypedArray()

        if (playlists.isEmpty()) {
            createPlaylistDialog()
            return
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.add_to_playlist)
            .setItems(playlists) { _, which ->
                val name = playlists[which]
                val tracks = PlaybackPreferences.getPlaylistTracks(this, name).toMutableList()
                if (tracks.none { it.id == track.id }) {
                    tracks.add(track)
                    PlaybackPreferences.savePlaylistTracks(this, name, tracks)
                    Toast.makeText(this, R.string.add_to_playlist, Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton(R.string.new_playlist) { _, _ -> createPlaylistDialog() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showSortMenu() {
        val options = arrayOf(
            getString(R.string.sort_by_title),
            getString(R.string.sort_by_artist),
            getString(R.string.sort_by_album),
            getString(R.string.sort_by_duration),
            getString(R.string.sort_by_date),
            getString(R.string.sort_by_path)
        )

        val modes = arrayOf("title", "artist", "album", "duration", "date_added", "path")

        AlertDialog.Builder(this)
            .setTitle(R.string.sort)
            .setItems(options) { _, which ->
                PlaybackPreferences.setSortMode(this, modes[which])
                allTracks = LibrarySorter.sortTracks(this, allTracks)
                showSongs()
            }
            .show()
    }

    private fun importPlaylist(uri: Uri) {
        val content = contentResolver.openInputStream(uri)?.bufferedReader()?.use {
            it.readText()
        } ?: return

        val imported = LibrarySorter.parseM3U(content, allTracks)
        if (imported.isEmpty()) {
            Toast.makeText(this, R.string.no_results, Toast.LENGTH_SHORT).show()
            return
        }

        val name = "Imported ${System.currentTimeMillis()}"
        PlaybackPreferences.createPlaylist(this, name)
        PlaybackPreferences.savePlaylistTracks(this, name, imported)
        showPlaylists()
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
            tracks.map { PlaybackService.createMediaItem(this, it) },
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
            visibleTracks.map { PlaybackService.createMediaItem(this, it) },
            position,
            0L
        )
        controller.prepare()
        controller.play()

        visibleTracks.getOrNull(position)?.let {
            PlaybackPreferences.addRecentlyPlayed(this, it)
        }
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