package uk.co.sullenart.photoalbum.google

import kotlinx.serialization.Serializable
import org.threeten.bp.Instant
import uk.co.sullenart.photoalbum.albums.Album
import uk.co.sullenart.photoalbum.items.MediaItem
import uk.co.sullenart.photoalbum.items.PhotoItem
import uk.co.sullenart.photoalbum.items.Rotation
import uk.co.sullenart.photoalbum.items.VideoItem

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
        val mimeType: String?,
    )

    @Serializable
    data class Metadata(
        val creationTime: String?,
        val photo: PhotoData?,
        val video: VideoData?,
    )

    @Serializable
    data class PhotoData(
        val cameraMake: String?,
        val cameraModel: String?,
    )

    @Serializable
    data class VideoData(
        val cameraMake: String?,
        val cameraModel: String?,
    )
}

fun MediaResponse.MediaItemResponse.toMediaItem(album: Album): MediaItem {
    when {
        this.mediaMetadata?.photo != null -> {
            var camera = this.mediaMetadata.photo.cameraMake.orEmpty()
            if (camera.isNotBlank()) {
                camera += " "
            }
            camera += this.mediaMetadata.photo.cameraModel.orEmpty()

            return PhotoItem(
                id = this.id.orEmpty(),
                albumId = album.id,
                url = this.baseUrl.orEmpty(),
                creationTime = Instant.parse(this.mediaMetadata.creationTime.orEmpty()),
                camera = camera,
                mimeType = this.mimeType.orEmpty(),
                rotation = Rotation.NONE,
            )
        }
        this.mediaMetadata?.video != null -> {
            var camera = this.mediaMetadata.video.cameraMake.orEmpty()
            if (camera.isNotBlank()) {
                camera += " "
            }
            camera += this.mediaMetadata.video.cameraModel.orEmpty()

            return VideoItem(
                id = this.id.orEmpty(),
                albumId = album.id,
                url = this.baseUrl.orEmpty(),
                creationTime = Instant.parse(this.mediaMetadata.creationTime.orEmpty()),
                camera = camera,
                mimeType = this.mimeType.orEmpty(),
                rotation = Rotation.NONE,
            )
        }
        else -> return PhotoItem.EMPTY
    }
}