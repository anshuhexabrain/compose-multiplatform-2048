package com.alexjlockwood.twentyfortyeight.analytics

import com.alexjlockwood.twentyfortyeight.brightsdk.BrightDataSdk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.harawata.appdirs.AppDirsFactory
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

actual fun createAppAnalytics(brightDataSdk: BrightDataSdk): AppAnalytics {
    return DesktopAppAnalytics()
}

private class DesktopAppAnalytics : AppAnalytics {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build()
    private val stateMutex = Mutex()
    private val appStartSent = AtomicBoolean(false)
    private val sessionIdReady = CompletableDeferred<String?>()

    override fun trackAppStart(brdEnabled: Boolean) {
        if (!appStartSent.compareAndSet(false, true)) {
            return
        }

        scope.launch {
            val stateSnapshot = stateMutex.withLock {
                val current = loadState()
                val withDeviceId = current.copy(
                    deviceId = resolveDeviceId(current.deviceId)
                )
                saveState(withDeviceId.copy(isFirstTime = false))
                withDeviceId
            }

            val request = AppStartSessionRequest(
                action = AnalyticsAction.APP_START.value,
                value = brdEnabled.toAnalyticsValue(),
                isFirstTime = stateSnapshot.isFirstTime
            )

            println("Analytics: APP_START request = $request")

            val responseBody = postJson(
                body = json.encodeToString(AppStartSessionRequest.serializer(), request),
                sessionId = null
            )

            val sessionId = responseBody
                ?.let { runCatching { json.decodeFromString(SessionResponse.serializer(), it).data.sessionId }.getOrNull() }

            println("Analytics: APP_START session id = ${sessionId ?: "<empty>"}")
            sessionIdReady.complete(sessionId)
        }
    }

    override fun trackBrdState(brdEnabled: Boolean) {
        scope.launch {
            val request = SessionRequest(
                action = AnalyticsAction.BRD.value,
                value = brdEnabled.toAnalyticsValue()
            )

            println("Analytics: BRD request = $request")

            val sessionId = withTimeoutOrNull(10_000) {
                sessionIdReady.await()
            }

            println("Analytics: BRD session header = ${sessionId ?: "<empty>"}")

            postJson(
                body = json.encodeToString(SessionRequest.serializer(), request),
                sessionId = sessionId
            )
        }
    }

    private suspend fun loadState(): AnalyticsState {
        return runCatching {
            val file = analyticsStateFile()
            val state = if (!file.exists()) {
                AnalyticsState()
            } else {
                json.decodeFromString(AnalyticsState.serializer(), file.readText())
            }
            val resolved = state.copy(deviceId = resolveDeviceId(state.deviceId))
            if (resolved != state) {
                saveState(resolved)
            }
            resolved
        }.getOrElse { AnalyticsState(deviceId = resolveDeviceId("")) }
    }

    private suspend fun saveState(state: AnalyticsState) {
        val file = analyticsStateFile()
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(AnalyticsState.serializer(), state))
    }

    private fun analyticsStateFile(): File {
        val versionedDir = File(
            AppDirsFactory.getInstance().getUserDataDir(APP_DATA_DIR_NAME, APP_VERSION, APP_AUTHOR)
        )
        val stableDir = versionedDir.parentFile ?: versionedDir
        return File(stableDir, ANALYTICS_STATE_FILE)
    }

    private fun postJson(body: String, sessionId: String?): String? {
        return runCatching {
            val state = loadStateBlocking()
            val builder = HttpRequest.newBuilder()
                .uri(URI.create("$BASE_URL$ANALYTICS_PATH"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("X-Req-Device", state.deviceId)
                .header("X-Api-Key", API_KEY)
                .header("X-Req-Platform", PLATFORM)
                .header("User-Agent", "$PLATFORM/1.0")
                .header("Device-Model", System.getProperty("os.arch", "unknown"))
                .header("Device-Manufacturer", System.getProperty("os.name", "Windows"))
                .header("Device-Brand", "Windows")
                .header("Device-SDK-Version", System.getProperty("os.version", "unknown"))
                .header("App-Version", APP_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(body))

            sessionId
                ?.takeIf { it.isNotBlank() }
                ?.let { builder.header("X-App-Session", it) }

            val request = builder.build()
            println("Analytics: POST ${request.uri()}")
            println("Analytics: Headers = ${request.headers().map()}")
            println("Analytics: Body = $body")

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            println("Analytics: Response code = ${response.statusCode()}")
            println("Analytics: Response body = ${response.body()}")
            if (response.statusCode() in 200..299) response.body() else null
        }.getOrElse { error ->
            println("Analytics: Request failed: ${error.message}")
            error.printStackTrace()
            null
        }
    }

    private fun loadStateBlocking(): AnalyticsState {
        return runCatching {
            val file = analyticsStateFile()
            val state = if (!file.exists()) {
                AnalyticsState(deviceId = resolveDeviceId(""))
            } else {
                json.decodeFromString(AnalyticsState.serializer(), file.readText())
            }
            val resolved = state.copy(deviceId = resolveDeviceId(state.deviceId))
            if (!file.exists() || resolved != state) {
                saveStateBlocking(resolved)
            }
            resolved
        }.getOrElse {
            AnalyticsState(deviceId = resolveDeviceId(""))
        }
    }

    private fun saveStateBlocking(state: AnalyticsState) {
        val file = analyticsStateFile()
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(AnalyticsState.serializer(), state))
    }
    private fun resolveDeviceId(savedDeviceId: String): String {
        return readMachineGuid()
            ?: savedDeviceId.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
    }

    private fun readMachineGuid(): String? {
        return runCatching {
            if (!Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, MACHINE_GUID_REG_PATH, MACHINE_GUID_VALUE_NAME)) {
                null
            } else {
                Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_LOCAL_MACHINE,
                    MACHINE_GUID_REG_PATH,
                    MACHINE_GUID_VALUE_NAME
                )
            }
        }.getOrNull()?.trim()?.takeIf { it.isNotEmpty() }
    }

    @Serializable
    private data class AnalyticsState(
        val deviceId: String = "",
        val isFirstTime: Boolean = true
    )

    @Serializable
    private data class AppStartSessionRequest(
        val action: String,
        val value: String,
        val isFirstTime: Boolean
    )

    @Serializable
    private data class SessionRequest(
        val action: String,
        val value: String
    )

    @Serializable
    private data class SessionResponse(
        val data: SessionData
    )

    @Serializable
    private data class SessionData(
        val sessionId: String
    )

    private enum class AnalyticsAction(val value: String) {
        APP_START("APP_START"),
        BRD("BRD")
    }

    private companion object {
        private const val BASE_URL = "https://app.hexabrain.com/hexa-game-api/"
        private const val ANALYTICS_PATH = "analytics/app/log"
        private const val API_KEY = "4dcf5c8a66ca425cee96cff1e54e1210"
        private const val PLATFORM = "WINDOWS"
        private const val APP_VERSION = "1.0.0"
        private const val APP_AUTHOR = "Hexabrain Systems"
        private const val APP_DATA_DIR_NAME = "com.alexjlockwood.twentyfortyeight"
        private const val ANALYTICS_STATE_FILE = "analytics_state.json"
        private const val MACHINE_GUID_REG_PATH = "SOFTWARE\\Microsoft\\Cryptography"
        private const val MACHINE_GUID_VALUE_NAME = "MachineGuid"

        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}

private fun Boolean.toAnalyticsValue(): String = if (this) "1" else "0"


