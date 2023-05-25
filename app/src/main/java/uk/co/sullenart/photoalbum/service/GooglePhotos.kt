package uk.co.sullenart.photoalbum.service

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import uk.co.sullenart.photoalbum.Album

class GooglePhotos {
    @Serializable
    private data class AlbumsResponse(
        val albums: List<Album>
    )

    private interface Service {
        @GET("/v1/albums")
        suspend fun albums(): AlbumsResponse
    }

    private val service: Service by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
            .build()
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://photoslibrary.googleapis.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(Service::class.java)
    }

    suspend fun getAlbums(): List<Album> =
        service.albums()
            .albums
}