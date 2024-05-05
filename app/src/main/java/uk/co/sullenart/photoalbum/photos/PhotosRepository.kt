package uk.co.sullenart.photoalbum.photos

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    fun getPhotoFromId(id: String): Photo? =
        realm.query<RealmPhoto>("id == $0", id).first().find()?.toPhoto()

    suspend fun clear() {
        realm.write {
            delete(RealmPhoto::class)
        }
    }

    suspend fun sync(
        photos: List<Photo>,
        progress: ((Int, Int) -> Unit)? = null,
        ) {
        var count = 0
        progress?.invoke(photos.size, count)

        realm.write {
            photos.forEach { photo ->
                // Is there a current record for this photo?
                val result = query<RealmPhoto>("id == $0", photo.id).first().find()
                if (result == null) {
                    // No, create a new record.
                    Timber.d("Photo not found, new record created [${photo.id}]")
                    copyToRealm(photo.toRealm())
                    runBlocking {
                        addToCache(photo)
                    }
                } else {
                    // Yes, update its properties, Realm will update the persisted record once outside the "write" scope.
                    Timber.d("Photo record updated [${photo.id}]")
                    result.copyFromPhoto(photo)
                    if (!isInCache(photo)) {
                        runBlocking {
                            addToCache(photo)
                        }
                    }
                }
                progress?.invoke(photos.size, count++)
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

    private fun isInCache(photo: Photo): Boolean {
        val snapshot = imageLoader.diskCache?.openSnapshot(photo.id)
        snapshot?.close()
        return snapshot != null
    }

    @OptIn(ExperimentalCoilApi::class)
    private suspend fun addToCache(photo: Photo) {
        val request = ImageRequest.Builder(context)
            .data(photo.usableUrl)
            .diskCacheKey(photo.id)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .decoderFactory { _, _, _ ->
                Decoder { DecodeResult(ColorDrawable(Color.BLACK).asCoilImage(), false) }
            }
            .build()
        imageLoader.execute(request)
        Timber.d("Photo loaded into cache [${photo.id}]")
    }

    private fun removeFromCache(id: String) {
        imageLoader.diskCache?.run {
            remove(id)
            Timber.d("Photo removed from cache [${id}]")
        }
    }

    fun clearCaches() {
        imageLoader.diskCache?.clear()
        imageLoader.memoryCache?.clear()
        Timber.d("Photo caches cleared")
    }
}