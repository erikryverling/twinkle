package se.yverling.twinkle.network

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistResponse(val snapshot_id: String)