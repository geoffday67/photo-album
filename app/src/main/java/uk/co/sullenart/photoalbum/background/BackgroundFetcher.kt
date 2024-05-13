package uk.co.sullenart.photoalbum.background

import kotlinx.coroutines.delay
import timber.log.Timber
import uk.co.sullenart.photoalbum.albums.AlbumsRepository
import uk.co.sullenart.photoalbum.google.GooglePhotos
import uk.co.sullenart.photoalbum.items.MediaItemsRepository

class BackgroundFetcher(
    private val googlePhotos: GooglePhotos,
    private val albumsRepository: AlbumsRepository,
    private val itemsRepository: MediaItemsRepository,
) {
    suspend fun start() {
        while (true) {
            refresh()
            // TODO Use WorkManager for scheduling updates.
            delay(5000)
        }
    }

    suspend fun refresh(
        progress: ((total: Int, processed: Int) -> Unit)? = null,
    ) {
        val albums = googlePhotos.getSharedAlbums() ?: run {
            Timber.e("Failed to get shared album list from Google")
            return
        }
        Timber.d("Fetched ${albums.size} shared albums")
        albumsRepository.sync(albums)

        val totalItems = albums.fold(0) { acc, element -> acc + element.itemCount }
        var itemsProcessed = 0

        // TODO Decide which albums to get the photos for, currently it fetches photos if the number of photos in the album has changed.

        albums.forEach { album ->
            if (album.itemCount == itemsRepository.getCountForAlbum(album.id)) {
                Timber.d("Skipping album ${album.title}, item count the same (${album.itemCount})")
                return@forEach
            }

            val mediaForAlbum = googlePhotos.getMediaForAlbum(album) ?: run {
                Timber.e("Failed to get items from Google for album ${album.title}")
                return@forEach
            }
            itemsRepository.sync(album.id, mediaForAlbum) {
                progress?.invoke(totalItems, ++itemsProcessed)
            }
            Timber.d("Fetched ${mediaForAlbum.size} items for album ${album.title}")
        }
    }
}