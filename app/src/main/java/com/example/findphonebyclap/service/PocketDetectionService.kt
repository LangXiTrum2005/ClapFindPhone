package com.example.findphonebyclap.service

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.findphonebyclap.data.model.FunctionMode
import com.example.findphonebyclap.engine.PocketDetector

class PocketDetectionService : BaseAlarmService() {

    private val pocketDetector by lazy { PocketDetector(this) }

    override fun getServiceMode(): FunctionMode = FunctionMode.MODE_ANTI_PICKPOCKET

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        if (prefs.activeMode == FunctionMode.MODE_ANTI_PICKPOCKET && intent?.action == null) {
            startListening()
        }
        return result
    }

    private fun startListening() {
        pocketDetector.start {
            triggerAlarm(FunctionMode.MODE_ANTI_PICKPOCKET)
        }
    }

    override fun onDestroy() {
        pocketDetector.stop()
        super.onDestroy()
    }

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, PocketDetectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, PocketDetectionService::class.java)
            context.stopService(intent)
        }
    }
}
