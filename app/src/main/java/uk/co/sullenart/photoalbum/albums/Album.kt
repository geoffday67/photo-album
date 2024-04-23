package uk.co.sullenart.photoalbum.albums

import kotlinx.serialization.Serializable

@Serializable
data class Album(
    val id: String,
    val title: String,
    val itemCount: Int,
)