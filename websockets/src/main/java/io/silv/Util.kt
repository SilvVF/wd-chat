package io.silv

import kotlinx.serialization.json.Json

internal val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    isLenient = true
}


internal const val serverPort = 8000