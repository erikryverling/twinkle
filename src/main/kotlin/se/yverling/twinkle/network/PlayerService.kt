package se.yverling.twinkle.network

import io.reactivex.Single
import retrofit2.http.GET

interface PlayerService {
    @GET("v1/me/player/currently-playing")
    fun getCurrentlyPlaying(): Single<PlayerResponse>
}