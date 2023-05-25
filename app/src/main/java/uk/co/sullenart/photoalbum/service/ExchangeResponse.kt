package uk.co.sullenart.photoalbum.service

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeResponse(
    val access_token: String? = null,
    val refresh_token: String? = null,
)
