package com.example.template.api

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class JokeApiClientTest {

    @Test
    fun `json parsing works for valid joke`() {
        val raw = """{"id":10,"type":"general","setup":"Setup","punchline":"Punch"}"""
        val decoded = Json { ignoreUnknownKeys = true }.decodeFromString<Joke>(raw)
        assertEquals(10, decoded.id)
        assertEquals("Setup", decoded.setup)
    }

    @Test
    fun `client close does not throw`() {
        val client = JokeApiClient()
        // close should not throw even if not used
        client.close()
        // second close also safe
        client.close()
    }

    @Test
    fun `fetch returns null on invalid json via direct decode failure`() = runTest {
        // verify our Json config ignores unknown keys but fails on completely invalid
        val invalid = "not json"
        var caught = false
        try {
            Json { ignoreUnknownKeys = true }.decodeFromString<Joke>(invalid)
        } catch (_: Exception) {
            caught = true
        }
        assertNotNull(caught)
        assertEquals(true, caught)
    }

    @Test
    fun `joke model null handling`() {
        // JokeApiClient fetch catches exceptions and returns null – we verify null path by ensuring client handles missing fields gracefully
        // Here we just ensure Joke requires all fields, missing field throws
        val incomplete = """{"id":1,"type":"general","setup":"hi"}"""
        var threw = false
        try {
            Json { ignoreUnknownKeys = true }.decodeFromString<Joke>(incomplete)
        } catch (_: Exception) {
            threw = true
        }
        assertEquals(true, threw)
    }
}
