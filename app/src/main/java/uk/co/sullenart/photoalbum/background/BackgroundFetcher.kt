package uk.co.sullenart.photoalbum.background

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.request.CachePolicy
import coil.request.ImageRequest
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
    private val context: Context,
    private val imageLoader: ImageLoader,
) {
    suspend fun start() {
        while (true) {
            refresh()
            // TODO Use WorkManager for scheduling updates.
            delay(5000)
        }
    }

    @OptIn(ExperimentalCoilApi::class)
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
            val snapshot = imageLoader.diskCache?.openSnapshot(it.id)
            if (snapshot == null) {
                val request = ImageRequest.Builder(context)
                    .data("${it.url}=w2048-h2048")
                    .diskCacheKey(it.id)
                    // Disable reading from/writing to the memory cache.
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    // Set a custom `Decoder.Factory` that skips the decoding step.
                    .decoderFactory { _, _, _ ->
                        Decoder { DecodeResult(ColorDrawable(Color.BLACK), false) }
                    }
                    .build()
                imageLoader.enqueue(request)
                Timber.d("Photo ${it.id} loaded into cache")
            }
            snapshot?.close()
        }
    }
}