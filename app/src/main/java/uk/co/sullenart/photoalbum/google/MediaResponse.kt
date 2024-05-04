package uk.co.sullenart.photoalbum.google

import kotlinx.serialization.Serializable
import uk.co.sullenart.photoalbum.photos.Photo

@Serializable
data class MediaResponse(
    val mediaItems: List<MediaItemResponse>?,
    val nextPageToken: String?,
) {
    @Serializable
    data class MediaItemResponse(
        val id: String?,
        val baseUrl: String?,
    )
}

fun MediaResponse.MediaItemResponse.toPhoto() =
    Photo(
        id = this.id.orEmpty(),
        url = this.baseUrl.orEmpty(),
    )