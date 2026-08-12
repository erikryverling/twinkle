package se.yverling.twinkle.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.http.parameters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object TokenService {
    suspend fun refreshTokens(
        client: HttpClient,
        grantType: String,
        refreshToken: String,
        headers: Map<String, String>
    ): TokenResponse {
        val response = client.submitForm(
            url = "api/token",
            formParameters = parameters {
                append("grant_type", grantType)
                append("refresh_token", refreshToken)
            }
        ) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
        }

        if (response.status.value !in 200..299) {
            val errorResponse: TokenErrorResponse = response.body()
            val description = errorResponse.description?.let { ": $it" }.orEmpty()

            error("Spotify token refresh failed (${response.status.value}): ${errorResponse.error}$description")
        }

        return response.body()
    }
}

@Serializable
private data class TokenErrorResponse(
    val error: String,
    @SerialName("error_description") val description: String? = null
)
