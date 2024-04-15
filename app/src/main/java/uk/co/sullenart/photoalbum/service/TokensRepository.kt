package uk.co.sullenart.photoalbum.service

class TokensRepository(
    ) {
    suspend fun getAccess(): String? =
        ""

    suspend fun getRefresh(): String? =
        ""

    suspend fun store(tokens: Tokens) {
    }
}