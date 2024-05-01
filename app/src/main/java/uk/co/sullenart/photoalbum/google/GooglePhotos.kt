package uk.co.sullenart.photoalbum.google

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.internal.throwArrayMissingFieldException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import timber.log.Timber
import uk.co.sullenart.photoalbum.albums.Album
import uk.co.sullenart.photoalbum.auth.AuthInterceptor
import uk.co.sullenart.photoalbum.photos.Photo

class GooglePhotos(
    private val interceptor: AuthInterceptor,
) {
    private interface Service {
        @GET("/v1/albums")
        suspend fun albums(): AlbumsResponse

        @GET("/v1/sharedAlbums")
        suspend fun sharedAlbums(): SharedAlbumsResponse

        @POST("/v1/mediaItems:search")
        suspend fun mediaSearch(@Body mediaRequest: MediaRequest): MediaResponse
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val service: Service by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BASIC) })
            .build()
        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        Retrofit.Builder()
            .baseUrl("https://photoslibrary.googleapis.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(Service::class.java)
    }

    suspend fun getAlbums(): List<Album> =
        try {
            service.albums()
                .albums.orEmpty()
                .map { it.toAlbum() }
        } catch (ignore: Exception) {
            emptyList()
        }

    suspend fun getSharedAlbums(): List<Album>? =
        try {
            service.sharedAlbums()
                .sharedAlbums.orEmpty()
                .map { it.toAlbum() }
                //.filterNot { it.title == "Photos lounge" }
        } catch (error: Exception) {
            Timber.e(error)
            null
        }

    suspend fun getPhotosForAlbum(album: Album): List<Photo>? =
        try {
            val request = MediaRequest(
                albumId = album.id,
            )
            service.mediaSearch(request)
                .mediaItems.orEmpty()
                .map { it.toPhoto() }
        } catch (error: Exception) {
            Timber.e(error)
            null
        }
}