package uk.co.sullenart.photoalbum.auth

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.realm.kotlin.ext.query
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import timber.log.Timber
import uk.co.sullenart.photoalbum.CLIENT_ID
import uk.co.sullenart.photoalbum.CLIENT_SECRET

class Auth(
    private val userRepository: UserRepository,
) {
    private interface Service {
        @POST("/token")
        @FormUrlEncoded
        suspend fun exchange(
            @Field("code") code: String,
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field("grant_type") grantType: String,
            @Field("redirect_uri") redirectUri: String,
        ): TokensResponse

        @POST("/token")
        @FormUrlEncoded
        suspend fun refresh(
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field("refresh_token") refreshToken: String,
            @Field("grant_type") grantType: String = "refresh_token",
        ): TokensResponse
    }

    private val service: Service by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BASIC) })
            .build()
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://oauth2.googleapis.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(Service::class.java)
    }

    val isSignedIn: Boolean
        get() = userRepository.fetch() != null

    private suspend fun storeUser(user: User) {
        userRepository.store(user)
    }

    suspend fun exchangeCode(account: GoogleSignInAccount) {
        try {
            val response = service.exchange(
                code = account.serverAuthCode.orEmpty(),
                clientId = CLIENT_ID,
                clientSecret = CLIENT_SECRET,
                grantType = GRANT_TYPE,
                redirectUri = REDIRECT_URI,
            )
            val user = User(
                name = account.displayName.orEmpty(),
                email = account.email.orEmpty(),
                accessToken = response.access_token.orEmpty(),
                refreshToken = response.refresh_token.orEmpty(),
            )
            Timber.i("Code exchanged for tokens for ${user.name}")
            storeUser(user)
        } catch (e: Exception) {
            Timber.e("Error exchanging code for tokens: ${e.message}")
        }
    }

    suspend fun refresh() {
        val user = userRepository.fetch()
        if (user == null) {
            Timber.w("No signed-in user found")
            return
        }

        val response = service.refresh(
            clientId = CLIENT_ID,
            clientSecret = CLIENT_SECRET,
            refreshToken = user.refreshToken,
        )

        val newUser = user.copy(accessToken = response.access_token.orEmpty())
        Timber.i("Tokens refreshed for ${user.name}")
        storeUser(newUser)
    }

    suspend fun signOut() {
        userRepository.delete()
    }

    companion object {
        private const val GRANT_TYPE = "authorization_code"
        private const val REDIRECT_URI = "https://www.sullenart.co.uk/photoalbum/auth"
    }
}