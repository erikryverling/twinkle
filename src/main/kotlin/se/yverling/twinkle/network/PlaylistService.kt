package se.yverling.twinkle.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post

object PlaylistService {
    suspend fun addUriToPlaylist(
        client: HttpClient,
        playlistId: String,
        uris: String
    ): PlaylistResponse {
        return client.post("/v1/playlists/$playlistId/tracks") {
            parameter("uris", uris)
        }.body()
    }
}
