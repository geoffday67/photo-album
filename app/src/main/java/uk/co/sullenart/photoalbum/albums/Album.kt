package uk.co.sullenart.photoalbum.albums

import kotlinx.serialization.Serializable

@Serializable
data class Album(
    val id: String,
    val title: String,
    val itemCount: Int,
) {
    companion object {
        val EMPTY
            get() =
                Album(
                    id = "",
                    title = "",
                    itemCount = 0,
                )
    }
}