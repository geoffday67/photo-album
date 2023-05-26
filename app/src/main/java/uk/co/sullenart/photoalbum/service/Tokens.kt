package uk.co.sullenart.photoalbum.service

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tokens")
data class Tokens(
    @PrimaryKey val accessToken: String = "",
    val refreshToken: String = "",
)
