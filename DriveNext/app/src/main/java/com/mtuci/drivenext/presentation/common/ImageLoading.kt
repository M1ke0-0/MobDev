package com.mtuci.drivenext.presentation.common

import android.widget.ImageView
import coil.load
import com.mtuci.drivenext.R

/** Coil скачивает картинку в фоне, кэширует и сам отменяет загрузку, если ячейку списка переиспользовали. */
fun ImageView.loadCarImage(url: String?) = load(url) {
    crossfade(true)
    placeholder(R.drawable.img_car_placeholder)
    error(R.drawable.img_car_placeholder)
    fallback(R.drawable.img_car_placeholder)
}

fun ImageView.loadAvatar(url: String?) = load(url) {
    crossfade(true)
    placeholder(R.drawable.img_avatar_placeholder)
    error(R.drawable.img_avatar_placeholder)
    fallback(R.drawable.img_avatar_placeholder)
}
