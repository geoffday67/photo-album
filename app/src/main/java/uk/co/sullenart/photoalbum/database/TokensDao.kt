package uk.co.sullenart.photoalbum.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import uk.co.sullenart.photoalbum.service.Tokens

@Dao
interface TokensDao {
    @Query("SELECT * FROM tokens LIMIT 1")
    suspend fun get(): Tokens

    @Insert
    suspend fun put(tokens: Tokens)

    @Query("DELETE FROM tokens")
    suspend fun clear()
}