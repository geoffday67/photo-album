package uk.co.sullenart.photoalbum.items

import org.threeten.bp.Instant

sealed class MediaItem(
) {
    abstract val id: String
    abstract val albumId: String
    abstract val url: String
    abstract val creationTime: Instant
    abstract val camera: String

    val usableUrl: String
        get() = "${url}=w4096-h4096"
}

data class PhotoItem(
    override val id: String,
    override val albumId: String,
    override val url: String,
    override val creationTime: Instant,
    override val camera: String,
) : MediaItem() {
    companion object {
        val EMPTY
            get() = PhotoItem(
                id = "",
                albumId = "",
                url = "",
                creationTime = Instant.MIN,
                camera = "",
            )
    }
}

data class VideoItem(
    override val id: String,
    override val albumId: String,
    override val url: String,
    override val creationTime: Instant,
    override val camera: String,
    var path: String,
) : MediaItem() {
    val downloadUrl: String
        get() = "${url}=dv"
}
