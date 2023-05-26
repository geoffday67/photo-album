package uk.co.sullenart.photoalbum.service

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import uk.co.sullenart.photoalbum.MainApplication
import uk.co.sullenart.photoalbum.database.Database

class Auth(
    private val database: Database,
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
        database.tokensDao().put(tokens)
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
        Log.d("GD", "Code exchanged for tokens $tokens")
        storeTokens(tokens)
    }

    suspend fun refresh() {
        val response = service.refresh(
            clientId = CLIENT_ID,
            clientSecret = CLIENT_SECRET,
            refreshToken = tokens.refreshToken,
        )
        val tokens = Tokens(
            accessToken = response.access_token.orEmpty(),
            refreshToken = response.refresh_token.orEmpty(),
        )
        Log.d("GD", "Tokens refreshed $tokens")
        storeTokens(tokens)
    }

    companion object {
        private const val CLIENT_ID = "623200176730-43pm5mfljjfj5unb63m75tdhhlt2jcdt.apps.googleusercontent.com"
        private const val CLIENT_SECRET = "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"
        private const val GRANT_TYPE = "authorization_code"
        private const val REDIRECT_URI = "https://www.sullenart.co.uk/photoalbum/auth"
    }
}