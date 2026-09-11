package com.example.template.model

import java.time.Instant

data class Reminder(
    val id: Long,
    var content: String,
    val firstSentAt: Instant,
    var lastSentAt: Instant,
    var sendCount: Int
)
