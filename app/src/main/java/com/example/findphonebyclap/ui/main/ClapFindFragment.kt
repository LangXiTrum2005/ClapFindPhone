package com.example.findphonebyclap.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.findphonebyclap.R
import com.example.findphonebyclap.data.model.FunctionMode
import com.example.findphonebyclap.data.model.SensitivityLevel
import com.example.findphonebyclap.data.prefs.AppPreferences
import com.example.findphonebyclap.data.repository.SoundRepository
import com.example.findphonebyclap.databinding.FragmentClapFindBinding
import com.example.findphonebyclap.service.ClapDetectionService
import com.example.findphonebyclap.service.ServiceManager
import com.example.findphonebyclap.ui.sound.SoundAdapter
import com.example.findphonebyclap.ui.sound.SoundSettingActivity

class ClapFindFragment : Fragment() {

    private var _binding: FragmentClapFindBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy { AppPreferences.getInstance(requireContext()) }
    private val soundRepository by lazy { SoundRepository(requireContext()) }
    private lateinit var soundAdapter: SoundAdapter

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            toggleActiveState()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.permission_audio_desc),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClapFindBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClapButton()
        setupSensitivitySlider()
        setupSoundList()
        updateUiState()
    }

    private fun setupClapButton() {
        binding.clapButton.setIconResource(R.drawable.boomclap_btn_home_clap)
        binding.clapButton.setOnClickListener {
            if (prefs.activeMode != FunctionMode.MODE_CLAP_FIND_PHONE) {
                checkPermissionAndActivate()
            } else {
                toggleActiveState()
            }
        }
    }

    private fun checkPermissionAndActivate() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            toggleActiveState()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun toggleActiveState() {
        ServiceManager.toggleMode(requireContext(), FunctionMode.MODE_CLAP_FIND_PHONE)
        updateUiState()
    }

    fun updateUiState() {
        if (_binding == null) return
        val isActive = prefs.activeMode == FunctionMode.MODE_CLAP_FIND_PHONE
        binding.clapButton.setActiveState(isActive)

        if (isActive) {
            binding.statusPillContainer.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_status_pill_active)
            binding.viewStatusDot.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.neon_active_green)
            binding.tvStatusTitle.text = getString(R.string.active)
            binding.tvStatusTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.neon_active_green))
            binding.tvStatusDesc.text = getString(R.string.status_clap_active)
        } else {
            binding.statusPillContainer.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_status_pill_inactive)
            binding.viewStatusDot.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.text_hint)
            binding.tvStatusTitle.text = getString(R.string.tap_to_activate)
            binding.tvStatusTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            binding.tvStatusDesc.text = getString(R.string.tap_to_activate)
        }
    }

    private fun setupSensitivitySlider() {
        val currentLevel = prefs.sensitivity
        binding.sliderSensitivity.value = when (currentLevel) {
            SensitivityLevel.LOW -> 0.0f
            SensitivityLevel.MEDIUM -> 1.0f
            SensitivityLevel.HIGH -> 2.0f
        }
        updateSensitivityLabel(currentLevel)

        binding.sliderSensitivity.addOnChangeListener { _, value, _ ->
            val level = when (value.toInt()) {
                0 -> SensitivityLevel.LOW
                1 -> SensitivityLevel.MEDIUM
                else -> SensitivityLevel.HIGH
            }
            prefs.sensitivity = level
            updateSensitivityLabel(level)
            if (prefs.activeMode == FunctionMode.MODE_CLAP_FIND_PHONE) {
                ClapDetectionService.startService(requireContext())
            }
        }
    }

    private fun updateSensitivityLabel(level: SensitivityLevel) {
        binding.tvSensitivityVal.text = when (level) {
            SensitivityLevel.LOW -> getString(R.string.sensitivity_low)
            SensitivityLevel.MEDIUM -> getString(R.string.sensitivity_medium)
            SensitivityLevel.HIGH -> getString(R.string.sensitivity_high)
        }
    }

    private fun setupSoundList() {
        val sounds = soundRepository.getQuickSounds(prefs.selectedSoundId, 6)
        soundAdapter = SoundAdapter(sounds, prefs.selectedSoundId) { selectedItem ->
            prefs.selectedSoundId = selectedItem.id
            soundRepository.playPreview(selectedItem)
        }

        binding.rvSounds.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvSounds.adapter = soundAdapter

        soundRepository.onPreviewStateChanged = { isPlaying ->
            if (_binding != null) {
                soundAdapter.setPlayingSound(if (isPlaying) prefs.selectedSoundId else null)
            }
        }

        binding.btnMoreSounds.setOnClickListener {
            val intent = Intent(requireContext(), SoundSettingActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUiState()
        setupSoundList()
    }

    override fun onPause() {
        super.onPause()
        soundRepository.stopPreview()
    }

    override fun onDestroyView() {
        soundRepository.stopPreview()
        super.onDestroyView()
        _binding = null
    }
}
