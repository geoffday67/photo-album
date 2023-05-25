package uk.co.sullenart.photoalbum.service

import okhttp3.Interceptor
import okhttp3.Response

object AuthInterceptor : Interceptor {
    var tokens = Tokens()

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${tokens.accessToken}")
            .build();
        return chain.proceed(newRequest)
    }
}