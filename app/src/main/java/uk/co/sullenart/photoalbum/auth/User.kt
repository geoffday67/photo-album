package uk.co.sullenart.photoalbum.auth

data class User(
    val name: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String,
)
