package uk.co.sullenart.photoalbum.items

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import uk.co.sullenart.photoalbum.google.GooglePhotos
import java.io.File

class MediaItemsRepository(
    private val realm: Realm,
    private val googlePhotos: GooglePhotos,
    private val itemUtils: ItemUtils,
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

    private fun isInCache(item: MediaItem): Boolean {
        val thumbExists = File(itemUtils.getThumbnailFilename(item)).exists()
        val detailExists = File(itemUtils.getDetailFilename(item)).exists()
        return thumbExists && detailExists
    }

    private suspend fun addToCache(item: MediaItem) {
        googlePhotos.saveMediaFile(item.thumbnailUrl, itemUtils.getThumbnailFilename(item))
        Timber.d("Thumbnail downloaded to [${itemUtils.getThumbnailFilename(item)}]")

        googlePhotos.saveMediaFile(item.detailUrl, itemUtils.getDetailFilename(item))
        Timber.d("Detail downloaded to [${itemUtils.getDetailFilename(item)}]")
    }

    private fun removeFromCache(item: MediaItem) {
        File(itemUtils.getThumbnailFilename(item)).delete()
        File(itemUtils.getDetailFilename(item)).delete()
        Timber.d("Media deleted")
    }

    fun clearCaches() {
        File(itemUtils.getMediaPath()).listFiles()?.forEach {
            it.delete()
        }
        Timber.d("Cache cleared")
    }
}