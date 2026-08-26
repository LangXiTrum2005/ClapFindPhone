package com.example.findphonebyclap.ui.setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.findphonebyclap.R
import com.example.findphonebyclap.data.model.LanguageItem
import com.example.findphonebyclap.data.prefs.AppPreferences
import com.example.findphonebyclap.databinding.ActivityLanguageBinding

class LanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageBinding
    private val prefs by lazy { AppPreferences.getInstance(this) }
    private lateinit var adapter: LanguageAdapter

    private val supportedLanguages = listOf(
        LanguageItem("en", "English", "English", R.drawable.boom_ic_language_us),
        LanguageItem("vi", "Vietnamese", "Tiếng Việt", R.drawable.country_vi),
        LanguageItem("es", "Spanish", "Español", R.drawable.boom_ic_lang_es_mx),
        LanguageItem("fr", "French", "Français", R.drawable.boom_ic_language_fr),
        LanguageItem("de", "German", "Deutsch", R.drawable.boom_ic_language_de),
        LanguageItem("pt", "Portuguese", "Português", R.drawable.boom_ic_lang_pt_br),
        LanguageItem("in", "Indonesian", "Bahasa Indonesia", R.drawable.boom_ic_language_id),
        LanguageItem("hi", "Hindi", "हिन्दी", R.drawable.boom_ic_language_hi),
        LanguageItem("ja", "Japanese", "日本語", R.drawable.boom_ic_language_ja),
        LanguageItem("ko", "Korean", "한국어", R.drawable.boom_ic_language_ko),
        LanguageItem("zh", "Chinese (Simplified)", "中文 (简体)", R.drawable.boom_ic_language_zh),
        LanguageItem("ru", "Russian", "Русский", R.drawable.country_ru),
        LanguageItem("ar", "Arabic", "العربية", R.drawable.boom_ic_language_ar)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val currentLang = prefs.languageCode.ifEmpty { "en" }

        adapter = LanguageAdapter(
            languages = supportedLanguages,
            selectedCode = currentLang
        ) { selectedItem ->
            prefs.languageCode = selectedItem.code
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(selectedItem.code))
            finish()
        }

        binding.rvLanguages.layoutManager = LinearLayoutManager(this)
        binding.rvLanguages.adapter = adapter
    }
}

