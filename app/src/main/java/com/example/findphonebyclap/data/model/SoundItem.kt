package com.example.findphonebyclap.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import java.io.Serializable

data class SoundItem(
    val id: String,
    val audioFileName: String,
    @StringRes val nameRes: Int,
    @DrawableRes val iconRes: Int,
    val isVip: Boolean = false
) : Serializable
