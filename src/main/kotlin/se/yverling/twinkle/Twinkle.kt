package se.yverling.twinkle

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import se.yverling.twinkle.network.PlayerService
import se.yverling.twinkle.network.PlaylistService
import se.yverling.twinkle.network.TokenResponse
import se.yverling.twinkle.network.TokenService
import java.io.File
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.system.exitProcess

fun main() = runBlocking {
    Twinkle.addCurrentPlayingUriToPlaylist()
}

object Twinkle {
    private const val OAUTH_BASE_URL = "https://accounts.spotify.com"

    private const val CONF_FILE_NAME = ".twinkle"
    private const val CONF_DELIMITER = "="
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"
    private const val CLIENT_ID_SECRET_HASH_KEY = "client_id_secret_hash"
    private const val PLAYLIST_ID_KEY = "playlist_id"

    private const val AUDIO_PATH = "./audio"

    private const val API_BASE_URL = "https://api.spotify.com"

    private const val REFRESH_TOKEN_GRANT_TYPE = "refresh_token"

    private const val HTTP_CLIENT_TIMEOUT_IN_MS = 15000L

    private val homeFolder = System.getProperty("user.home")

    private var apiClient: HttpClient
    private var oauthClient: HttpClient

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var clientIdSecretHash: String
    private lateinit var playlistId: String

    init {
        readConfigValuesFromFile()

        when {
            !::accessToken.isInitialized -> exitWithMessage(ACCESS_TOKEN_KEY)
            !::refreshToken.isInitialized -> exitWithMessage(REFRESH_TOKEN_KEY)
            !::clientIdSecretHash.isInitialized -> exitWithMessage(CLIENT_ID_SECRET_HASH_KEY)
            !::playlistId.isInitialized -> exitWithMessage(PLAYLIST_ID_KEY)
        }

        apiClient = createApiClient()
        oauthClient = createOAuthClient()
    }

    private fun exitWithMessage(fieldName: String) {
        playFailSound()
        println("Could not parse $fieldName. Check ~/.twinkle and run again.")
        exitProcess(-1)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun addCurrentPlayingUriToPlaylist() {
        flow {
            emit(PlayerService.getCurrentlyPlaying(apiClient))
        }
            .flatMapConcat {
                flow {
                    emit(PlaylistService.addUriToPlaylist(apiClient, playlistId, it.item.uri))
                }
            }
            .flowOn(Dispatchers.IO)
            .catch {
                playFailSound()
                println(it.stackTraceToString())
                exitProcess(-1)
            }
            .collect {
                playSuccessSound()
                println("Currently playing added to playlist $playlistId successfully")
                exitProcess(0)
            }
    }

    fun refreshTokens(): Flow<TokenResponse> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "Basic $clientIdSecretHash"

        return flow {
            emit(TokenService.refreshTokens(
                client = oauthClient,
                grantType = REFRESH_TOKEN_GRANT_TYPE,
                refreshToken = refreshToken,
                headers = headerMap
            ))
        }
            .flowOn(Dispatchers.IO)
            .onEach {
                writeConfigurationFile(it.access_token)
                readConfigValuesFromFile()
            }
    }

    private fun refreshTokensSync(): String {
        return runBlocking {
            refreshTokens()
                .catch {
                    println(it.stackTraceToString())
                    exitProcess(-1)
                }
                .first().access_token
        }
    }

    private fun createApiClient(): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = HTTP_CLIENT_TIMEOUT_IN_MS
                connectTimeoutMillis = HTTP_CLIENT_TIMEOUT_IN_MS
                socketTimeoutMillis = HTTP_CLIENT_TIMEOUT_IN_MS
            }

            defaultRequest { url(API_BASE_URL) }

        }.also { client ->
            client.plugin(HttpSend).intercept { request ->
                // Add Bearer token to all requests
                request.headers.append("Authorization", "Bearer $accessToken")

                val originalCall = execute(request)

                if (originalCall.response.status == HttpStatusCode.Unauthorized) {
                    println("Access token expired. Refreshing tokens...")
                    val newToken = refreshTokensSync()

                    // Retry with new token
                    request.headers.remove("Authorization")
                    request.headers.append("Authorization", "Bearer $newToken")
                    execute(request)
                } else {
                    originalCall
                }
            }
        }
    }

    private fun createOAuthClient(): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = HTTP_CLIENT_TIMEOUT_IN_MS
                connectTimeoutMillis = HTTP_CLIENT_TIMEOUT_IN_MS
                socketTimeoutMillis = HTTP_CLIENT_TIMEOUT_IN_MS
            }

            defaultRequest { url(OAUTH_BASE_URL) }
        }
    }

    private fun playSuccessSound() = playSound("success.wav")
    private fun playFailSound() = playSound("fail.wav")

    private fun playSound(fileName: String) {
        val clip = AudioSystem.getClip()
        val inputStream: AudioInputStream =
            AudioSystem.getAudioInputStream(File("$AUDIO_PATH/$fileName"))
        clip.open(inputStream)
        clip.start()
    }

    private fun readConfigValuesFromFile() {
        File("$homeFolder/$CONF_FILE_NAME").forEachLine {
            val key = it.split(CONF_DELIMITER).first()
            val value = it.split(CONF_DELIMITER).last()

            if (value.isNotBlank()) {
                when (key) {
                    ACCESS_TOKEN_KEY -> accessToken = value
                    REFRESH_TOKEN_KEY -> refreshToken = value
                    CLIENT_ID_SECRET_HASH_KEY -> clientIdSecretHash = value
                    PLAYLIST_ID_KEY -> playlistId = value
                }
            }
        }
    }

    private fun writeConfigurationFile(accessToken: String) {
        File("$homeFolder/$CONF_FILE_NAME").printWriter().use {
            it.println("$ACCESS_TOKEN_KEY$CONF_DELIMITER$accessToken")
            it.println("$REFRESH_TOKEN_KEY$CONF_DELIMITER$refreshToken")
            it.println("$CLIENT_ID_SECRET_HASH_KEY$CONF_DELIMITER$clientIdSecretHash")
            it.println("$PLAYLIST_ID_KEY$CONF_DELIMITER$playlistId")
        }
    }
}
