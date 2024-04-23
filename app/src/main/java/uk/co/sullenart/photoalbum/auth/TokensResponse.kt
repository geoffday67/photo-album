package uk.co.sullenart.photoalbum.auth

import kotlinx.serialization.Serializable

@Serializable
data class TokensResponse(
    val access_token: String? = null,
    val refresh_token: String? = null,
)

fun TokensResponse.toTokens() =
    Tokens(
        accessToken = this.access_token.orEmpty(),
        refreshToken = this.refresh_token.orEmpty(),
    )