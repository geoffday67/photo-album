package uk.co.sullenart.photoalbum.items

import android.content.Context
import android.os.Environment
import timber.log.Timber
import java.io.File

class ItemUtils(
    context: Context,
) {
    private val root: String by lazy {
        val base: String = context.getExternalFilesDirs(null).firstOrNull { !Environment.isExternalStorageEmulated(it) }?.path
            ?: context.getExternalFilesDir(null)?.path.orEmpty()
        val result = "${base}/media"
        File(result).mkdirs()
        Timber.i("Using storage $result")
        result
    }

    fun getThumbnailFilename(item: MediaItem): String =
        "$root${File.separator}${item.id}-thumbnail.dat"

    fun getDetailFilename(item: MediaItem): String =
        "$root${File.separator}${item.id}-detail.dat"

    fun getMediaPath() : String = root
}
