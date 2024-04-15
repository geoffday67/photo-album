package uk.co.sullenart.photoalbum.service

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
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
    private val tokens = Tokens()

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
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://oauth2.googleapis.com/")
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
        val tokens = Tokens(
            accessToken = response.access_token.orEmpty(),
            refreshToken = response.refresh_token.orEmpty(),
        )
        Timber.i("Code exchanged for $tokens")
        storeTokens(tokens)
    }

    suspend fun refresh() {
        val refreshToken = tokensRepository.getRefresh()
        if (refreshToken == null) {
            Timber.w("No refresh token found")
            return
        }
        val response = service.refresh(
            clientId = CLIENT_ID,
            clientSecret = CLIENT_SECRET,
            refreshToken = refreshToken,
        )
        val tokens = Tokens(
            accessToken = response.access_token.orEmpty(),
            refreshToken = response.refresh_token.orEmpty(),
        )
        Timber.i("Tokens refreshed $tokens")
        storeTokens(tokens)
    }

    companion object {
        private const val GRANT_TYPE = "authorization_code"
        private const val REDIRECT_URI = "https://www.sullenart.co.uk/photoalbum/auth"
    }
}