package com.example.findphonebyclap.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.findphonebyclap.R
import com.example.findphonebyclap.data.model.FunctionMode
import com.example.findphonebyclap.data.prefs.AppPreferences
import com.example.findphonebyclap.data.repository.SoundRepository
import com.example.findphonebyclap.databinding.FragmentMotionAlertBinding
import com.example.findphonebyclap.service.MotionDetectionService
import com.example.findphonebyclap.ui.sound.SoundAdapter
import com.example.findphonebyclap.ui.sound.SoundSettingActivity

class MotionAlertFragment : Fragment() {

    private var _binding: FragmentMotionAlertBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy { AppPreferences.getInstance(requireContext()) }
    private val soundRepository by lazy { SoundRepository(requireContext()) }
    private lateinit var soundAdapter: SoundAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMotionAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMotionButton()
        setupSoundList()
        updateUiState()
    }

    private fun setupMotionButton() {
        binding.btnMotion.setIconResource(R.drawable.boomclap_btn_home_touch)
        binding.btnMotion.setOnClickListener {
            toggleActiveState()
        }
    }

    private fun toggleActiveState() {
        if (prefs.activeMode == FunctionMode.MODE_NOT_TOUCH_PHONE) {
            prefs.activeMode = FunctionMode.NONE
            MotionDetectionService.stopService(requireContext())
        } else {
            prefs.activeMode = FunctionMode.MODE_NOT_TOUCH_PHONE
            MotionDetectionService.startService(requireContext())
        }
        updateUiState()
    }

    fun updateUiState() {
        if (_binding == null) return
        val isActive = prefs.activeMode == FunctionMode.MODE_NOT_TOUCH_PHONE
        binding.btnMotion.setActiveState(isActive)

        if (isActive) {
            binding.tvStatusTitle.text = getString(R.string.active)
            binding.tvStatusTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.neon_active_green))
            binding.tvStatusDesc.text = getString(R.string.status_touch_active)
        } else {
            binding.tvStatusTitle.text = getString(R.string.tap_to_activate)
            binding.tvStatusTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            binding.tvStatusDesc.text = getString(R.string.tap_to_activate)
        }
    }

    private fun setupSoundList() {
        val sounds = soundRepository.allSounds.take(6)
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
        soundAdapter.setSelectedSound(prefs.selectedSoundId)
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
