package uk.co.sullenart.photoalbum.service

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class Auth {
    private interface Service {
        @POST("/token")
        @FormUrlEncoded
        suspend fun exchangeCode(
            @Field("code") code: String,
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field("grant_type") grantType: String,
            @Field("redirect_uri") redirectUri: String,
        ): ExchangeResponse
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

    private fun storeTokens(tokens: Tokens) {
        AuthInterceptor.tokens = tokens
    }

    suspend fun exchangeCode(code: String) {
        val response = service.exchangeCode(
            code = code,
            clientId = CLIENT_ID,
            clientSecret = CLIENT_SECRET,
            grantType = GRANT_TYPE,
            redirectUri = REDIRECT_URI,
        )
        storeTokens(
            Tokens(
                accessToken = response.access_token.orEmpty(),
                refreshToken = response.refresh_token.orEmpty(),
            )
        )
    }

    companion object {
        private const val CLIENT_ID = "623200176730-43pm5mfljjfj5unb63m75tdhhlt2jcdt.apps.googleusercontent.com"
        private const val CLIENT_SECRET = "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"
        private const val GRANT_TYPE = "authorization_code"
        private const val REDIRECT_URI = "https://www.sullenart.co.uk/photoalbum/auth"
    }
}