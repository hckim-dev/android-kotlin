package com.hocheol.githubrepository.utillity

import com.hocheol.githubrepository.data.response.GithubAccessTokenResponse
import com.hocheol.githubrepository.data.response.GithubDeviceCodeResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApiService {
    @FormUrlEncoded
    @POST("login/device/code")
    @Headers("Accept: application/json")
    suspend fun getDeviceCode(
        @Field("client_id") clientId: String
    ): Response<GithubDeviceCodeResponse>

    @FormUrlEncoded
    @POST("login/oauth/access_token")
    @Headers("Accept: application/json")
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("device_code") deviceCode: String,
        @Field("grant_type") grantType: String = "urn:ietf:params:oauth:grant-type:device_code"
    ): Response<GithubAccessTokenResponse>
}
