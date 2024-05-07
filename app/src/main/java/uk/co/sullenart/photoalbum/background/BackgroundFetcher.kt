package uk.co.sullenart.photoalbum.background

import kotlinx.coroutines.delay
import timber.log.Timber
import uk.co.sullenart.photoalbum.albums.AlbumsRepository
import uk.co.sullenart.photoalbum.google.GooglePhotos
import uk.co.sullenart.photoalbum.photos.Photo
import uk.co.sullenart.photoalbum.photos.PhotosRepository

class BackgroundFetcher(
    private val googlePhotos: GooglePhotos,
    private val albumsRepository: AlbumsRepository,
    private val photosRepository: PhotosRepository,
) {
    suspend fun start() {
        while (true) {
            refresh()
            // TODO Use WorkManager for scheduling updates.
            delay(5000)
        }
    }

    suspend fun refresh(
        progress: ((Int, Int) -> Unit)? = null,
    ) {
        val albums = googlePhotos.getSharedAlbums() ?: run {
            Timber.e("Failed to get shared album list from Google")
            return
        }
        Timber.d("Fetched ${albums.size} shared albums")
        albumsRepository.sync(albums)

        // TODO Decide which albums to get the photos for, currently it fetches photos if the number of photos in the album has changed.

        albums.forEach { album ->
            if (album.itemCount == photosRepository.getCountForAlbum(album.id)) {
                Timber.d("Skipping album ${album.title}, item count the same (${album.itemCount})")
                return@forEach
            }

            val photosForAlbum = googlePhotos.getPhotosForAlbum(album)
                ?.map { it.copy(albumId = album.id) } ?: run {
                Timber.e("Failed to get photos from Google for album ${album.title}")
                return@forEach
            }
            photosRepository.sync(album.id, photosForAlbum, progress)
            Timber.d("Fetched ${photosForAlbum.size} photos for album ${album.title}")
        }
    }
}