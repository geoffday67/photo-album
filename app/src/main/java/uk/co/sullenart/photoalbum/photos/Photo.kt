package uk.co.sullenart.photoalbum.photos

data class Photo(
    val id: String,
    val albumId: String? = null,
    val url: String,
) {
    val usableUrl: String
        get() = "${url}=w4096-h4096"
}
