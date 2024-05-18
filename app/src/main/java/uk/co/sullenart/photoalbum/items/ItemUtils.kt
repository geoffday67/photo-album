package uk.co.sullenart.photoalbum.items

import android.content.Context
import timber.log.Timber
import java.io.File

class ItemUtils(
    context: Context,
) {
    private val root: String by lazy { "${context.filesDir}${File.separator}media" }

    init {
        File(root).mkdir()
        Timber.d("Media directory created")
    }

    fun getThumbnailFilename(item: MediaItem): String =
        "$root${File.separator}${item.id}-thumbnail.dat"

    fun getDetailFilename(item: MediaItem): String =
        "$root${File.separator}${item.id}-detail.dat"

    fun getMediaPath() : String = root
}
