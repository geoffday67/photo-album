package uk.co.sullenart.photoalbum.albums

import kotlinx.serialization.Serializable

@Serializable
data class Album(
    val id: String,
    val title: String,
    val itemCount: Int,
    val sortOrder: SortOrder,
) {
    enum class SortOrder {
        UNKNOWN, OLDEST_FIRST, NEWEST_FIRST,
    }

    companion object {
        val EMPTY
            get() =
                Album(
                    id = "",
                    title = "",
                    itemCount = 0,
                    sortOrder = SortOrder.UNKNOWN,
                )
    }
}