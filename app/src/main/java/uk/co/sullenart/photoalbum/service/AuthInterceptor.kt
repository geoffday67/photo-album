package uk.co.sullenart.photoalbum.service

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import uk.co.sullenart.photoalbum.MainApplication
import uk.co.sullenart.photoalbum.database.Database
import java.io.IOException

class AuthInterceptor(
    private val auth: Auth,
    private val database: Database,
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var requestBuilder = chain.request().newBuilder()

        runBlocking {
            requestBuilder.addHeader("Authorization", "Bearer ${database.tokensDao().get().accessToken}")
        }

        // Try the call once.
        var response = chain.proceed(requestBuilder.build())

        // If it failed then refresh the token and try again.
        if (response.code == 401) {
            response.close()
            requestBuilder = chain.request().newBuilder()

            // We're already in a background thread so safe (and recommended) to do a simple block here.
            runBlocking {
                auth.refresh()
                requestBuilder.addHeader("Authorization", "Bearer ${database.tokensDao().get().accessToken}")
            }
            response = chain.proceed(requestBuilder.build())
        }

        return response
    }
}
