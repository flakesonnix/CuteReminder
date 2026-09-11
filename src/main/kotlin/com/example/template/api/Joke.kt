package com.example.template.api

import kotlinx.serialization.Serializable

@Serializable
data class Joke(
    val id: Int,
    val type: String,
    val setup: String,
    val punchline: String
)
