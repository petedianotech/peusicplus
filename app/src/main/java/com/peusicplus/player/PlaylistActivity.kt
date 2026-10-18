package com.peusicplus.player

import android.content.ComponentName
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PlaylistActivity : AppCompatActivity() {

    private lateinit var playlistName: String
    private lateinit var adapter: TrackAdapter
    private var tracks: List<Track> = emptyList()

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            contentResolver.openOutputStream(uri)?.use { output ->
                output.write(LibrarySorter.exportM3U(this, tracks).toByteArray())
            }
            Toast.makeText(this, R.string.playlist_exported, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        playlistName = intent.getStringExtra(EXTRA_PLAYLIST) ?: return
        title = playlistName

        adapter = TrackAdapter(
            onTrackClick = { position -> playPlaylist(position) },
            onTrackLongClick = { _ -> }
        )

        findViewById<RecyclerView>(R.id.playlistTracks).apply {
            layoutManager = LinearLayoutManager(this@PlaylistActivity)
            adapter = adapter
        }

        findViewById<Button>(R.id.playPlaylistButton).setOnClickListener {
            playPlaylist(0)
        }

        findViewById<Button>(R.id.exportPlaylistButton).setOnClickListener {
            if (tracks.isEmpty()) {
                Toast.makeText(this, R.string.playlist_empty, Toast.LENGTH_SHORT).show()
            } else {
                exportLauncher.launch("$playlistName.m3u")
            }
        }

        loadPlaylist()
    }

    private fun loadPlaylist() {
        tracks = PlaybackPreferences.getPlaylistTracks(this, playlistName)
        adapter.submitTracks(tracks)

        findViewById<TextView>(R.id.playlistCount).text =
            getString(R.string.playlist_track_count, tracks.size)
    }

    private fun playPlaylist(position: Int) {
        if (tracks.isEmpty()) return

        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        future.addListener({
            val controller = future.get()
            controller.setMediaItems(
                tracks.map { PlaybackService.createMediaItem(this, it) },
                position,
                0L
            )
            controller.prepare()
            controller.play()
            controller.release()
        }, ContextCompat.getMainExecutor(this))

        Toast.makeText(this, R.string.opening_playlist, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_PLAYLIST = "extra_playlist"
    }
}