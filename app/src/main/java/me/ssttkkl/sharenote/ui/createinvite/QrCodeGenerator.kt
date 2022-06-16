package me.ssttkkl.sharenote.ui.createinvite

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter


object QrCodeGenerator {
    fun generate(
        content: String,
        edgeLength: Int,
    ): Bitmap {
        if (content.isEmpty())
            error("content must not be empty")

        if (edgeLength <= 0)
            error("edgeLength must be positive")

        val bitMatrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            edgeLength,
            edgeLength
        )

        val bitmap = Bitmap.createBitmap(edgeLength, edgeLength, Bitmap.Config.ARGB_8888)
        for (i in 0 until edgeLength) {
            for (j in 0 until edgeLength) {
                if (bitMatrix[i, j])
                    bitmap[i, j] = Color.BLACK
            }
        }

        return bitmap
    }
}