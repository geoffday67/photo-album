package uk.co.sullenart.photoalbum.auth

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(
    private val realm: Realm,
) {
    val userFlow: Flow<User?>
        get() = realm.query<RealmUser>().first().asFlow()
            .map { it.obj?.toUser() }

    fun getAccess(): String? =
        realm.query<RealmUser>().first().find()?.toUser()?.accessToken

    fun getRefresh(): String? =
        realm.query<RealmUser>().first().find()?.toUser()?.refreshToken

    fun fetch(): User? =
        realm.query<RealmUser>().first().find()?.toUser()

    suspend fun delete() {
        realm.write {
            delete(RealmUser::class)
        }
    }

    suspend fun store(user: User) {
        realm.write {
            delete(RealmUser::class)
            copyToRealm(user.toRealm())
        }
    }
}