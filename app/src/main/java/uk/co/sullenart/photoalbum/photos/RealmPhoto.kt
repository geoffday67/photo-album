package uk.co.sullenart.photoalbum.photos

import io.realm.kotlin.types.RealmObject
import uk.co.sullenart.photoalbum.albums.Album
import uk.co.sullenart.photoalbum.albums.RealmAlbum
import java.time.Instant

class RealmPhoto : RealmObject {
    var id: String = ""
    var albumId: String? = null
    var url: String = ""
    var creationTime: String = ""
    var camera: String = ""
}

fun RealmPhoto.copyFromPhoto(source: Photo) {
    this.albumId = source.albumId
    this.url = source.url
    this.creationTime = source.creationTime.toString()
    this.camera = source.camera
}

fun Photo.toRealm() =
    RealmPhoto().apply {
        id = this@toRealm.id
        albumId = this@toRealm.albumId
        url = this@toRealm.url
        creationTime = this@toRealm.creationTime.toString()
        camera = this@toRealm.camera
    }

fun RealmPhoto.toPhoto() =
    Photo(
        id = this@toPhoto.id,
        albumId = this@toPhoto.albumId,
        url = this@toPhoto.url,
        creationTime = Instant.parse(this@toPhoto.creationTime),
        camera = this@toPhoto.camera,
    )