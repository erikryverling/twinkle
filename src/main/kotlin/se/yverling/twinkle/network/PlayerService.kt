package se.yverling.twinkle.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

object PlayerService {
    suspend fun getCurrentlyPlaying(client: HttpClient): PlayerResponse {
        return client.get("v1/me/player/currently-playing").body()
    }
}
