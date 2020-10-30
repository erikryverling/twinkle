package se.yverling.twinkle.network

import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TokenService {
    @FormUrlEncoded
    @POST("api/token")
    fun refreshTokens(
            @Field("grant_type") grantType: String,
            @Field("refresh_token") refreshToken: String,
            @Field("client_id") clientId: String
    ): Single<TokenResponse>
}