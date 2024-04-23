package uk.co.sullenart.photoalbum.albums

import io.realm.kotlin.types.RealmObject

class RealmAlbum : RealmObject {
    var id: String = ""
    var title: String = ""
    var itemCount: Int = 0
}

fun RealmAlbum.copyFromAlbum(source: Album) {
    this.title = source.title
    this.itemCount = source.itemCount
}

fun Album.toRealm() =
    RealmAlbum().apply {
        id = this@toRealm.id
        title = this@toRealm.title
        itemCount = this@toRealm.itemCount
    }

fun RealmAlbum.toAlbum() =
    Album(
        id = this@toAlbum.id,
        title = this@toAlbum.title,
        itemCount = this@toAlbum.itemCount,
    )