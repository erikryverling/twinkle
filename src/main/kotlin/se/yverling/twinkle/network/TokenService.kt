package se.yverling.twinkle.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.http.parameters

object TokenService {
    suspend fun refreshTokens(
        client: HttpClient,
        grantType: String,
        refreshToken: String,
        headers: Map<String, String>
    ): TokenResponse {
        return client.submitForm(
            url = "api/token",
            formParameters = parameters {
                append("grant_type", grantType)
                append("refresh_token", refreshToken)
            }
        ) {
            headers.forEach { (key, value) ->
                header(key, value)
            }
        }.body()
    }
}
