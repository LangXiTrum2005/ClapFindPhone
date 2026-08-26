package com.example.findphonebyclap.engine

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import com.example.findphonebyclap.data.model.FlashlightMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FlashlightController(private val context: Context) {

    private val cameraManager: CameraManager? by lazy {
        try {
            context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        } catch (e: Exception) {
            null
        }
    }

    private var cameraId: String? = null
    private var strobeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        findCameraId()
    }

    private fun findCameraId() {
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val characteristics = cameraManager?.getCameraCharacteristics(id)
                val hasFlash = characteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics?.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun start(mode: FlashlightMode) {
        stop()
        if (mode == FlashlightMode.OFF || cameraId == null) return

        strobeJob = scope.launch {
            try {
                when (mode) {
                    FlashlightMode.DEFAULT, FlashlightMode.FAST -> {
                        while (isActive) {
                            setTorch(true)
                            delay(mode.delayOnMs)
                            setTorch(false)
                            delay(mode.delayOffMs)
                        }
                    }
                    FlashlightMode.SOS -> {
                        val dot = 150L
                        val dash = 450L
                        val symbolGap = 150L
                        val letterGap = 450L
                        val wordGap = 1000L

                        while (isActive) {
                            // 3 dots
                            repeat(3) {
                                setTorch(true); delay(dot)
                                setTorch(false); delay(symbolGap)
                            }
                            delay(letterGap)
                            // 3 dashes
                            repeat(3) {
                                setTorch(true); delay(dash)
                                setTorch(false); delay(symbolGap)
                            }
                            delay(letterGap)
                            // 3 dots
                            repeat(3) {
                                setTorch(true); delay(dot)
                                setTorch(false); delay(symbolGap)
                            }
                            delay(wordGap)
                        }
                    }
                    else -> {}
                }
            } finally {
                setTorch(false)
            }
        }
    }

    fun stop() {
        strobeJob?.cancel()
        strobeJob = null
        setTorch(false)
    }

    private fun setTorch(enabled: Boolean) {
        val id = cameraId ?: return
        try {
            cameraManager?.setTorchMode(id, enabled)
        } catch (e: Exception) {
            // Ignore camera lock exceptions
        }
    }
}
