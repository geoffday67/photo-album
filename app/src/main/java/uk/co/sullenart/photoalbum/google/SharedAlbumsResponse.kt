package uk.co.sullenart.photoalbum.google

import kotlinx.serialization.Serializable
import uk.co.sullenart.photoalbum.albums.Album
import uk.co.sullenart.photoalbum.albums.Album.SortOrder

@Serializable
data class SharedAlbumsResponse(
    val sharedAlbums: List<AlbumResponse>?,
) {
    @Serializable
    data class AlbumResponse(
        val id: String?,
        val title: String?,
        val mediaItemsCount: String?,
    )
}

fun SharedAlbumsResponse.AlbumResponse.toAlbum() =
    Album(
        id = this.id.orEmpty(),
        title = this.title.orEmpty(),
        itemCount = try {
            this.mediaItemsCount?.toInt() ?: 0
        } catch (ignore: Exception) {
            0
        },
        sortOrder = SortOrder.UNKNOWN
    )