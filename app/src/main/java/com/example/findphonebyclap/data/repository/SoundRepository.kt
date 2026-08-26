package com.example.findphonebyclap.data.repository

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.findphonebyclap.R
import com.example.findphonebyclap.data.model.SoundItem

class SoundRepository(private val context: Context) {

    private var previewMediaPlayer: MediaPlayer? = null
    var onPreviewStateChanged: ((Boolean) -> Unit)? = null

    val allSounds: List<SoundItem> = listOf(
        SoundItem("airhorn", "airhorn.mp3", R.string.sound_airhorn, R.drawable.boomclap_btn_home_clap),
        SoundItem("warning", "warning.mp3", R.string.sound_warning, R.drawable.boomclap_btn_home_2),
        SoundItem("cat", "Cat.mp3", R.string.sound_cat, R.drawable.boomclap_btn_home_3),
        SoundItem("dog", "dog.mp3", R.string.sound_dog, R.drawable.boomclap_btn_home_4),
        SoundItem("rooster", "rooster.mp3", R.string.sound_rooster, R.drawable.boomclap_btn_home_5),
        SoundItem("gun", "gun.mp3", R.string.sound_gun, R.drawable.boomclap_btn_home_default),
        SoundItem("bomb", "bomb.mp3", R.string.sound_bomb, R.drawable.boomclap_btn_home_touch),
        SoundItem("baby_smile", "BabySmile.mp3", R.string.sound_baby_smile, R.drawable.boomclap_btn_home_pocket),
        SoundItem("hahaha", "hahaha.mp3", R.string.sound_hahaha, R.drawable.boomclap_btn_home_clap),
        SoundItem("nokia", "Nokia Ringtone.mp3", R.string.sound_nokia, R.drawable.boomclap_btn_home_2),
        SoundItem("horn_stabs", "HornStabs.mp3", R.string.sound_horn_stabs, R.drawable.boomclap_btn_home_3),
        SoundItem("cow", "Cow.mp3", R.string.sound_cow, R.drawable.boomclap_btn_home_4),
        SoundItem("happy", "Happy.mp3", R.string.sound_happy, R.drawable.boomclap_btn_home_5),
        SoundItem("here", "here.mp3", R.string.sound_here, R.drawable.boomclap_btn_home_default),
        SoundItem("burp", "LoudBurp.mp3", R.string.sound_burp, R.drawable.boomclap_btn_home_touch),
        SoundItem("magic", "Magic.mp3", R.string.sound_magic, R.drawable.boomclap_btn_home_pocket),
        SoundItem("oh_no", "Oh No.mpeg", R.string.sound_oh_no, R.drawable.boomclap_btn_home_clap),
        SoundItem("oiia", "Oiiaiiooiiai.mp3", R.string.sound_oiia, R.drawable.boomclap_btn_home_2),
        SoundItem("shriek", "shriek.mp3", R.string.sound_shriek, R.drawable.boomclap_btn_home_3),
        SoundItem("snoring", "snoring.mp3", R.string.sound_snoring, R.drawable.boomclap_btn_home_4),
        SoundItem("ting", "TingTingTing.mp3", R.string.sound_ting, R.drawable.boomclap_btn_home_5),
        SoundItem("wolf", "VIP_Wolf.mp3", R.string.sound_wolf, R.drawable.boomclap_btn_home_default),
        SoundItem("disco", "vip_Disco.mp3", R.string.sound_disco, R.drawable.boomclap_btn_home_touch),
        SoundItem("car_horn", "vip_CarHorn.mp3", R.string.sound_car_horn, R.drawable.boomclap_btn_home_pocket),
        SoundItem("fart", "vip_Fart.mp3", R.string.sound_fart, R.drawable.boomclap_btn_home_clap),
        SoundItem("horror", "vip_Horror.mp3", R.string.sound_horror, R.drawable.boomclap_btn_home_2),
        SoundItem("beatbox", "vip_Beatbox.mp3", R.string.sound_beatbox, R.drawable.boomclap_btn_home_3)
    )

    fun getSoundById(id: String): SoundItem {
        return allSounds.find { it.id == id } ?: allSounds.first()
    }

    fun playPreview(soundItem: SoundItem) {
        stopPreview()
        try {
            val afd: AssetFileDescriptor = context.assets.openFd("audio/${soundItem.audioFileName}")
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
                setOnCompletionListener {
                    onPreviewStateChanged?.invoke(false)
                }
                start()
            }
            previewMediaPlayer = player
            onPreviewStateChanged?.invoke(true)
        } catch (e: Exception) {
            e.printStackTrace()
            onPreviewStateChanged?.invoke(false)
        }
    }

    fun stopPreview() {
        try {
            previewMediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            previewMediaPlayer = null
            onPreviewStateChanged?.invoke(false)
        }
    }

    fun isPreviewPlaying(): Boolean {
        return try {
            previewMediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }
}
