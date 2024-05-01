package uk.co.sullenart.photoalbum.auth

import kotlinx.serialization.Serializable

@Serializable
data class TokensResponse(
    val access_token: String? = null,
    val refresh_token: String? = null,
)