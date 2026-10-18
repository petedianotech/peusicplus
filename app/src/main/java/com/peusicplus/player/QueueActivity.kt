package com.peusicplus.player

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.ComponentName

class QueueActivity : AppCompatActivity() {

    private lateinit var queueAdapter: QueueAdapter
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue)

        queueAdapter = QueueAdapter(
            onPlay = { position -> mediaController?.seekTo(position, 0) },
            onRemove = { position -> mediaController?.removeMediaItem(position) },
            onMoveUp = { position -> moveItem(position, position - 1) },
            onMoveDown = { position -> moveItem(position, position + 1) }
        )

        findViewById<RecyclerView>(R.id.queueList).apply {
            layoutManager = LinearLayoutManager(this@QueueActivity)
            adapter = queueAdapter
        }

        findViewById<ImageButton>(R.id.saveQueueButton).setOnClickListener { saveCurrentQueue() }
        findViewById<ImageButton>(R.id.loadQueueButton).setOnClickListener { showSavedQueues() }
        findViewById<ImageButton>(R.id.clearQueueButton).setOnClickListener {
            mediaController?.let { controller ->
                controller.clearMediaItems()
                refreshQueue()
            }
        }

        connect()
    }

    private fun connect() {
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        future.addListener({
            mediaController = future.get()
            refreshQueue()
        }, androidx.core.content.ContextCompat.getMainExecutor(this))
    }

    private fun moveItem(from: Int, to: Int) {
        val controller = mediaController ?: return
        if (from < 0 || to < 0 || from >= controller.mediaItemCount || to >= controller.mediaItemCount) return
        controller.moveMediaItem(from, to)
        refreshQueue()
    }

    private fun refreshQueue() {
        val controller = mediaController ?: return
        val items = (0 until controller.mediaItemCount).map { index ->
            val item = controller.getMediaItemAt(index)
            QueueItem(
                title = item.mediaMetadata.title?.toString() ?: "Unknown title",
                artist = item.mediaMetadata.artist?.toString() ?: "Unknown artist",
                isCurrent = index == controller.currentMediaItemIndex
            )
        }
        queueAdapter.submitQueue(items)
    }

    private fun saveCurrentQueue() {
        val controller = mediaController ?: return
        if (controller.mediaItemCount == 0) {
            Toast.makeText(this, R.string.queue_empty, Toast.LENGTH_SHORT).show()
            return
        }

        val name = "Queue ${System.currentTimeMillis()}"
        val tracks = (0 until controller.mediaItemCount).mapNotNull { index ->
            val item = controller.getMediaItemAt(index)
            val id = item.mediaId.toLongOrNull() ?: return@mapNotNull null
            allTracks().firstOrNull { it.id == id }
        }

        PlaybackPreferences.saveQueue(this, name, tracks)
        Toast.makeText(this, R.string.queue_saved, Toast.LENGTH_SHORT).show()
    }

    private fun allTracks(): List<Track> {
        return MusicLibraryRepository.loadTracks(this)
    }

    private fun showSavedQueues() {
        val queues = PlaybackPreferences.getSavedQueues(this)
        if (queues.isEmpty()) {
            Toast.makeText(this, R.string.no_saved_queues, Toast.LENGTH_SHORT).show()
            return
        }

        val names = queues.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.saved_queues)
            .setItems(names) { _, which ->
                val tracks = PlaybackPreferences.getSavedQueue(this, names[which])
                mediaController?.let { controller ->
                    controller.setMediaItems(tracks.map(PlaybackService::createMediaItem))
                    controller.prepare()
                    controller.play()
                    refreshQueue()
                }
            }
            .setNeutralButton(R.string.delete) { _, _ ->
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(R.string.delete_queue)
                    .setItems(names) { _, which ->
                        PlaybackPreferences.deleteSavedQueue(this, names[which])
                    }
                    .show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroy() {
        mediaController?.release()
        mediaController = null
        super.onDestroy()
    }
}

data class QueueItem(
    val title: String,
    val artist: String,
    val isCurrent: Boolean
)