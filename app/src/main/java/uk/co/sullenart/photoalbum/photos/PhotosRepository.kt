package uk.co.sullenart.photoalbum.photos

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class PhotosRepository(
    private val realm: Realm,
    ) {
    val photoFlow: Flow<List<Photo>>
        get() = realm.query<RealmPhoto>("albumId == $0", "AGMfN6Pcs2aS42pFDBuGOuZ7IcSGKWQAll6oFUA-KYa0xkCSaxNTb3kwMd1ntivM95Ojs3QYJMme").asFlow().map {
            it.list
                .map { it.toPhoto() }
        }

    suspend fun clear() {
        realm.write {
            delete(RealmPhoto::class)
        }
    }

    suspend fun sync(photos: List<Photo>) {
        realm.write {
            delete(RealmPhoto::class)
            photos.forEach {
                copyToRealm(it.toRealm())
            }
        }
    }

    suspend fun upsert(photo: Photo) {
        realm.write {
            // Is there a current record for this photo?
            val result = query<RealmPhoto>("id == $0", photo.id).first().find()
            if (result == null) {
                // No, create a new record.
                copyToRealm(photo.toRealm())
                Timber.d("Photo not found, creating new record")
            } else {
                // Yes, update its properties, Realm will update the persisted record once outside the "write" scope.
                result.copyFromPhoto(photo)
                Timber.d("Photo updated")
            }
        }
    }
}