package com.hocheol.restaurantmap

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object SearchRepository {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit by lazy {
        require(BuildConfig.NAVER_SEARCH_PROXY_BASE_URL.startsWith("https://")) { "Search service is unavailable" }
        Retrofit.Builder()
        .baseUrl(BuildConfig.NAVER_SEARCH_PROXY_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    }

    private val service by lazy { retrofit.create(SearchService::class.java) }

    fun getGoodRestaurants(query: String): Call<SearchResult> {
        return service.getGoodRestaurants(query = "$query 맛집", display = 5)
    }

}
