package uk.co.sullenart.photoalbum.coil

import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import uk.co.sullenart.photoalbum.items.ItemUtils
import uk.co.sullenart.photoalbum.items.MediaItem

class ItemFetcher(
    private val item: MediaItem,
    private val itemUtils: ItemUtils,
) : Fetcher {
    class Factory : Fetcher.Factory<MediaItem>, KoinComponent {
        override fun create(data: MediaItem, options: Options, imageLoader: ImageLoader): Fetcher? {
            Timber.d("Creating factory for id ${data.id}")
            return get<ItemFetcher> { parametersOf(data) }
        }
    }

    override suspend fun fetch(): FetchResult {
        val destination = itemUtils.getPath(item)
        Timber.d("Fetching item from $destination")
        val source = FileSystem.SYSTEM.source(destination.toPath())
        val imageSource = ImageSource(
            source = source.buffer(),
            fileSystem = FileSystem.SYSTEM,
        )
        return SourceFetchResult(
            source = imageSource,
            dataSource = DataSource.DISK,
            mimeType = item.mimeType,
        )
    }
}