package uk.co.sullenart.photoalbum.albums

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.copyFromRealm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class AlbumsRepository(
    private val realm: Realm,
) {
    val albumFlow: Flow<List<Album>>
        get() = realm.query<RealmAlbum>().asFlow().map {
            it.list
                .map { it.toAlbum() }
        }

    suspend fun clear() {
        realm.write {
            delete(RealmAlbum::class)
        }
    }

    suspend fun sync(albums: List<Album>) {
        realm.write {
            delete(RealmAlbum::class)
            albums.forEach {
                copyToRealm(it.toRealm())
            }
        }
    }

    suspend fun store(albums: List<Album>) {
        albums.forEach {
            store(it)
        }
    }

    suspend fun store(album: Album) {
        realm.write {
            copyToRealm(album.toRealm())
            Timber.i("Album ${album.title} written to Realm")
        }
    }

    suspend fun upsert(album: Album) {
        realm.write {
            // Is there a current record for this album?
            val result = query<RealmAlbum>("id == $0", album.id).first().find()
            if (result == null) {
                // No, create a new record.
                copyToRealm(album.toRealm())
                Timber.d("Album ${album.title} not found, creating new record")
            } else {
                // Yes, update its properties, Realm will update the persisted record once outside the "write" scope.
                result.copyFromAlbum(album)
                Timber.d("Album ${result.title} updated")
            }
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