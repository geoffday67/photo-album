package uk.co.sullenart.photoalbum.auth

import io.realm.kotlin.types.RealmObject

class RealmTokens : RealmObject {
    var accessToken: String = ""
    var refreshToken: String = ""
}

fun RealmTokens.toTokens() =
    Tokens(
        accessToken = this.accessToken,
        refreshToken = this.refreshToken,
    )

fun Tokens.toRealm() =
    RealmTokens().apply {
        accessToken = this@toRealm.accessToken
        refreshToken = this@toRealm.refreshToken
    }