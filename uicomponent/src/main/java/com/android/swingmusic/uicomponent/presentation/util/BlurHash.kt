package com.android.swingmusic.uicomponent.presentation.util

import android.graphics.Bitmap
import android.graphics.Color
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign

/**
 * Decodes a blurhash (https://blurha.sh) into a tiny bitmap that stands in for an image
 * until the real one arrives. Decoded bitmaps are cached by hash; a 32×32 bitmap is 4 KB.
 */
object BlurHash {
    private const val SIZE = 32
    private const val CHARS =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz#$%*+,-.:;=?@[]^_{|}~"
    private val cache = LruCache<String, Bitmap>(256)

    fun cached(hash: String): Bitmap? = cache.get(hash)

    fun decode(hash: String, width: Int = SIZE, height: Int = SIZE): Bitmap? {
        cache.get(hash)?.let { return it }
        if (hash.length < 6) return null

        val sizeFlag = decode83(hash, 0, 1) ?: return null
        val numY = sizeFlag / 9 + 1
        val numX = sizeFlag % 9 + 1
        if (hash.length != 4 + 2 * numX * numY) return null

        val maxValue = ((decode83(hash, 1, 2) ?: return null) + 1) / 166f
        val colors = Array(numX * numY) { i ->
            if (i == 0) decodeDc(decode83(hash, 2, 6) ?: return null)
            else decodeAc(decode83(hash, 4 + i * 2, 6 + i * 2) ?: return null, maxValue)
        }

        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var r = 0f
                var g = 0f
                var b = 0f
                for (j in 0 until numY) {
                    val basisY = cos(PI * y * j / height)
                    for (i in 0 until numX) {
                        val basis = (cos(PI * x * i / width) * basisY).toFloat()
                        val color = colors[i + j * numX]
                        r += color[0] * basis
                        g += color[1] * basis
                        b += color[2] * basis
                    }
                }
                pixels[x + y * width] = Color.rgb(linearToSrgb(r), linearToSrgb(g), linearToSrgb(b))
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
            .also { cache.put(hash, it) }
    }

    private fun decode83(hash: String, from: Int, to: Int): Int? {
        var value = 0
        for (index in from until to) {
            val digit = CHARS.indexOf(hash[index])
            if (digit < 0) return null
            value = value * 83 + digit
        }
        return value
    }

    private fun decodeDc(value: Int) = floatArrayOf(
        srgbToLinear(value shr 16), srgbToLinear((value shr 8) and 255), srgbToLinear(value and 255)
    )

    private fun decodeAc(value: Int, maxValue: Float): FloatArray {
        val r = value / (19 * 19)
        val g = (value / 19) % 19
        val b = value % 19
        return floatArrayOf(
            signPow((r - 9) / 9f) * maxValue,
            signPow((g - 9) / 9f) * maxValue,
            signPow((b - 9) / 9f) * maxValue
        )
    }

    private fun signPow(value: Float) = sign(value) * abs(value).pow(2f)

    private fun srgbToLinear(value: Int): Float {
        val v = value / 255f
        return if (v <= 0.04045f) v / 12.92f else ((v + 0.055f) / 1.055f).pow(2.4f)
    }

    private fun linearToSrgb(value: Float): Int {
        val v = value.coerceIn(0f, 1f)
        return if (v <= 0.0031308f) (v * 12.92f * 255f + 0.5f).toInt()
        else ((1.055f * v.pow(1f / 2.4f) - 0.055f) * 255f + 0.5f).toInt()
    }
}

/** A painter for [hash], decoded off the main thread; null while decoding or when there is no hash. */
@Composable
fun rememberBlurhashPainter(hash: String?): Painter? {
    if (hash.isNullOrBlank()) return null
    val painter by produceState<Painter?>(
        initialValue = BlurHash.cached(hash)?.let { BitmapPainter(it.asImageBitmap()) },
        key1 = hash
    ) {
        if (value == null) {
            value = withContext(Dispatchers.Default) { BlurHash.decode(hash) }
                ?.let { BitmapPainter(it.asImageBitmap()) }
        }
    }
    return painter
}
