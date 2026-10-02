package com.hocheol.unsplash.data

import com.hocheol.unsplash.BuildConfig
import com.hocheol.unsplash.data.models.PhotoResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object Repository {

    suspend fun getRandomPhotos(query: String?): List<PhotoResponse>? =
        unsplashApiService.getRandomPhotos(query).body()

    private val unsplashApiService: UnsplashApiService by lazy {
        require(BuildConfig.UNSPLASH_PROXY_BASE_URL.startsWith("https://")) { "Image service is unavailable" }
        Retrofit.Builder()
            .baseUrl(BuildConfig.UNSPLASH_PROXY_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildOkHttpClient())
            .build()
            .create()
    }

    private fun buildOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()

}