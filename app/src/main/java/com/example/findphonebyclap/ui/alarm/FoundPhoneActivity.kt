package com.example.findphonebyclap.ui.alarm

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.findphonebyclap.R
import com.example.findphonebyclap.data.model.FunctionMode
import com.example.findphonebyclap.databinding.ActivityFoundPhoneBinding
import com.example.findphonebyclap.service.ClapDetectionService
import com.example.findphonebyclap.service.MotionDetectionService
import com.example.findphonebyclap.service.PocketDetectionService
import com.example.findphonebyclap.service.ServiceManager
import com.example.findphonebyclap.util.Constants
import java.lang.ref.WeakReference

class FoundPhoneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoundPhoneBinding
    private var pulseAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeActivityRef = WeakReference(this)

        configureLockScreenFlags()

        binding = ActivityFoundPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val modeName = intent.getStringExtra(Constants.EXTRA_FUNCTION_MODE)
        val mode = try {
            FunctionMode.valueOf(modeName ?: FunctionMode.MODE_CLAP_FIND_PHONE.name)
        } catch (e: Exception) {
            FunctionMode.MODE_CLAP_FIND_PHONE
        }

        setupTitle(mode)
        setupPulseAnimation()

        binding.btnStopAlarm.setOnClickListener {
            stopAllAlarms()
            finish()
        }
    }

    private fun configureLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun setupTitle(mode: FunctionMode) {
        when (mode) {
            FunctionMode.MODE_CLAP_FIND_PHONE -> {
                binding.tvAlarmTitle.text = getString(R.string.phone_found_title)
                binding.ivAlertRadar.setImageResource(R.drawable.boomclap_ic_activate)
            }
            FunctionMode.MODE_NOT_TOUCH_PHONE -> {
                binding.tvAlarmTitle.text = getString(R.string.motion_detected_title)
                binding.ivAlertRadar.setImageResource(R.drawable.boomclap_btn_home_touch)
            }
            FunctionMode.MODE_ANTI_PICKPOCKET -> {
                binding.tvAlarmTitle.text = getString(R.string.pocket_detected_title)
                binding.ivAlertRadar.setImageResource(R.drawable.boomclap_btn_home_pocket)
            }
            else -> {
                binding.tvAlarmTitle.text = getString(R.string.phone_found_title)
            }
        }
    }

    private fun setupPulseAnimation() {
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.25f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.25f)
        val alpha = PropertyValuesHolder.ofFloat("alpha", 0.7f, 1.0f)

        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
            binding.ivAlertRadar, scaleX, scaleY, alpha
        ).apply {
            duration = 600L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
    }

    private fun stopAllAlarms() {
        ServiceManager.stopAllServices(this)
    }

    override fun onDestroy() {
        pulseAnimator?.cancel()
        if (activeActivityRef?.get() == this) {
            activeActivityRef = null
        }
        super.onDestroy()
    }

    companion object {
        private var activeActivityRef: WeakReference<FoundPhoneActivity>? = null

        fun dismissActiveScreen() {
            activeActivityRef?.get()?.finish()
            activeActivityRef = null
        }
    }
}
