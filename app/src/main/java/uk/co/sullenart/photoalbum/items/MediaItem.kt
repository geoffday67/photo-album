package uk.co.sullenart.photoalbum.items

import org.threeten.bp.Instant

enum class Rotation {
    NONE, LEFT, RIGHT, INVERT,
}

sealed class MediaItem(
) {
    abstract val id: String
    abstract val albumId: String
    abstract val url: String
    abstract val creationTime: Instant
    abstract val camera: String
    abstract val mimeType: String
    abstract val thumbnailUrl: String
    abstract val detailUrl: String
    abstract val rotation: Rotation
}

data class PhotoItem(
    override val id: String,
    override val albumId: String,
    override val url: String,
    override val creationTime: Instant,
    override val camera: String,
    override val mimeType: String,
    override val rotation: Rotation
) : MediaItem() {
    override val thumbnailUrl: String
        get() = "${url}=w400-h400"

    override val detailUrl: String
        get() = "${url}=w10000-h10000"

    companion object {
        val EMPTY
            get() = PhotoItem(
                id = "",
                albumId = "",
                url = "",
                creationTime = Instant.MIN,
                camera = "",
                mimeType = "",
                rotation = Rotation.NONE,
            )
    }
}

data class VideoItem(
    override val id: String,
    override val albumId: String,
    override val url: String,
    override val creationTime: Instant,
    override val camera: String,
    override val mimeType: String,
    override val rotation: Rotation,
) : MediaItem() {
    override val thumbnailUrl: String
        get() = "${url}=w400-h400"

    override val detailUrl: String
        get() = "${url}=dv"
}
