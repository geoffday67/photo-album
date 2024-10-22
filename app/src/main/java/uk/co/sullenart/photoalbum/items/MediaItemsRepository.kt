package uk.co.sullenart.photoalbum.items

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import uk.co.sullenart.photoalbum.albums.Album
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

    suspend fun clear() {
        realm.write {
            delete(RealmItem::class)
        }
    }

    suspend fun setRotation(photo: PhotoItem, rotation: Rotation): PhotoItem {
        val newItem = photo.copy(rotation = rotation)
        realm.write {
            query<RealmItem>("id == $0", photo.id).first().find()?.copyFromItem(newItem)
        }
        Timber.d("Rotation set to $rotation for ${photo.id}")
        return newItem
    }

    suspend fun populateCache(
        album: Album,
    ) {
        // For each media item in Realm for this album...
        realm.query<RealmItem>("albumId == $0", album.id).find()
            .map { it.toMediaItem() }
            .filterNotNull()
            .forEach { item ->
                // If it's not in the cache then add it.
                if (!isInCache(item)) {
                    runBlocking {
                        addToCache(item)
                    }
                }
            }
    }

    suspend fun upsert(
        items: List<MediaItem>,
    ) {
        realm.write {
            items.forEach { item ->
                // Is there a current record for this item?
                val result = query<RealmItem>("id == $0", item.id).first().find()
                if (result == null) {
                    // No, create a new record.
                    copyToRealm(item.toRealmItem())
                    Timber.d("Media item not found, new record created [${item.id}]")
                } else {
                    // Yes, update its properties, Realm will update the persisted record once outside the "write" scope.
                    // The "item" has come from the remote API - retain properties like rotation that are local.
                    result.copyFromItem(item, except = setOf(result::rotation))
                    Timber.d("Media record updated [${item.id}]")
                }
            }
        }
    }

    suspend fun prune(
        keep: List<String>,
    ) {
        val forDeletion = realm.query<RealmItem>().find()
            .map { it.id }
            .filterNot { it in keep }

        realm.write {
            forDeletion.forEach {
                Timber.d("Deleting record [${it}]")
                query<RealmItem>("id == $0", it).first().find()?.let {
                    delete(it)
                }
            }
        }

        File(itemUtils.getMediaPath()).listFiles()?.forEach { file ->
            val id = file.name.replaceAfterLast('-', "").dropLast(1)
            if (id !in keep) {
                file.delete()
                Timber.d("Media file deleted [${file.name}]")
            }
        }
    }

    private fun isInCache(item: MediaItem): Boolean {
        val thumbExists = File(itemUtils.getThumbnailFilename(item)).exists()
        val detailExists = File(itemUtils.getDetailFilename(item)).exists()
        return thumbExists && detailExists
    }

    private suspend fun saveMediaFileWithRetry(
        sourceUrl: String,
        destinationPath: String,
    ): Boolean {
        var tries = 0
        while (true) {
            if (googlePhotos.saveMediaFile(sourceUrl, destinationPath)) {
                return true
            }
            if (++tries == 3) {
                Timber.w("Failed to save media file after re-tries")
                return false
            }
            delay(1000)
        }
    }

    private suspend fun addToCache(item: MediaItem) {
        if (saveMediaFileWithRetry(item.thumbnailUrl, itemUtils.getThumbnailFilename(item))) {
            Timber.d("Thumbnail downloaded to [${itemUtils.getThumbnailFilename(item)}]")
        }

        if (saveMediaFileWithRetry(item.detailUrl, itemUtils.getDetailFilename(item))) {
            Timber.d("Detail downloaded to [${itemUtils.getDetailFilename(item)}]")
        }
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