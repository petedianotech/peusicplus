package com.peusicplus.player

import android.content.Context
import android.media.audiofx.Visualizer
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class VisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var visualizer: Visualizer? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ThemeManager.getAccent(context)
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private var waveform: ByteArray = ByteArray(0)

    fun start(audioSessionId: Int) {
        if (audioSessionId == 0) return
        if (!ThemeManager.isVisualizerEnabled(context)) return
        stop()

        runCatching {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            this@VisualizerView.waveform = waveform ?: ByteArray(0)
                            invalidate()
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) = Unit
                    },
                    Visualizer.getMaxCaptureRate() / 2
                )
                enabled = true
            }
        }
    }

    fun stop() {
        runCatching {
            visualizer?.enabled = false
            visualizer?.release()
        }
        visualizer = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (waveform.isEmpty()) return

        val middle = height / 2f
        val step = (waveform.size / 120f).coerceAtLeast(1f)

        for (i in 0 until 120) {
            val index = (i * step).toInt()
            if (index >= waveform.size) break

            val value = (waveform[index].toInt() - 128) / 128f
            val y = middle + value * (height / 2.4f)
            val x = width * i / 120f

            if (i == 0) {
                canvas.drawLine(x, y, x, y, paint)
            } else {
                val previousIndex = ((i - 1) * step).toInt()
                val previousValue = (waveform[previousIndex].toInt() - 128) / 128f
                val previousY = middle + previousValue * (height / 2.4f)
                canvas.drawLine(width * (i - 1) / 120f, previousY, x, y, paint)
            }
        }
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }
}