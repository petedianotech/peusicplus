package com.peusicplus.player

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PlaylistActivity : AppCompatActivity() {

    private lateinit var playlistName: String
    private lateinit var adapter: TrackAdapter
    private var tracks: List<Track> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        playlistName = intent.getStringExtra(EXTRA_PLAYLIST) ?: return
        title = playlistName

        adapter = TrackAdapter { position -> playPlaylist(position) }
        findViewById<RecyclerView>(R.id.playlistTracks).apply {
            layoutManager = LinearLayoutManager(this@PlaylistActivity)
            adapter = adapter
        }

        findViewById<ImageButton>(R.id.playPlaylistButton).setOnClickListener {
            playPlaylist(0)
        }

        findViewById<ImageButton>(R.id.exportPlaylistButton).setOnClickListener {
            exportPlaylist()
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
        startActivity(
            android.content.Intent(this, QueueActivity::class.java)
        )

        // Playback is delegated through the media session by returning to Main.
        Toast.makeText(this, R.string.opening_playlist, Toast.LENGTH_SHORT).show()
    }

    private fun exportPlaylist() {
        if (tracks.isEmpty()) {
            Toast.makeText(this, R.string.playlist_empty, Toast.LENGTH_SHORT).show()
            return
        }

        exportLauncher.launch("$playlistName.m3u")
    }

    private val exportLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            contentResolver.openOutputStream(uri)?.use { output ->
                output.write(LibrarySorter.exportM3U(this, tracks).toByteArray())
            }
            Toast.makeText(this, R.string.playlist_exported, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_PLAYLIST = "extra_playlist"
    }
}