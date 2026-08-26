package com.example.findphonebyclap.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class MotionDetector(private val context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val gravity = FloatArray(3)
    private val linearAcceleration = FloatArray(3)
    private var isCalibrated = false
    private var lastStillTime = 0L
    private var isListening = false
    private var startTime = 0L

    var onMotionDetected: (() -> Unit)? = null

    fun start(listener: () -> Unit) {
        stop()
        onMotionDetected = listener
        isCalibrated = false
        lastStillTime = 0L
        startTime = System.currentTimeMillis()

        if (accelerometer != null && sensorManager != null) {
            isListening = true
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val alpha = 0.8f

        // Low-pass filter to isolate gravity
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        // High-pass filter to get linear acceleration
        linearAcceleration[0] = event.values[0] - gravity[0]
        linearAcceleration[1] = event.values[1] - gravity[1]
        linearAcceleration[2] = event.values[2] - gravity[2]

        val magnitude = sqrt(
            (linearAcceleration[0] * linearAcceleration[0] +
                    linearAcceleration[1] * linearAcceleration[1] +
                    linearAcceleration[2] * linearAcceleration[2]).toDouble()
        ).toFloat()

        if (magnitude >= 1.8f) {
            // Trigger if device was placed down and now moved, OR if after 4s delay user still hasn't placed it down
            if (isCalibrated || (System.currentTimeMillis() - startTime > 500L)) {
                onMotionDetected?.invoke()
            }
            isCalibrated = false
        } else {
            if (!isCalibrated) {
                isCalibrated = true
                lastStillTime = System.currentTimeMillis()
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
            isListening = false
            isCalibrated = false
        }
    }
}
