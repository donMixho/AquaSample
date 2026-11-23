package com.aldemar.aquasample.data.remote.backend

import com.aldemar.aquasample.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BackendClient {
    fun create(baseUrl: String, idToken: String? = null): AquaSampleBackendApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val url = chain.request().url
                val localDebug = BuildConfig.DEBUG && url.host in setOf("localhost", "127.0.0.1", "10.0.2.2")
                if (idToken != null && !url.isHttps && !localDebug)
                    throw java.io.IOException("El backend requiere HTTPS para enviar la sesión.")
                val request = chain.request().newBuilder()
                if (idToken != null) request.header("Authorization", "Bearer $idToken")
                chain.proceed(request.build())
            }
            .connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
                )
            }.build()
        return Retrofit.Builder().baseUrl(baseUrl).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(AquaSampleBackendApi::class.java)
    }
}
