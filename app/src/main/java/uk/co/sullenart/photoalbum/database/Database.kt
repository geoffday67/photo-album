package uk.co.sullenart.photoalbum.database

import androidx.room.RoomDatabase
import uk.co.sullenart.photoalbum.service.Tokens

@androidx.room.Database(entities = [Tokens::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun tokensDao(): TokensDao
}
