package com.example.findphonebyclap.ui.sound

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.findphonebyclap.R
import com.example.findphonebyclap.data.model.FlashlightMode
import com.example.findphonebyclap.data.model.VibrationMode
import com.example.findphonebyclap.data.prefs.AppPreferences
import com.example.findphonebyclap.data.repository.SoundRepository
import com.example.findphonebyclap.databinding.ActivitySoundSettingBinding

class SoundSettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoundSettingBinding
    private val prefs by lazy { AppPreferences.getInstance(this) }
    private val soundRepository by lazy { SoundRepository(this) }
    private lateinit var soundAdapter: SoundAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoundSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        setupCurrentSoundHeader()
        setupSoundGrid()
        setupFlashlightControls()
        setupVibrationControls()
        setupVolumeControl()
        setupDurationControls()
    }

    private fun setupCurrentSoundHeader() {
        val currentSound = soundRepository.getSoundById(prefs.selectedSoundId)
        binding.ivCurrentSoundCover.setImageResource(currentSound.iconRes)
        binding.tvCurrentSoundTitle.text = getString(currentSound.nameRes)

        binding.btnPlayPreview.setOnClickListener {
            if (soundRepository.isPreviewPlaying()) {
                soundRepository.stopPreview()
            } else {
                val sound = soundRepository.getSoundById(prefs.selectedSoundId)
                soundRepository.playPreview(sound)
            }
        }

        soundRepository.onPreviewStateChanged = { isPlaying ->
            binding.btnPlayPreview.text =
                if (isPlaying) getString(R.string.stop_preview) else getString(R.string.preview)
            soundAdapter.setPlayingSound(if (isPlaying) prefs.selectedSoundId else null)
        }
    }

    private fun setupSoundGrid() {
        soundAdapter = SoundAdapter(soundRepository.allSounds, prefs.selectedSoundId) { sound ->
            prefs.selectedSoundId = sound.id
            binding.ivCurrentSoundCover.setImageResource(sound.iconRes)
            binding.tvCurrentSoundTitle.text = getString(sound.nameRes)
            soundRepository.playPreview(sound)
        }

        binding.rvAllSounds.layoutManager = GridLayoutManager(this, 3)
        binding.rvAllSounds.adapter = soundAdapter
    }

    private fun setupFlashlightControls() {
        binding.switchFlashlight.isChecked = prefs.isFlashlightEnabled
        binding.switchFlashlight.setOnCheckedChangeListener { _, isChecked ->
            prefs.isFlashlightEnabled = isChecked
        }

        when (prefs.flashlightMode) {
            FlashlightMode.DEFAULT -> binding.chipFlashDefault.isChecked = true
            FlashlightMode.FAST -> binding.chipFlashFast.isChecked = true
            FlashlightMode.SOS -> binding.chipFlashSos.isChecked = true
            else -> binding.chipFlashDefault.isChecked = true
        }

        binding.chipGroupFlash.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            prefs.flashlightMode = when (checkedIds.first()) {
                R.id.chipFlashFast -> FlashlightMode.FAST
                R.id.chipFlashSos -> FlashlightMode.SOS
                else -> FlashlightMode.DEFAULT
            }
        }
    }

    private fun setupVibrationControls() {
        binding.switchVibration.isChecked = prefs.isVibrationEnabled
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.isVibrationEnabled = isChecked
        }

        when (prefs.vibrationMode) {
            VibrationMode.DEFAULT -> binding.chipVibDefault.isChecked = true
            VibrationMode.STRONG -> binding.chipVibStrong.isChecked = true
            VibrationMode.HEARTBEAT -> binding.chipVibHeartbeat.isChecked = true
            VibrationMode.SOS -> binding.chipVibSos.isChecked = true
            else -> binding.chipVibDefault.isChecked = true
        }

        binding.chipGroupVibration.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            prefs.vibrationMode = when (checkedIds.first()) {
                R.id.chipVibStrong -> VibrationMode.STRONG
                R.id.chipVibHeartbeat -> VibrationMode.HEARTBEAT
                R.id.chipVibSos -> VibrationMode.SOS
                else -> VibrationMode.DEFAULT
            }
        }
    }

    private fun setupVolumeControl() {
        binding.sliderVolume.value = prefs.alarmVolumePercent.toFloat()
        binding.tvVolumeVal.text = "${prefs.alarmVolumePercent}%"

        binding.sliderVolume.addOnChangeListener { _, value, _ ->
            val percent = value.toInt()
            prefs.alarmVolumePercent = percent
            binding.tvVolumeVal.text = "$percent%"
        }
    }

    private fun setupDurationControls() {
        when (prefs.alarmDurationSeconds) {
            15 -> binding.chipDur15s.isChecked = true
            30 -> binding.chipDur30s.isChecked = true
            60 -> binding.chipDur1m.isChecked = true
            120 -> binding.chipDur2m.isChecked = true
            0 -> binding.chipDurLoop.isChecked = true
            else -> binding.chipDur30s.isChecked = true
        }

        binding.chipGroupDuration.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            prefs.alarmDurationSeconds = when (checkedIds.first()) {
                R.id.chipDur15s -> 15
                R.id.chipDur1m -> 60
                R.id.chipDur2m -> 120
                R.id.chipDurLoop -> 0
                else -> 30
            }
        }
    }

    override fun onPause() {
        super.onPause()
        soundRepository.stopPreview()
    }

    override fun onDestroy() {
        soundRepository.stopPreview()
        super.onDestroy()
    }
}
