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

    suspend fun refresh() {
        val albums = googlePhotos.getSharedAlbums() ?: run {
            Timber.e("Failed to get shared album list from Google")
            return
        }
        Timber.d("Fetched ${albums.size} shared albums")
        albumsRepository.sync(albums)

        val photos: MutableList<Photo> = mutableListOf()
        albums.forEach { album ->
            val photosForAlbum = googlePhotos.getPhotosForAlbum(album)
                ?.map { it.copy(albumId = album.id) } ?: run {
                Timber.e("Failed to get photos from Google for album ${album.title}")
                return
            }
            Timber.d("Fetched ${photosForAlbum.size} photos for album ${album.title}")
            photos.addAll(photosForAlbum)
        }

        photosRepository.sync(photos)
        photos.forEach {

            // Check Coil cache and fetch image if needed.
        }
    }
}