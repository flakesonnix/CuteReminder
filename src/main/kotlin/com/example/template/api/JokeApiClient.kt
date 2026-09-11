package com.example.template.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

class JokeApiClient {
    private val client = HttpClient(CIO)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchRandomJoke(): Joke? =
        try {
            val raw = client.get("https://official-joke-api.appspot.com/random_joke").bodyAsText()
            json.decodeFromString(raw)
        } catch (e: Exception) {
            null
        }

    fun close() {
        client.close()
    }
}
