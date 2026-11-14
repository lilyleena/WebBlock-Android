package com.kshrd.features.remote.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/** Project's upload limit: 2 MiB, JPEG or PNG (the server reads the bytes, never the name or type). */
internal const val MAX_PHOTO_BYTES = 2 * 1024 * 1024

/** The longest side kept when a photo must be re-encoded; enough for a product page. */
private const val MAX_SIDE_PX = 2048

/** JPEG qualities tried in turn until the photo fits (the iOS app's steps). */
@Suppress("MagicNumber") // The steps themselves.
private val JPEG_QUALITIES = listOf(85, 70, 55, 40)

/** A photo ready for `POST /media`. */
class EncodedPhoto(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String,
)

/** A photo the app can't read or can't bring under 2 MiB; the message is for the owner (not an IOException, which
 * the screens show as "no connection"). */
class PhotoNotUsableException(
    message: String,
) : Exception(message)

/** Turns a photo the owner picked into an upload for `POST /media`. */
interface PhotoEncoder {
    suspend fun encode(sourceUri: String): EncodedPhoto
}

/**
 * Reads a picked photo (`content://`): a JPEG or PNG of 2 MiB or less is sent as it is; anything else (larger, HEIC,
 * WebP…) is scaled to [MAX_SIDE_PX] and re-encoded as JPEG at falling quality until it fits, and refused if it never
 * does.
 */
class AndroidPhotoEncoder
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : PhotoEncoder {
        override suspend fun encode(sourceUri: String): EncodedPhoto =
            withContext(Dispatchers.IO) {
                val uri = Uri.parse(sourceUri)
                val resolver = context.contentResolver
                val bytes =
                    resolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw PhotoNotUsableException("The app couldn't read this photo. Choose another one.")
                val type = resolver.getType(uri)
                if ((type == JPEG || type == PNG) && bytes.size <= MAX_PHOTO_BYTES) {
                    EncodedPhoto(bytes, type, if (type == PNG) "photo.png" else "photo.jpg")
                } else {
                    reencode(bytes)
                }
            }

        private fun reencode(bytes: ByteArray): EncodedPhoto {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE_PX) sample *= 2
            val bitmap =
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
                    ?: throw PhotoNotUsableException("This file isn't a photo the app can read. Choose a JPEG or PNG.")
            try {
                for (quality in JPEG_QUALITIES) {
                    val out = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    if (out.size() <= MAX_PHOTO_BYTES) return EncodedPhoto(out.toByteArray(), JPEG, "photo.jpg")
                }
            } finally {
                bitmap.recycle()
            }
            throw PhotoNotUsableException("This photo is too large, even made smaller. Choose a smaller one (2 MB at most).")
        }

        private companion object {
            const val JPEG = "image/jpeg"
            const val PNG = "image/png"
        }
    }
