package uk.co.sullenart.photoalbum.items

import org.threeten.bp.Instant

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
}

data class PhotoItem(
    override val id: String,
    override val albumId: String,
    override val url: String,
    override val creationTime: Instant,
    override val camera: String,
    override val mimeType: String,
    ) : MediaItem() {
    override val thumbnailUrl: String
        get() = "${url}=w600-h600"

    override val detailUrl: String
        get() = "${url}=w4000-h4000"

    companion object {
        val EMPTY
            get() = PhotoItem(
                id = "",
                albumId = "",
                url = "",
                creationTime = Instant.MIN,
                camera = "",
                mimeType = "",
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
) : MediaItem() {
    override val thumbnailUrl: String
        get() = "${url}=w600-h600"

    override val detailUrl: String
        get() = "${url}=dv"
}
