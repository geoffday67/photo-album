package uk.co.sullenart.photoalbum.photos

data class Photo(
    val id: String,
    val albumId: String? = null,
    val url: String,
)
