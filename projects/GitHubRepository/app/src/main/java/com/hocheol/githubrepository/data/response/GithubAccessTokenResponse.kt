package com.hocheol.githubrepository.data.response

data class GithubAccessTokenResponse(
    val accessToken: String? = null,
    val scope: String? = null,
    val tokenType: String? = null,
    val error: String? = null
)
