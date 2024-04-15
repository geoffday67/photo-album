package uk.co.sullenart.photoalbum.albums

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.copyFromRealm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.sullenart.photoalbum.Album
import uk.co.sullenart.photoalbum.realm.RealmAlbum

class AlbumsRepository(
    private val realm: Realm,
) {
    val albumFlow: Flow<List<Album>>
        get() = realm.query<RealmAlbum>().asFlow().map {
            it.list.copyFromRealm().map {
                Album(
                    title = it.title,
                )
            }
        }

    suspend fun test(title: String) {
        realm.write {
            val album = RealmAlbum().apply {
                this.title = title
            }
            copyToRealm(album)
        }
    }
}