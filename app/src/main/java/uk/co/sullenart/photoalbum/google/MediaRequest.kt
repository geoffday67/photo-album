package uk.co.sullenart.photoalbum.google

import kotlinx.serialization.Serializable

@Serializable
data class MediaRequest(
    val albumId: String,
    val pageSize: Int,
    val pageToken: String?,
)
