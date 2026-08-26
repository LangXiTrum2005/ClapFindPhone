package com.example.findphonebyclap.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper

class PocketDetector(private val context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private var isInsidePocket = false
    private var isTriggered = false
    private var isListening = false
    private val handler = Handler(Looper.getMainLooper())

    var onPocketExtracted: (() -> Unit)? = null

    fun start(listener: () -> Unit) {
        stop()
        onPocketExtracted = listener
        isInsidePocket = false
        isTriggered = false

        if (sensorManager != null) {
            val sensorToUse = lightSensor ?: proximitySensor
            if (sensorToUse != null) {
                isListening = true
                sensorManager.registerListener(this, sensorToUse, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            if (lux < 5.0f && !isInsidePocket) {
                isInsidePocket = true
                isTriggered = false
            } else if (lux >= 5.0f && isInsidePocket) {
                isInsidePocket = false
                if (!isTriggered) {
                    isTriggered = true
                    handler.postDelayed({
                        onPocketExtracted?.invoke()
                    }, 500L)
                }
            }
        } else if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            val distance = event.values[0]
            val maxRange = event.sensor.maximumRange
            val isNear = distance < maxRange

            if (isNear && !isInsidePocket) {
                isInsidePocket = true
                isTriggered = false
            } else if (!isNear && isInsidePocket) {
                isInsidePocket = false
                if (!isTriggered) {
                    isTriggered = true
                    handler.postDelayed({
                        onPocketExtracted?.invoke()
                    }, 500L)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun stop() {
        if (isListening) {
            try {
                sensorManager?.unregisterListener(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            handler.removeCallbacksAndMessages(null)
            isListening = false
            isInsidePocket = false
            isTriggered = false
        }
    }
}
