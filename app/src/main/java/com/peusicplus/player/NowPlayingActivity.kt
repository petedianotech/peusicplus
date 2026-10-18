package com.peusicplus.player

import android.content.ComponentName
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.load
import com.google.android.material.appbar.MaterialToolbar

class NowPlayingActivity : AppCompatActivity() {

    private var mediaController: MediaController? = null
    private var currentTrack: Track? = null
    private var lyricLines: List<LyricsParser.LyricLine> = emptyList()

    private lateinit var artwork: ImageView
    private lateinit var titleText: TextView
    private lateinit var artistText: TextView
    private lateinit var lyricsText: TextView
    private lateinit var codecText: TextView

    private val artworkPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val track = currentTrack ?: return@registerForActivityResult
        if (uri != null) {
            val existing = MetadataOverrides.get(this, track.id.toString())
            MetadataOverrides.save(
                this,
                track.id.toString(),
                existing.copy(artworkUri = uri.toString())
            )
            loadArtwork(track)
        }
    }

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    private val lyricRunnable = object : Runnable {
        override fun run() {
            val controller = mediaController ?: return
            if (lyricLines.isNotEmpty()) {
                val index = LyricsParser.currentLineIndex(lyricLines, controller.currentPosition)
                lyricsText.text = if (index >= 0) lyricLines[index].text else ""
            }
            handler.postDelayed(this, 250)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)

        artwork = findViewById(R.id.nowPlayingArtwork)
        titleText = findViewById(R.id.nowPlayingTitle)
        artistText = findViewById(R.id.nowPlayingArtist)
        lyricsText = findViewById(R.id.lyricsText)
        codecText = findViewById(R.id.codecText)

        val gestureDetector = GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (kotlin.math.abs(velocityX) > kotlin.math.abs(velocityY)) {
                        if (velocityX < 0) {
                            mediaController?.seekToNextMediaItem()
                        } else {
                            mediaController?.seekToPreviousMediaItem()
                        }
                        return true
                    }
                    return false
                }
            }
        )

        artwork.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        findViewById<Button>(R.id.editTagsButton).setOnClickListener { showTagEditor() }
        findViewById<Button>(R.id.editLyricsButton).setOnClickListener { showLyricsEditor() }
        findViewById<Button>(R.id.changeArtworkButton).setOnClickListener {
            artworkPicker.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }

        findViewById<Button>(R.id.codecInfoButton).setOnClickListener { showCodecInfo() }

        connect()
        handler.post(lyricRunnable)
    }

    private fun connect() {
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        future.addListener({
            mediaController = future.get()
            mediaController?.addListener(object : androidx.media3.common.Player.Listener {
                override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                    loadCurrentTrack()
                }
            })
            loadCurrentTrack()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun loadCurrentTrack() {
        val controller = mediaController ?: return
        val mediaId = controller.currentMediaItem?.mediaId ?: return
        val track = MusicLibraryRepository.loadTracks(this)
            .firstOrNull { it.id.toString() == mediaId } ?: return

        currentTrack = MetadataOverrides.applyToTrack(this, track)
        titleText.text = currentTrack?.title
        artistText.text = "${currentTrack?.artist} • ${currentTrack?.album}"

        loadArtwork(track)
        loadLyrics(track)
    }

    private fun loadArtwork(track: Track) {
        val overrideArt = MetadataOverrides.artworkUri(this, track)
        artwork.load(overrideArt ?: track.albumArtUri) {
            crossfade(true)
            error(R.drawable.ic_music_note)
        }
    }

    private fun loadLyrics(track: Track) {
        val override = MetadataOverrides.get(this, track.id.toString())
        val embedded = LyricsParser.embeddedLyrics(this, track.uri)
        val lyrics = override.lyrics ?: embedded ?: ""

        lyricLines = LyricsParser.parse(lyrics)
        lyricsText.text = if (lyricLines.isEmpty()) {
            lyrics.ifEmpty { getString(R.string.no_lyrics) }
        } else {
            lyricsText.text
        }
    }

    private fun showTagEditor() {
        val track = currentTrack ?: return
        val override = MetadataOverrides.get(this, track.id.toString())

        val layout = layoutInflater.inflate(R.layout.dialog_tag_editor, null)
        val titleInput = layout.findViewById<android.widget.EditText>(R.id.titleInput)
        val artistInput = layout.findViewById<android.widget.EditText>(R.id.artistInput)
        val albumInput = layout.findViewById<android.widget.EditText>(R.id.albumInput)

        titleInput.setText(override.title ?: track.title)
        artistInput.setText(override.artist ?: track.artist)
        albumInput.setText(override.album ?: track.album)

        AlertDialog.Builder(this)
            .setTitle(R.string.tag_editor)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val updated = MetadataOverrides.Override(
                    title = titleInput.text.toString(),
                    artist = artistInput.text.toString(),
                    album = albumInput.text.toString(),
                    lyrics = override.lyrics,
                    artworkUri = override.artworkUri
                )

                MetadataOverrides.save(this, track.id.toString(), updated)
                loadCurrentTrack()
            }
            .setNeutralButton(R.string.apply_to_album) { _, _ ->
                applyToAlbum(track, artistInput.text.toString(), albumInput.text.toString())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun applyToAlbum(track: Track, artist: String, album: String) {
        MusicLibraryRepository.tracksForAlbum(MusicLibraryRepository.loadTracks(this), track.albumId)
            .forEach { albumTrack ->
                val existing = MetadataOverrides.get(this, albumTrack.id.toString())
                MetadataOverrides.save(
                    this,
                    albumTrack.id.toString(),
                    existing.copy(artist = artist, album = album)
                )
            }

        Toast.makeText(this, R.string.batch_metadata_applied, Toast.LENGTH_SHORT).show()
        loadCurrentTrack()
    }

    private fun showLyricsEditor() {
        val track = currentTrack ?: return
        val override = MetadataOverrides.get(this, track.id.toString())

        val input = android.widget.EditText(this)
        input.setText(override.lyrics ?: LyricsParser.embeddedLyrics(this, track.uri) ?: "")
        input.minLines = 8
        input.gravity = android.view.Gravity.TOP

        AlertDialog.Builder(this)
            .setTitle(R.string.lyrics_editor)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val updated = override.copy(lyrics = input.text.toString())
                MetadataOverrides.save(this, track.id.toString(), updated)
                loadLyrics(track)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showCodecInfo() {
        val track = currentTrack ?: return
        val retriever = MediaMetadataRetriever()

        runCatching {
            retriever.setDataSource(this, track.uri)
            val info = buildString {
                append("MIME: ")
                appendLine(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "Unknown")
                append("Bitrate: ")
                appendLine(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE) ?: "Unknown")
                append("Duration: ")
                appendLine(formatTime(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0))
                append("Title: ")
                appendLine(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Unknown")
            }

            AlertDialog.Builder(this)
                .setTitle(R.string.audio_format)
                .setMessage(info)
                .show()
        }.onFailure {
            Toast.makeText(this, R.string.codec_info_unavailable, Toast.LENGTH_SHORT).show()
        }

        runCatching { retriever.release() }
    }

    private fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        return "%d:%02d".format(seconds / 60, seconds % 60)
    }

    override fun onDestroy() {
        handler.removeCallbacks(lyricRunnable)
        mediaController?.release()
        mediaController = null
        super.onDestroy()
    }
}