package com.example.findphonebyclap

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.findphonebyclap.data.prefs.AppPreferences
import com.example.findphonebyclap.databinding.ActivityMainBinding
import com.example.findphonebyclap.ui.main.ClapFindFragment
import com.example.findphonebyclap.ui.main.MotionAlertFragment
import com.example.findphonebyclap.ui.main.PocketAlertFragment
import com.example.findphonebyclap.ui.setting.GuideActivity
import com.example.findphonebyclap.ui.setting.SettingActivity
import com.example.findphonebyclap.ui.sound.SoundSettingActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val prefs by lazy { AppPreferences.getInstance(this) }

    private val clapFragment = ClapFindFragment()
    private val motionFragment = MotionAlertFragment()
    private val pocketFragment = PocketAlertFragment()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupTabs()
        setupTopBarActions()
        checkNotificationPermission()
        checkFirstLaunchGuide()
    }

    private fun setupViewPager() {
        val fragments = listOf(clapFragment, motionFragment, pocketFragment)
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabSelection(position)
            }
        })
    }

    private fun setupTabs() {
        binding.tabClap.setOnClickListener { binding.viewPager.currentItem = 0 }
        binding.tabTouch.setOnClickListener { binding.viewPager.currentItem = 1 }
        binding.tabPocket.setOnClickListener { binding.viewPager.currentItem = 2 }
        updateTabSelection(0)
    }

    private fun updateTabSelection(selectedTab: Int) {
        binding.tabClap.background = if (selectedTab == 0) ContextCompat.getDrawable(this, R.drawable.bg_tab_rounded_selected) else ContextCompat.getDrawable(this, R.drawable.bg_tab_rounded_unselected)
        binding.tabClap.setTextColor(if (selectedTab == 0) ContextCompat.getColor(this, R.color.white) else ContextCompat.getColor(this, R.color.text_secondary))

        binding.tabTouch.background = if (selectedTab == 1) ContextCompat.getDrawable(this, R.drawable.bg_tab_rounded_selected) else ContextCompat.getDrawable(this, R.drawable.bg_tab_rounded_unselected)
        binding.tabTouch.setTextColor(if (selectedTab == 1) ContextCompat.getColor(this, R.color.white) else ContextCompat.getColor(this, R.color.text_secondary))

        binding.tabPocket.background = if (selectedTab == 2) ContextCompat.getDrawable(this, R.drawable.bg_tab_rounded_selected) else ContextCompat.getDrawable(this, R.drawable.bg_tab_rounded_unselected)
        binding.tabPocket.setTextColor(if (selectedTab == 2) ContextCompat.getColor(this, R.color.white) else ContextCompat.getColor(this, R.color.text_secondary))
    }

    private fun setupTopBarActions() {
        binding.btnSoundSettings.setOnClickListener {
            startActivity(Intent(this, SoundSettingActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun checkFirstLaunchGuide() {
        if (prefs.isFirstLaunch) {
            prefs.isFirstLaunch = false
            startActivity(Intent(this, GuideActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        clapFragment.updateUiState()
        motionFragment.updateUiState()
        pocketFragment.updateUiState()
    }
}