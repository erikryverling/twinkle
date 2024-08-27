package se.yverling.twinkle.network

import kotlinx.serialization.Serializable

@Serializable
data class PlayerResponse(val item: Item)

@Serializable
data class Item(val uri: String)