package uk.co.sullenart.photoalbum.auth

data class Tokens(
    val accessToken: String = "",
    val refreshToken: String = "",
)
