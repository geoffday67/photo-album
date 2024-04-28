package uk.co.sullenart.photoalbum.photos

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class PhotosRepository(
    private val realm: Realm,
    private val context: Context,
    private val imageLoader: ImageLoader,
) {
    fun getPhotoFlowForAlbum(albumId: String): Flow<List<Photo>> =
        realm.query<RealmPhoto>("albumId == $0", albumId).asFlow().map {
            it.list
                .map { it.toPhoto() }
        }

    suspend fun clear() {
        realm.write {
            delete(RealmPhoto::class)
        }
    }

    suspend fun sync(photos: List<Photo>) {
        // TODO Sync the Coil image cache as well as the local data.
        realm.write {
            photos.forEach { photo ->
                // Is there a current record for this photo?
                val result = query<RealmPhoto>("id == $0", photo.id).first().find()
                if (result == null) {
                    // No, create a new record.
                    Timber.d("Photo not found, new record created [${photo.id}]")
                    copyToRealm(photo.toRealm())
                    addToCache(photo)
                } else {
                    // Yes, update its properties, Realm will update the persisted record once outside the "write" scope.
                    Timber.d("Photo record updated [${photo.id}]")
                    result.copyFromPhoto(photo)
                    if (!isInCache(photo)) {
                        addToCache(photo)
                    }
                }
            }

            // Remove photos that we have locally but which aren't in the list.
            val newIds = photos.map { it.id }
            query<RealmPhoto>().find().forEach { existing ->
                if (!newIds.contains(existing.id)) {
                    Timber.d("Photo record deleted [${existing.id}]")
                    removeFromCache(existing.id)
                    delete(existing)
                }
            }
        }
    }

    @OptIn(ExperimentalCoilApi::class)
    private fun isInCache(photo: Photo): Boolean {
        val snapshot = imageLoader.diskCache?.openSnapshot(photo.id)
        snapshot?.close()
        return snapshot != null
    }

    private fun addToCache(photo: Photo) {
        val request = ImageRequest.Builder(context)
            // TODO Use a common method for getting the image URL.
            .data("${photo.url}=w2048-h2048")
            .diskCacheKey(photo.id)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .decoderFactory { _, _, _ ->
                Decoder { DecodeResult(ColorDrawable(Color.BLACK), false) }
            }
            .build()
        imageLoader.enqueue(request)
        Timber.d("Photo loaded into cache [${photo.id}]")
    }

    @OptIn(ExperimentalCoilApi::class)
    private fun removeFromCache(id: String) {
        imageLoader.diskCache?.run {
            remove(id)
            Timber.d("Photo removed from cache [${id}]")
        }
    }
}