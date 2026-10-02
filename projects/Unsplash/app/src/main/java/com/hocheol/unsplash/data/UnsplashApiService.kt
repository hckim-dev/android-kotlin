package com.hocheol.unsplash.data

import com.hocheol.unsplash.data.models.PhotoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApiService {

    @GET(
        "/photos/random?" +
                "&count=30"
    )
    suspend fun getRandomPhotos(
        @Query("query") query: String?,
    ): Response<List<PhotoResponse>>

}
