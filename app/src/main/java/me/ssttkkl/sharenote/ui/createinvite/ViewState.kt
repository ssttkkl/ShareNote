package me.ssttkkl.sharenote.ui.createinvite

import android.graphics.Bitmap

data class ViewState(
    val loading: Boolean = true,
    val qr: Bitmap? = null,
    val expiresAt: String = "",
)