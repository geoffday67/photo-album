package uk.co.sullenart.photoalbum.google

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


class GooglePhotos(
    private val interceptor: AuthInterceptor,
    private val context: Context,
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

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun saveMediaFile(
        sourceUrl: String,
        destinationPath: String,
    ) {
        val request = Request.Builder()
            .url(sourceUrl)
            .build()
        // TODO Re-use single client.
        val client = OkHttpClient.Builder()
            .build()
        val response = client.newCall(request).executeAsync()
        withContext(Dispatchers.IO) {
            response.body.byteStream().use { byteStream ->
                FileOutputStream(destinationPath).use { fileStream ->
                    byteStream.copyTo(fileStream)
                }
            }
        }
    }
}