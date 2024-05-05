package uk.co.sullenart.photoalbum.photos

import java.time.Instant

data class Photo(
    val id: String,
    val albumId: String? = null,
    val url: String,
    val creationTime: Instant,
    val camera: String,
) {
    val usableUrl: String
        get() = "${url}=w4096-h4096"
}
