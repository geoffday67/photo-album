package uk.co.sullenart.photoalbum.albums

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import uk.co.sullenart.photoalbum.items.RealmItem
import uk.co.sullenart.photoalbum.items.copyFromItem
import uk.co.sullenart.photoalbum.items.toMediaItem
import uk.co.sullenart.photoalbum.items.toRealmItem

class AlbumsRepository(
    private val realm: Realm,
) {
    fun getAlbums(): List<Album> =
        realm.query<RealmAlbum>().find().map { it.toAlbum() }

    val albumFlow: Flow<List<Album>>
        get() = realm.query<RealmAlbum>().asFlow().map {
            it.list
                .map { it.toAlbum() }
        }

    suspend fun sync(albums: List<Album>) {
        realm.write {
            albums.forEach { album ->
                val result = query<RealmAlbum>("id == $0", album.id).first().find()
                if (result == null) {
                    Timber.d("Album not found, creating new record [${album.id}]")
                    copyToRealm(album.toRealmAlbum())
                } else {
                    Timber.d("Updating album record [${album.id}]")
                    result.copyFromAlbum(album)
                }
            }

            val newIds = albums.map { it.id }
            query<RealmAlbum>().find().forEach { existing ->
                if (!newIds.contains(existing.id)) {
                    Timber.d("Deleting existing album record [${existing.id}]")
                    delete(existing)
                }
            }
        }
    }

    suspend fun setSortOrder(album: Album, sortOrder: Album.SortOrder): Album {
        val newAlbum = album.copy(sortOrder = sortOrder)
        realm.write {
            query<RealmAlbum>("id == $0", album.id).first().find()?.copyFromAlbum(newAlbum)
        }
        return newAlbum
    }

    suspend fun clear() {
        realm.write {
            delete(RealmAlbum::class)
        }
    }

    fun getAlbum(id: String): Album? {
        val result = realm.query<RealmAlbum>("id == $0", id)
            .first().find()?.toAlbum()
        if (result == null) {
            Timber.d("Album $id not found")
        } else {
            Timber.d("Album ${result.title} found for $id")
        }
        return result
    }
}