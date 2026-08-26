package com.example.findphonebyclap.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.findphonebyclap.data.model.FlashlightMode
import com.example.findphonebyclap.data.model.FunctionMode
import com.example.findphonebyclap.data.model.SensitivityLevel
import com.example.findphonebyclap.data.model.VibrationMode

class AppPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var activeMode: FunctionMode
        get() {
            val name = prefs.getString(KEY_ACTIVE_MODE, FunctionMode.NONE.name)
            return try {
                FunctionMode.valueOf(name ?: FunctionMode.NONE.name)
            } catch (e: Exception) {
                FunctionMode.NONE
            }
        }
        set(value) = prefs.edit().putString(KEY_ACTIVE_MODE, value.name).apply()

    var selectedSoundId: String
        get() = prefs.getString(KEY_SELECTED_SOUND, "airhorn") ?: "airhorn"
        set(value) = prefs.edit().putString(KEY_SELECTED_SOUND, value).apply()

    var isFlashlightEnabled: Boolean
        get() = prefs.getBoolean(KEY_FLASH_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_FLASH_ENABLED, value).apply()

    var flashlightMode: FlashlightMode
        get() {
            val name = prefs.getString(KEY_FLASH_MODE, FlashlightMode.DEFAULT.name)
            return try {
                FlashlightMode.valueOf(name ?: FlashlightMode.DEFAULT.name)
            } catch (e: Exception) {
                FlashlightMode.DEFAULT
            }
        }
        set(value) = prefs.edit().putString(KEY_FLASH_MODE, value.name).apply()

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()

    var vibrationMode: VibrationMode
        get() {
            val name = prefs.getString(KEY_VIBRATION_MODE, VibrationMode.DEFAULT.name)
            return try {
                VibrationMode.valueOf(name ?: VibrationMode.DEFAULT.name)
            } catch (e: Exception) {
                VibrationMode.DEFAULT
            }
        }
        set(value) = prefs.edit().putString(KEY_VIBRATION_MODE, value.name).apply()

    var alarmDurationSeconds: Int
        get() = prefs.getInt(KEY_ALARM_DURATION, 30) // default 30s
        set(value) = prefs.edit().putInt(KEY_ALARM_DURATION, value).apply()

    var alarmVolumePercent: Int
        get() = prefs.getInt(KEY_ALARM_VOLUME, 100) // default 100%
        set(value) = prefs.edit().putInt(KEY_ALARM_VOLUME, value).apply()

    var sensitivity: SensitivityLevel
        get() {
            val name = prefs.getString(KEY_SENSITIVITY, SensitivityLevel.MEDIUM.name)
            return try {
                SensitivityLevel.valueOf(name ?: SensitivityLevel.MEDIUM.name)
            } catch (e: Exception) {
                SensitivityLevel.MEDIUM
            }
        }
        set(value) = prefs.edit().putString(KEY_SENSITIVITY, value.name).apply()

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    var languageCode: String
        get() = prefs.getString(KEY_LANGUAGE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    companion object {
        private const val PREFS_NAME = "find_phone_by_clap_prefs"
        private const val KEY_ACTIVE_MODE = "key_active_mode"
        private const val KEY_SELECTED_SOUND = "key_selected_sound"
        private const val KEY_FLASH_ENABLED = "key_flash_enabled"
        private const val KEY_FLASH_MODE = "key_flash_mode"
        private const val KEY_VIBRATION_ENABLED = "key_vibration_enabled"
        private const val KEY_VIBRATION_MODE = "key_vibration_mode"
        private const val KEY_ALARM_DURATION = "key_alarm_duration"
        private const val KEY_ALARM_VOLUME = "key_alarm_volume"
        private const val KEY_SENSITIVITY = "key_sensitivity"
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
        private const val KEY_LANGUAGE = "key_language"

        @Volatile
        private var instance: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return instance ?: synchronized(this) {
                instance ?: AppPreferences(context).also { instance = it }
            }
        }
    }
}
