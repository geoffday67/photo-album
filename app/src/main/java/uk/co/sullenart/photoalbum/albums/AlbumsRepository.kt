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

    suspend fun sync(albums: List<Album>) {
        realm.write {
            delete(RealmAlbum::class)
            albums.forEach {
                copyToRealm(it.toRealm())
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