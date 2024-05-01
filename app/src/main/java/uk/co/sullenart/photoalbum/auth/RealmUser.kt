package uk.co.sullenart.photoalbum.auth

import io.realm.kotlin.types.RealmObject

class RealmUser : RealmObject {
    var name: String = ""
    var email: String = ""
    var accessToken: String = ""
    var refreshToken: String = ""
}

fun RealmUser.toUser() =
    User(
        name = this.name,
        email = this.email,
        accessToken = this.accessToken,
        refreshToken = this.refreshToken,
    )

fun User.toRealm() =
    RealmUser().apply {
        name = this@toRealm.name
        email = this@toRealm.email
        accessToken = this@toRealm.accessToken
        refreshToken = this@toRealm.refreshToken
    }