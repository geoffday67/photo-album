package uk.co.sullenart.photoalbum.coil

import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okio.Path.Companion.toPath
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import uk.co.sullenart.photoalbum.items.ItemUtils
import uk.co.sullenart.photoalbum.items.MediaItem

class ItemFetcher(
    private val item: MediaItem,
    private val type: Type,
    private val itemUtils: ItemUtils,
) : Fetcher {
    enum class Type {
        THUMBNAIL, DETAIL,
    }

    class Factory : Fetcher.Factory<MediaItem>, KoinComponent {
        override fun create(data: MediaItem, options: Options, imageLoader: ImageLoader): Fetcher? {
            Timber.d("Creating factory for id ${data.id}, type ${options.parameters.value<String>("type")}")
            val type = when (options.parameters.value<String>("type")) {
                "thumbnail" -> Type.THUMBNAIL
                "detail" -> Type.DETAIL
                else -> return null
            }
            return get<ItemFetcher> { parametersOf(data, type) }
        }
    }

    override suspend fun fetch(): FetchResult {
        val destination = when (type) {
            Type.THUMBNAIL -> itemUtils.getThumbnailFilename(item)
            Type.DETAIL -> itemUtils.getDetailFilename(item)
        }
        Timber.d("Fetching item from $destination")

        return SourceResult(
            source = ImageSource(destination.toPath()),
            mimeType = item.mimeType,
            dataSource = DataSource.DISK,
        )
    }
}
