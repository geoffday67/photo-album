package uk.co.sullenart.photoalbum.auth

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
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
    private val tokensRepository: TokensRepository,
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
            .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
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

    private suspend fun storeTokens(tokens: Tokens) {
        tokensRepository.store(tokens)
    }

    suspend fun exchangeCode(code: String) {
        val response = service.exchange(
            code = code,
            clientId = CLIENT_ID,
            clientSecret = CLIENT_SECRET,
            grantType = GRANT_TYPE,
            redirectUri = REDIRECT_URI,
        )
        val tokens = response.toTokens()
        Timber.i("Code exchanged for $tokens")
        storeTokens(tokens)
    }

    suspend fun refresh() {
        val tokens = tokensRepository.fetch()
        if (tokens == null) {
            Timber.w("No tokens found")
            return
        }

        val response = service.refresh(
            clientId = CLIENT_ID,
            clientSecret = CLIENT_SECRET,
            refreshToken = tokens.refreshToken,
        )

        val newTokens = tokens.copy(accessToken = response.toTokens().accessToken)
        Timber.i("Tokens refreshed $newTokens")
        storeTokens(newTokens)
    }

    companion object {
        private const val GRANT_TYPE = "authorization_code"
        private const val REDIRECT_URI = "https://www.sullenart.co.uk/photoalbum/auth"
    }
}