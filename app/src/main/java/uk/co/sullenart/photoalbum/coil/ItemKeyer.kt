package uk.co.sullenart.photoalbum.coil

import coil.key.Keyer
import coil.request.Options
import timber.log.Timber
import uk.co.sullenart.photoalbum.items.MediaItem

class ItemKeyer: Keyer<MediaItem> {
    override fun key(data: MediaItem, options: Options): String? {
        val type: String = options.parameters.value<String>("type").orEmpty()
        val result = "${data.id}-$type-${data.rotation.name}"
        Timber.d("Using cache key $result")
        return result
    }
}