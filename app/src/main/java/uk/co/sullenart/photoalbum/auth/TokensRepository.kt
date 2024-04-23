package uk.co.sullenart.photoalbum.auth

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.copyFromRealm
import io.realm.kotlin.ext.query
import timber.log.Timber

class TokensRepository(
    private val realm: Realm,
) {
    fun getAccess(): String? {
        val result = realm.query<RealmTokens>().first().find()?.toTokens()?.accessToken
        Timber.i("Access token read from Realm")
        return result
    }

    fun getRefresh(): String? {
        val result = realm.query<RealmTokens>().first().find()?.toTokens()?.refreshToken
        Timber.i("Refresh token read from Realm")
        return result
    }

    fun fetch(): Tokens? {
        val result = realm.query<RealmTokens>().first().find()?.toTokens()
        Timber.i("Tokens read from Realm")
        return result
    }

    suspend fun store(tokens: Tokens) {
        realm.write {
            delete(RealmTokens::class)
            copyToRealm(tokens.toRealm())
            Timber.i("Tokens written to Realm")
        }
    }
}