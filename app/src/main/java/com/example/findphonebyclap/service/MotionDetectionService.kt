package com.example.findphonebyclap.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.example.findphonebyclap.data.model.FunctionMode
import com.example.findphonebyclap.engine.MotionDetector

class MotionDetectionService : BaseAlarmService() {

    private val motionDetector by lazy { MotionDetector(this) }
    private val handler = Handler(Looper.getMainLooper())

    override fun getServiceMode(): FunctionMode = FunctionMode.MODE_NOT_TOUCH_PHONE

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        armMotionDetectorWithDelay()
        return result
    }

    private fun armMotionDetectorWithDelay() {
        motionDetector.stop()
        handler.removeCallbacksAndMessages(null)

        // Give user 3 seconds to set the phone down before arming
        handler.postDelayed({
            motionDetector.start {
                triggerAlarm(FunctionMode.MODE_NOT_TOUCH_PHONE)
            }
        }, 3000L)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        motionDetector.stop()
        super.onDestroy()
    }

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, MotionDetectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MotionDetectionService::class.java)
            context.stopService(intent)
        }
    }
}
