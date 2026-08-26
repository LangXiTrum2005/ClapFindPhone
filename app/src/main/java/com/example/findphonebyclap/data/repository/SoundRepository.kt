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
        SoundItem("airhorn", "airhorn.mp3", R.string.sound_airhorn, R.mipmap.air_horn),
        SoundItem("warning", "warning.mp3", R.string.sound_warning, R.mipmap.warning),
        SoundItem("cat", "Cat.mp3", R.string.sound_cat, R.mipmap.cat),
        SoundItem("dog", "dog.mp3", R.string.sound_dog, R.mipmap.dog),
        SoundItem("rooster", "rooster.mp3", R.string.sound_rooster, R.mipmap.rooster),
        SoundItem("gun", "gun.mp3", R.string.sound_gun, R.mipmap.gun),
        SoundItem("bomb", "bomb.mp3", R.string.sound_bomb, R.mipmap.bomb),
        SoundItem("baby_smile", "BabySmile.mp3", R.string.sound_baby_smile, R.mipmap.baby_smile),
        SoundItem("hahaha", "hahaha.mp3", R.string.sound_hahaha, R.mipmap.hahaha),
        SoundItem("nokia", "Nokia Ringtone.mp3", R.string.sound_nokia, R.mipmap.nokia),
        SoundItem("horn_stabs", "HornStabs.mp3", R.string.sound_horn_stabs, R.mipmap.sound_horn_stab),
        SoundItem("cow", "Cow.mp3", R.string.sound_cow, R.mipmap.sound_cow),
        SoundItem("happy", "Happy.mp3", R.string.sound_happy, R.mipmap.happy),
        SoundItem("here", "here.mp3", R.string.sound_here, R.mipmap.here),
        SoundItem("burp", "LoudBurp.mp3", R.string.sound_burp, R.mipmap.sound_loud_burp),
        SoundItem("magic", "Magic.mp3", R.string.sound_magic, R.mipmap.sound_magic),
        SoundItem("oh_no", "Oh No.mpeg", R.string.sound_oh_no, R.mipmap.oh_no),
        SoundItem("oiia", "Oiiaiiooiiai.mp3", R.string.sound_oiia, R.mipmap.sound_oiiaiiooiiai),
        SoundItem("shriek", "shriek.mp3", R.string.sound_shriek, R.mipmap.shriek),
        SoundItem("snoring", "snoring.mp3", R.string.sound_snoring, R.mipmap.snoring),
        SoundItem("ting", "TingTingTing.mp3", R.string.sound_ting, R.mipmap.sound_tingtingting),
        SoundItem("wolf", "VIP_Wolf.mp3", R.string.sound_wolf, R.mipmap.sound_wolf, isVip = true),
        SoundItem("disco", "vip_Disco.mp3", R.string.sound_disco, R.mipmap.sound_disco, isVip = true),
        SoundItem("car_horn", "vip_CarHorn.mp3", R.string.sound_car_horn, R.mipmap.car_horn, isVip = true),
        SoundItem("fart", "vip_Fart.mp3", R.string.sound_fart, R.mipmap.sound_fart, isVip = true),
        SoundItem("horror", "vip_Horror.mp3", R.string.sound_horror, R.mipmap.sound_horror, isVip = true),
        SoundItem("beatbox", "vip_Beatbox.mp3", R.string.sound_beatbox, R.mipmap.sound_beatbox, isVip = true)
    )

    fun getSoundById(id: String): SoundItem {
        return allSounds.find { it.id == id } ?: allSounds.first()
    }

    fun getQuickSounds(selectedId: String, count: Int = 6): List<SoundItem> {
        val selected = allSounds.find { it.id == selectedId }
        val rest = allSounds.filter { it.id != selectedId }
        return if (selected != null) {
            listOf(selected) + rest.take(count - 1)
        } else {
            allSounds.take(count)
        }
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
