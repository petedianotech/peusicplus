package com.peusicplus.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private val handler = Handler(Looper.getMainLooper())
    private var sleepTimerEnd: Long = 0

    private val settingsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            applyAudioSettings()
        }
    }

    private val crossfadeRunnable = object : Runnable {
        override fun run() {
            val player = mediaSession?.player ?: return
            val crossfadeMs = AudioSettings.getCrossfadeSeconds(this@PlaybackService) * 1000L

            if (crossfadeMs > 0 && player.isPlaying) {
                val remaining = player.duration - player.currentPosition
                if (remaining in 1 until crossfadeMs) {
                    player.volume = (remaining.toFloat() / crossfadeMs).coerceIn(0f, 1f)
                } else if (remaining > crossfadeMs) {
                    player.volume = 1f
                }
            }

            handler.postDelayed(this, 250)
        }
    }

    private val sleepTimerRunnable = object : Runnable {
        override fun run() {
            val player = mediaSession?.player ?: return
            if (sleepTimerEnd > 0 && System.currentTimeMillis() >= sleepTimerEnd) {
                player.pause()
                sleepTimerEnd = 0
            }
            if (sleepTimerEnd > 0) {
                handler.postDelayed(this, 1000)
            }
        }
    }

    private val sessionCallback = object : MediaSession.Callback {
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_START_SLEEP_TIMER -> {
                    val minutes = customCommand.customExtras.getInt("minutes", 30)
                    startSleepTimer(minutes)
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                ACTION_CANCEL_SLEEP_TIMER -> {
                    cancelSleepTimer()
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
            }

            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED))
        }
    }

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()

        player.setAudioAttributes(audioAttributes, true)
        player.setHandleAudioBecomingNoisy(true)
        player.repeatMode = ExoPlayer.REPEAT_MODE_ALL
        player.setPauseAtEndOfMediaItems(false)

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(sessionCallback)
            .build()

        applyAudioSettings()

        registerReceiver(
            settingsReceiver,
            IntentFilter(ACTION_AUDIO_SETTINGS_CHANGED)
        )

        handler.post(crossfadeRunnable)
    }

    private fun applyAudioSettings() {
        val player = mediaSession?.player ?: return

        AudioEffectsManager.apply(this, player.audioSessionId)

        player.playbackParameters = PlaybackParameters(
            AudioSettings.getPlaybackSpeed(this),
            AudioSettings.getPitch(this)
        )

        val preamp = if (AudioSettings.isReplayGainEnabled(this)) {
            AudioSettings.getReplayGainPreamp(this)
        } else 0f

        player.volume = Math.pow(10.0, (preamp / 20.0)).toFloat().coerceIn(0f, 1f)
    }

    private fun startSleepTimer(minutes: Int) {
        sleepTimerEnd = System.currentTimeMillis() + minutes * 60_000L
        handler.post(sleepTimerRunnable)
    }

    private fun cancelSleepTimer() {
        sleepTimerEnd = 0
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        handler.removeCallbacks(crossfadeRunnable)
        handler.removeCallbacks(sleepTimerRunnable)
        runCatching { unregisterReceiver(settingsReceiver) }
        AudioEffectsManager.release()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_AUDIO_SETTINGS_CHANGED = "com.peusicplus.player.AUDIO_SETTINGS_CHANGED"
        const val ACTION_START_SLEEP_TIMER = "com.peusicplus.player.START_SLEEP_TIMER"
        const val ACTION_CANCEL_SLEEP_TIMER = "com.peusicplus.player.CANCEL_SLEEP_TIMER"

        fun createMediaItem(context: Context, track: Track): MediaItem {
            val overridden = MetadataOverrides.applyToTrack(context, track)
            val metadata = MediaMetadata.Builder()
                .setTitle(overridden.title)
                .setArtist(overridden.artist)
                .setAlbumTitle(overridden.album)
                .build()

            return MediaItem.Builder()
                .setMediaId(track.id.toString())
                .setUri(track.uri)
                .setMediaMetadata(metadata)
                .build()
        }
    }
}