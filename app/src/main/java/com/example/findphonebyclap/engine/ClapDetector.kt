package com.example.findphonebyclap.engine

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class ClapDetector(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val sampleRate = 22050
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = 1024
    private val energyListSize = 20

    private val energyList = ArrayList<Float>()
    private var preEnergy = 0f
    private var preFrequency = 0
    private var lastClapTime = 0L
    private var isClapping = false

    var onClapDetected: (() -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun start(threshold: Float = 3.0f, listener: () -> Unit) {
        stop()
        onClapDetected = listener

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val minBufSize = max(
            AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat),
            bufferSize * 2
        )

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        recordingJob = scope.launch {
            val audioBuffer = ShortArray(bufferSize)
            energyList.clear()

            while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val readCount = audioRecord?.read(audioBuffer, 0, bufferSize) ?: -1
                if (readCount > 0) {
                    processAudioBuffer(audioBuffer, threshold)
                }
            }
        }
    }

    private fun processAudioBuffer(buffer: ShortArray, threshold: Float) {
        val currentEnergy = calculateRMS(buffer)
        val currentFrequency = calculateZeroCrossingFrequency(buffer)

        val averageEnergy = if (energyList.size == energyListSize) {
            energyList.average().toFloat()
        } else {
            0f
        }

        val useEnergy = max(currentEnergy, preEnergy)
        val useFrequency = max(currentFrequency, preFrequency)

        val multiple = if (averageEnergy > 0.001f) {
            abs(useEnergy - averageEnergy) / averageEnergy
        } else {
            0f
        }

        // Clap criteria: Energy spike >= threshold, frequency in clap range (350Hz - 7000Hz)
        if (averageEnergy <= 0.001f || multiple <= threshold || useFrequency < 350 || useFrequency > 7000) {
            if (currentEnergy > 0.001f) {
                energyList.add(currentEnergy)
                if (energyList.size > energyListSize) {
                    energyList.removeAt(0)
                }
            }
            isClapping = false
        } else {
            if (!isClapping) {
                val currentTime = System.currentTimeMillis()
                val interval = currentTime - lastClapTime

                // Dual-clap detection: Two claps separated by 200ms to 1000ms
                if (interval in 200..1000) {
                    isClapping = true
                    lastClapTime = 0L
                    scope.launch(Dispatchers.Main) {
                        onClapDetected?.invoke()
                    }
                } else {
                    lastClapTime = currentTime
                }
            }
        }

        preEnergy = currentEnergy
        preFrequency = currentFrequency
    }

    private fun calculateRMS(buffer: ShortArray): Float {
        var sum = 0.0
        for (sample in buffer) {
            sum += (sample.toDouble() * sample.toDouble())
        }
        val mean = sum / buffer.size
        return sqrt(mean).toFloat()
    }

    private fun calculateZeroCrossingFrequency(buffer: ShortArray): Int {
        var zeroCrossings = 0
        for (i in 0 until buffer.size - 1) {
            if ((buffer[i] >= 0 && buffer[i + 1] < 0) || (buffer[i] < 0 && buffer[i + 1] >= 0)) {
                zeroCrossings++
            }
        }
        val durationSeconds = buffer.size.toFloat() / sampleRate
        return if (durationSeconds > 0) ((zeroCrossings / 2f) / durationSeconds).toInt() else 0
    }

    fun stop() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.let {
                if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioRecord = null
            energyList.clear()
            preEnergy = 0f
            preFrequency = 0
            isClapping = false
        }
    }
}
