package uk.co.sullenart.photoalbum.background

import timber.log.Timber
import uk.co.sullenart.photoalbum.albums.AlbumsRepository
import uk.co.sullenart.photoalbum.google.GooglePhotos
import uk.co.sullenart.photoalbum.items.MediaItemsRepository

class BackgroundFetcher(
    private val googlePhotos: GooglePhotos,
    private val albumsRepository: AlbumsRepository,
    private val itemsRepository: MediaItemsRepository,
) {
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

        val allIds = mutableListOf<String>()
        albums.forEach { album ->
            val mediaForAlbum = googlePhotos.getMediaForAlbum(album)
            itemsRepository.upsert(mediaForAlbum)
            itemsRepository.populateCache(album)
            Timber.d("Fetched ${mediaForAlbum.size} items for album ${album.title}")

            allIds.addAll(mediaForAlbum.map { it.id })
        }

        itemsRepository.prune(allIds)
    }
}