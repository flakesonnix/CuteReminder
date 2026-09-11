package com.example.template.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class JokeTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `joke serializes and deserializes`() {
        val joke = Joke(1, "general", "Why did the chicken?", "To get to the other side")
        val encoded = json.encodeToString(joke)
        assertNotNull(encoded)
        val decoded = json.decodeFromString<Joke>(encoded)
        assertEquals(joke.id, decoded.id)
        assertEquals(joke.type, decoded.type)
        assertEquals(joke.setup, decoded.setup)
        assertEquals(joke.punchline, decoded.punchline)
    }

    @Test
    fun `joke decodes ignores unknown keys`() {
        val raw = """{"id":42,"type":"programming","setup":"hello","punchline":"world","extra":"ignored"}"""
        val decoded = json.decodeFromString<Joke>(raw)
        assertEquals(42, decoded.id)
        assertEquals("programming", decoded.type)
    }

    @Test
    fun `joke handles special characters`() {
        val joke = Joke(2, "general", "Setup with \"quotes\" & §colors", "Punch with \n newline")
        val encoded = json.encodeToString(joke)
        val decoded = json.decodeFromString<Joke>(encoded)
        assertEquals(joke.setup, decoded.setup)
        assertEquals(joke.punchline, decoded.punchline)
    }
}
