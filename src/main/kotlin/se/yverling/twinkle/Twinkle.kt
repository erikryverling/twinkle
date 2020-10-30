package se.yverling.twinkle

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import se.yverling.twinkle.Twinkle.OAUTH_HEADER_NAME
import se.yverling.twinkle.network.PlayerService
import se.yverling.twinkle.network.PlaylistService
import se.yverling.twinkle.network.TokenResponse
import se.yverling.twinkle.network.TokenService
import java.io.File
import java.util.concurrent.TimeUnit
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.system.exitProcess

fun main() {
    Twinkle.addCurrentPlayingUriToPlaylist()
}

object Twinkle {
    const val OAUTH_HEADER_NAME = "Authorization"
    private const val OAUTH_HOST = "accounts.spotify.com"
    private const val OAUTH_BASE_URL = "https://$OAUTH_HOST"

    private const val CONF_FILE_NAME = ".twinkle"
    private const val CONF_DELIMITER = "="
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"
    private const val CLIENT_ID_KEY = "client_id"
    private const val PLAYLIST_ID_KEY = "playlist_id"

    private const val AUDIO_PATH = "./audio"

    private const val API_BASE_URL = "https://api.spotify.com"

    private const val REFRESH_TOKEN_GRANT_TYPE = "refresh_token"

    private const val HTTP_CLIENT_TIMEOUT_IN_SEC = 15L

    private val homeFolder = System.getProperty("user.home")

    private var client: OkHttpClient

    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var clientId: String
    private lateinit var playlistId: String

    init {
        client = createHttpClient()
        readConfigValuesFromFile()

        when {
            !::accessToken.isInitialized -> exitWithMessage(ACCESS_TOKEN_KEY)
            !::refreshToken.isInitialized -> exitWithMessage(REFRESH_TOKEN_KEY)
            !::clientId.isInitialized -> exitWithMessage(CLIENT_ID_KEY)
            !::playlistId.isInitialized -> exitWithMessage(PLAYLIST_ID_KEY)
        }
    }

    private fun exitWithMessage(fieldName: String) {
        playFailSound()
        println("Could not parse $fieldName. Check ~/.twinkle and run again.")
        exitProcess(-1)
    }


    fun addCurrentPlayingUriToPlaylist() {
        val playerService = buildService<PlayerService>(API_BASE_URL)
        val playlistService = buildService<PlaylistService>(API_BASE_URL)

        playerService.getCurrentlyPlaying()
            .flatMap { playlistService.addUriToPlaylist(playlistId, it.item.uri) }
            .subscribeBy(
                onSuccess = {
                    playSuccessSound()
                    println("Currently playing added to playlist $playlistId successfully")
                    exitProcess(0)
                },
                onError = {
                    playFailSound()
                    println(it.stackTraceToString())
                    exitProcess(-1)
                }
            )
    }

    fun refreshTokens(): Single<TokenResponse> {
        return buildService<TokenService>(OAUTH_BASE_URL)
            .refreshTokens(REFRESH_TOKEN_GRANT_TYPE, refreshToken, clientId)
            .doOnSuccess {
                writeConfigurationFile(it.access_token, it.refresh_token)
                readConfigValuesFromFile()
            }
    }

    private fun createHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(HTTP_CLIENT_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                var request = chain.request()

                // Add OAUTH header to all authenticated requests
                if (chain.request().url().host() != OAUTH_HOST) {
                    request = chain.request().newBuilder()
                        .addHeader(OAUTH_HEADER_NAME, "Bearer $accessToken")
                        .build()
                }

                chain.proceed(request)
            }
            .authenticator(AccessTokenAuthenticator())
            .readTimeout(HTTP_CLIENT_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
            .build()
    }

    private inline fun <reified T> buildService(baseUrl: String): T {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(T::class.java)
    }


    private fun playSuccessSound() = playSound("success.wav")
    private fun playFailSound() = playSound("fail.wav")

    private fun playSound(fileName: String) {
        val clip = AudioSystem.getClip()
        val inputStream: AudioInputStream = AudioSystem.getAudioInputStream(File("$AUDIO_PATH/$fileName"))
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
                    CLIENT_ID_KEY -> clientId = value
                    PLAYLIST_ID_KEY -> playlistId = value
                }
            }
        }
    }

    private fun writeConfigurationFile(accessToken: String, refreshToken: String) {
        File("$homeFolder/$CONF_FILE_NAME").printWriter().use {
            it.println("$ACCESS_TOKEN_KEY$CONF_DELIMITER$accessToken")
            it.println("$REFRESH_TOKEN_KEY$CONF_DELIMITER$refreshToken")
            it.println("$CLIENT_ID_KEY$CONF_DELIMITER$clientId")
            it.println("$PLAYLIST_ID_KEY$CONF_DELIMITER$playlistId")
        }
    }
}

class AccessTokenAuthenticator : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {

        // Refresh access (and refresh) token if it has expired
        if (response.code() == 401) {
            println("Access token expired. Refreshing tokens...")

            val updatedToken = Twinkle.refreshTokens().blockingGet().access_token

            return response.request()
                .newBuilder()
                .removeHeader(OAUTH_HEADER_NAME)
                .addHeader(OAUTH_HEADER_NAME, "Bearer $updatedToken")
                .build()
        }
        return null
    }
}