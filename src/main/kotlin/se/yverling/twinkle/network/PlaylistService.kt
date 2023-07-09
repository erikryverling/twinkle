package se.yverling.twinkle.network

import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PlaylistService {
    @POST("v1/playlists/{playlistId}/tracks")
    suspend fun addUriToPlaylist(
            @Path("playlistId") playlistId: String,
            @Query("uris") uris: String
    ): PlaylistResponse
}