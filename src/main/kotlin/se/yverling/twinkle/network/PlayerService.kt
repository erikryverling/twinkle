package se.yverling.twinkle.network

import retrofit2.http.GET

interface PlayerService {
    @GET("v1/me/player/currently-playing")
    suspend fun getCurrentlyPlaying(): PlayerResponse
}