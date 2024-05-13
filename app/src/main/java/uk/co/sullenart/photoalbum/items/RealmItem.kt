package uk.co.sullenart.photoalbum.items

import io.realm.kotlin.types.RealmObject
import org.threeten.bp.Instant
import timber.log.Timber

internal enum class MediaType(
) {
    UNKNOWN,
    PHOTO,
    VIDEO,
}

class RealmItem : RealmObject {
    internal var type: MediaType
        get() =
            try {
                MediaType.valueOf(mediaName)
            } catch (e: IllegalArgumentException) {
                MediaType.UNKNOWN
            }
        set(value) {
            mediaName = value.name
        }
    private var mediaName: String = MediaType.UNKNOWN.name
    var id: String = ""
    var albumId: String = ""
    var url: String = ""
    var creationTime: String = ""
    var camera: String = ""
    var path: String = ""
}

fun RealmItem.copyFromItem(source: MediaItem) {
    type = when (source) {
        is PhotoItem -> MediaType.PHOTO
        is VideoItem -> MediaType.VIDEO
    }
    this.albumId = source.albumId
    this.url = source.url
    this.creationTime = source.creationTime.toString()
    this.camera = source.camera
    this.path = when (source) {
        is PhotoItem -> ""
        is VideoItem -> source.path
    }
}

fun MediaItem.toRealmItem() =
    RealmItem().apply {
        id = this@toRealmItem.id
        type = when (this@toRealmItem) {
            is PhotoItem -> MediaType.PHOTO
            is VideoItem -> MediaType.VIDEO
        }
        albumId = this@toRealmItem.albumId
        url = this@toRealmItem.url
        creationTime = this@toRealmItem.creationTime.toString()
        camera = this@toRealmItem.camera
        path = when (this@toRealmItem) {
            is PhotoItem -> ""
            is VideoItem -> this@toRealmItem.path
        }
    }

fun RealmItem.toMediaItem(): MediaItem? =
    when (this@toMediaItem.type) {
        MediaType.PHOTO -> PhotoItem(
            id = this@toMediaItem.id,
            albumId = this@toMediaItem.albumId,
            url = this@toMediaItem.url,
            creationTime = Instant.parse(this@toMediaItem.creationTime),
            camera = this@toMediaItem.camera,
        )
        MediaType.VIDEO -> VideoItem(
            id = this@toMediaItem.id,
            albumId = this@toMediaItem.albumId,
            url = this@toMediaItem.url,
            creationTime = Instant.parse(this@toMediaItem.creationTime),
            camera = this@toMediaItem.camera,
            path = this@toMediaItem.path,
        )
        else -> {
            Timber.e("Unknown media type: ${this@toMediaItem.type}")
            null
        }
    }
