package com.example.findphonebyclap.service

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.findphonebyclap.data.model.FunctionMode
import com.example.findphonebyclap.engine.ClapDetector

class ClapDetectionService : BaseAlarmService() {

    private val clapDetector by lazy { ClapDetector(this) }

    override fun getServiceMode(): FunctionMode = FunctionMode.MODE_CLAP_FIND_PHONE

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        startListening()
        return result
    }

    private fun startListening() {
        val threshold = prefs.sensitivity.threshold
        clapDetector.start(threshold) {
            triggerAlarm(FunctionMode.MODE_CLAP_FIND_PHONE)
        }
    }

    override fun onDestroy() {
        clapDetector.stop()
        super.onDestroy()
    }

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, ClapDetectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ClapDetectionService::class.java)
            context.stopService(intent)
        }
    }
}
