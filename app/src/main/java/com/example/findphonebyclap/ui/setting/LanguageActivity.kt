package com.example.findphonebyclap.ui.setting

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import com.example.findphonebyclap.R
import com.example.findphonebyclap.data.prefs.AppPreferences
import com.example.findphonebyclap.databinding.ActivityLanguageBinding

class LanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageBinding
    private val prefs by lazy { AppPreferences.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        updateLanguageSelection()

        binding.cardLangEn.setOnClickListener {
            prefs.languageCode = "en"
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            updateLanguageSelection()
            finish()
        }

        binding.cardLangVi.setOnClickListener {
            prefs.languageCode = "vi"
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("vi"))
            updateLanguageSelection()
            finish()
        }
    }

    private fun updateLanguageSelection() {
        val currentLang = prefs.languageCode
        val isVi = currentLang == "vi"

        binding.ivCheckEn.visibility = if (!isVi) View.VISIBLE else View.GONE
        binding.ivCheckVi.visibility = if (isVi) View.VISIBLE else View.GONE

        binding.cardLangEn.strokeColor = ContextCompat.getColor(
            this,
            if (!isVi) R.color.primary_purple else R.color.app_card_border
        )
        binding.cardLangVi.strokeColor = ContextCompat.getColor(
            this,
            if (isVi) R.color.primary_purple else R.color.app_card_border
        )
    }
}
