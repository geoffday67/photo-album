package uk.co.sullenart.photoalbum.google

import kotlinx.serialization.Serializable
import uk.co.sullenart.photoalbum.photos.Photo
import java.time.Instant

@Serializable
data class MediaResponse(
    val mediaItems: List<MediaItemResponse>?,
    val nextPageToken: String?,
) {
    @Serializable
    data class MediaItemResponse(
        val id: String?,
        val baseUrl: String?,
        val mediaMetadata: Metadata?,
    )

    @Serializable
    data class Metadata(
        val creationTime: String?,
        val photo: PhotoData?,
    )

    @Serializable
    data class PhotoData(
        val cameraMake: String?,
        val cameraModel: String?,
    )
}

fun MediaResponse.MediaItemResponse.toPhoto(): Photo {
    var camera = this.mediaMetadata?.photo?.cameraMake.orEmpty()
    if (camera.isNotBlank()) {
        camera += " "
    }
    camera += this.mediaMetadata?.photo?.cameraModel.orEmpty()

    return Photo(
        id = this.id.orEmpty(),
        url = this.baseUrl.orEmpty(),
        creationTime = Instant.parse(this.mediaMetadata?.creationTime.orEmpty()),
        camera = camera,
    )
}