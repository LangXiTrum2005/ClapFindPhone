package com.example.findphonebyclap.ui.setting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.findphonebyclap.databinding.ActivitySettingBinding
import com.example.findphonebyclap.ui.sound.SoundSettingActivity

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.tvVersionVal.text = "1.0.0"

        binding.btnRowSound.setOnClickListener {
            startActivity(Intent(this, SoundSettingActivity::class.java))
        }

        binding.btnRowLanguage.setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
        }

        binding.btnRowGuide.setOnClickListener {
            startActivity(Intent(this, GuideActivity::class.java))
        }

        binding.btnRowPrivacy.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://policies.google.com/privacy")
            )
            startActivity(browserIntent)
        }
    }
}
