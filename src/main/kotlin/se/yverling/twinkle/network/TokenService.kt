package se.yverling.twinkle.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface TokenService {
    @FormUrlEncoded
    @POST("api/token")
    suspend fun refreshTokens(
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
        @HeaderMap headers: Map<String, String>
    ): TokenResponse
}