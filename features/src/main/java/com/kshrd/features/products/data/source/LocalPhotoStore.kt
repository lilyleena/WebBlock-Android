package com.kshrd.features.products.data.source

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stand-in for uploading a product photo. It copies the picked image into the app's private
 * storage (shrunk to a sensible size) and returns a `file://` URL. The picker's own `content://`
 * link can expire, so the app keeps its own copy. With a real backend this becomes an upload that
 * returns a remote URL; nothing above the repository changes.
 */
@Singleton
class LocalPhotoStore
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        suspend fun store(sourceUri: String): Result<String> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val resolver = context.contentResolver
                    val uri = Uri.parse(sourceUri)
                    val size = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
                    if (size > MAX_BYTES) error(TOO_LARGE_MESSAGE)
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                    val sample = sampleSizeFor(maxOf(bounds.outWidth, bounds.outHeight))
                    val bitmap =
                        resolver.openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
                        } ?: error("Couldn't read that image.")
                    val dir = File(context.filesDir, "product_photos").apply { mkdirs() }
                    val file = File(dir, "${UUID.randomUUID()}.jpg")
                    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
                    bitmap.recycle()
                    Uri.fromFile(file).toString()
                }
            }

        private fun sampleSizeFor(longestSide: Int): Int {
            var sample = 1
            while (longestSide / (sample * 2) >= MAX_SIDE_PX) sample *= 2
            return sample
        }

        private companion object {
            const val MAX_BYTES = 2_000_000L
            const val TOO_LARGE_MESSAGE = "That file is larger than 2 MB."
            const val MAX_SIDE_PX = 1600
            const val JPEG_QUALITY = 85
        }
    }
