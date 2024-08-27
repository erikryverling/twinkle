package se.yverling.twinkle.network

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(val access_token: String)