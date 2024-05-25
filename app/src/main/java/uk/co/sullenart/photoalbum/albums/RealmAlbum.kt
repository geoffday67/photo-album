package uk.co.sullenart.photoalbum.albums

import io.realm.kotlin.types.RealmObject
import uk.co.sullenart.photoalbum.albums.Album.SortOrder

class RealmAlbum : RealmObject {
    var id: String = ""
    var title: String = ""
    var itemCount: Int = 0
    internal var sortOrder: SortOrder
        get() =
            try {
                SortOrder.valueOf(sortOrderName)
            } catch (e: IllegalArgumentException) {
                SortOrder.UNKNOWN
            }
        set(value) {
            sortOrderName = value.name
        }
    private var sortOrderName: String = SortOrder.UNKNOWN.name
}

fun RealmAlbum.copyFromAlbum(source: Album) {
    this.id = source.id
    this.title = source.title
    this.itemCount = source.itemCount
    this.sortOrder = source.sortOrder
}

fun Album.toRealmAlbum(): RealmAlbum =
    RealmAlbum().apply {
        id = this@toRealmAlbum.id
        title = this@toRealmAlbum.title
        itemCount = this@toRealmAlbum.itemCount
        sortOrder = this@toRealmAlbum.sortOrder
    }

fun RealmAlbum.toAlbum(): Album =
    Album(
        id = this@toAlbum.id,
        title = this@toAlbum.title,
        itemCount = this@toAlbum.itemCount,
        sortOrder = this@toAlbum.sortOrder,
    )