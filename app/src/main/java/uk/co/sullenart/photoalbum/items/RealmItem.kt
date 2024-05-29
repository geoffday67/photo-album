package uk.co.sullenart.photoalbum.items

import io.realm.kotlin.types.RealmObject
import org.threeten.bp.Instant
import timber.log.Timber
import kotlin.reflect.KProperty

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
    var mimeType: String = ""

    private var rotationName: String = Rotation.NONE.name
    var rotation: Rotation
        get() =
            try {
                Rotation.valueOf(rotationName)
            } catch (e: IllegalArgumentException) {
                Rotation.NONE
            }
        set(value) {
            rotationName = value.name
        }
}

fun RealmItem.copyFromItem(
    source: MediaItem,
    except: Set<KProperty<*>> = emptySet()
) {
    type = when (source) {
        is PhotoItem -> MediaType.PHOTO
        is VideoItem -> MediaType.VIDEO
    }
    if (this::albumId !in except) this.albumId = source.albumId
    if (this::url !in except) this.url = source.url
    if (this::creationTime !in except) this.creationTime = source.creationTime.toString()
    if (this::camera !in except) this.camera = source.camera
    if (this::mimeType !in except) this.mimeType = source.mimeType
    if (this::rotation !in except) this.rotation = source.rotation
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
        mimeType = this@toRealmItem.mimeType
        rotation = this@toRealmItem.rotation
    }

fun RealmItem.toMediaItem(): MediaItem? =
    when (this@toMediaItem.type) {
        MediaType.PHOTO -> PhotoItem(
            id = this@toMediaItem.id,
            albumId = this@toMediaItem.albumId,
            url = this@toMediaItem.url,
            creationTime = Instant.parse(this@toMediaItem.creationTime),
            camera = this@toMediaItem.camera,
            mimeType = this@toMediaItem.mimeType,
            rotation = this@toMediaItem.rotation
        )
        MediaType.VIDEO -> VideoItem(
            id = this@toMediaItem.id,
            albumId = this@toMediaItem.albumId,
            url = this@toMediaItem.url,
            creationTime = Instant.parse(this@toMediaItem.creationTime),
            camera = this@toMediaItem.camera,
            mimeType = this@toMediaItem.mimeType,
            rotation = this@toMediaItem.rotation
        )
        else -> {
            Timber.e("Unknown media type: ${this@toMediaItem.type}")
            null
        }
    }
