package uk.co.sullenart.photoalbum.items

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import uk.co.sullenart.photoalbum.google.GooglePhotos
import java.io.File
import java.nio.file.Files

class MediaItemsRepository(
    private val realm: Realm,
    private val context: Context,
    private val imageLoader: ImageLoader,
    private val googlePhotos: GooglePhotos,
) {
    fun getItemFlowForAlbum(albumId: String): Flow<List<MediaItem>> =
        realm.query<RealmItem>("albumId == $0", albumId).asFlow().map {
            it.list.mapNotNull { it.toMediaItem() }
        }

    fun getCountForAlbum(albumId: String): Int =
        realm.query<RealmItem>("albumId == $0", albumId).count().find().toInt()

    suspend fun clear() {
        realm.write {
            delete(RealmItem::class)
        }
    }

    suspend fun sync(
        albumId: String,
        items: List<MediaItem>,
        progress: (() -> Unit)? = null,
    ) {
        realm.write {
            items.forEach { item ->
                // Is there a current record for this photo?
                val result = query<RealmItem>("id == $0 AND albumId == $1", item.id, albumId).first().find()
                if (result == null) {
                    // No, create a new record.
                    Timber.d("Photo not found, new record created [${item.id}]")
                    runBlocking {
                        addToCache(item)
                    }
                    copyToRealm(item.toRealmItem())
                } else {
                    // Yes, update its properties, Realm will update the persisted record once outside the "write" scope.
                    Timber.d("Photo record updated [${item.id}]")
                    if (!isInCache(item)) {
                        runBlocking {
                            addToCache(item)
                        }
                    }
                    result.copyFromItem(item)
                }
                progress?.invoke()
            }

            // Remove photos that we have locally but which aren't in the list.
            val newIds = items.map { it.id }
            query<RealmItem>("albumId == $0", albumId).find().forEach { existing ->
                if (!newIds.contains(existing.id)) {
                    Timber.d("Deleting record [${existing.id}]")
                    existing.toMediaItem()?.let {
                        removeFromCache(it)
                    }
                    delete(existing)
                }
            }
        }
    }

    private fun isInCache(photo: MediaItem): Boolean {
        val snapshot = imageLoader.diskCache?.openSnapshot(photo.id)
        snapshot?.close()
        return snapshot != null
    }

    @OptIn(ExperimentalCoilApi::class)
    private suspend fun addToCache(item: MediaItem) {
        val request = ImageRequest.Builder(context)
            .data(item.usableUrl)
            .diskCacheKey(item.id)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .decoderFactory { _, _, _ ->
                Decoder { DecodeResult(ColorDrawable(Color.BLACK).asCoilImage(), false) }
            }
            .build()
        imageLoader.execute(request)

        // If it's a video then also download the video file into app storage.
        // TODO Use external storage.
        if (item is VideoItem) {
            val dest = "${context.filesDir}${File.separator}${item.id}.mp4"
            googlePhotos.saveMediaFile(item.downloadUrl, dest)
            item.path = dest
            Timber.d("Video downloaded to [${item.path}]")
        }

        Timber.d("Item loaded into cache [${item.id}]")
    }

    private fun removeFromCache(item: MediaItem) {
        imageLoader.diskCache?.run {
            remove(item.id)
            Timber.d("Item removed from cache [${item.id}]")
        }

        if (item is VideoItem) {
            val dest = "${context.filesDir}${File.separator}${item.id}.mp4"
            File(dest).delete()
        }
    }

    fun clearCaches() {
        imageLoader.diskCache?.clear()
        imageLoader.memoryCache?.clear()
        Timber.d("Caches cleared")
    }
}