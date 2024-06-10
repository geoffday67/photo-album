package uk.co.sullenart.photoalbum.google

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import timber.log.Timber
import uk.co.sullenart.photoalbum.albums.Album
import uk.co.sullenart.photoalbum.auth.AuthInterceptor
import uk.co.sullenart.photoalbum.items.MediaItem
import java.io.FileOutputStream

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
            .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
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
        } catch (error: Exception) {
            Timber.e(error)
            null
        }

    suspend fun getMediaForAlbum(album: Album): List<MediaItem> {
        val result: MutableList<MediaItem> = mutableListOf()
        var nextPageToken: String? = null

        try {
            do {
                val request = MediaRequest(
                    albumId = album.id,
                    pageSize = 100,
                    pageToken = nextPageToken,
                )
                val response = service.mediaSearch(request)
                result.addAll(response
                    .mediaItems.orEmpty()
                    .map { it.toMediaItem(album) }
                )
                nextPageToken = response.nextPageToken
            } while (nextPageToken != null)
        } catch (error: Exception) {
            Timber.e(error)
        }

        return result
    }

    private val saveClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .build()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun saveMediaFile(
        sourceUrl: String,
        destinationPath: String,
    ): Boolean =
        try {
            val request = Request.Builder()
                .url(sourceUrl)
                .build()
            val response = saveClient.newCall(request).executeAsync()
            if (!response.isSuccessful) {
                response.close()
                throw Exception("Error getting media file, code ${response.code}")
            }
            withContext(Dispatchers.IO) {
                response.body.byteStream().use { byteStream ->
                    FileOutputStream(destinationPath).use { fileStream ->
                        byteStream.copyTo(fileStream)
                    }
                }
            }
            true
        } catch (error: Exception) {
            Timber.e(error.message)
            false
        }
}