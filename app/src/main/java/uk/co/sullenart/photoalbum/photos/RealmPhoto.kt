package uk.co.sullenart.photoalbum.photos

import io.realm.kotlin.types.RealmObject
import uk.co.sullenart.photoalbum.albums.Album
import uk.co.sullenart.photoalbum.albums.RealmAlbum

class RealmPhoto : RealmObject {
    var id: String = ""
    var albumId: String? = null
    var url: String = ""
}

fun RealmPhoto.copyFromPhoto(source: Photo) {
    this.albumId = source.albumId
    this.url = source.url
}

fun Photo.toRealm() =
    RealmPhoto().apply {
        id = this@toRealm.id
        albumId = this@toRealm.albumId
        url = this@toRealm.url
    }

fun RealmPhoto.toPhoto() =
    Photo(
        id = this@toPhoto.id,
        albumId = this@toPhoto.albumId,
        url = this@toPhoto.url,
    )