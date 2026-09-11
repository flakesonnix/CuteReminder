package com.example.template.model

import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class ReminderTest {

    @Test
    fun `reminder holds fields`() {
        val now = Instant.now()
        val r = Reminder(1L, "hello", now, now, 0)
        assertEquals(1L, r.id)
        assertEquals("hello", r.content)
        assertEquals(now, r.firstSentAt)
        assertEquals(0, r.sendCount)
    }

    @Test
    fun `reminder mutable fields update`() {
        val now = Instant.now()
        val later = now.plusSeconds(60)
        val r = Reminder(2L, "a", now, now, 1)
        r.content = "b"
        r.lastSentAt = later
        r.sendCount = 2
        assertEquals("b", r.content)
        assertEquals(later, r.lastSentAt)
        assertEquals(2, r.sendCount)
    }

    @Test
    fun `reminder copy and equality`() {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val r1 = Reminder(1L, "x", now, now, 0)
        val r2 = r1.copy(content = "y")
        assertEquals(1L, r2.id)
        assertEquals("y", r2.content)
        assertNotEquals(r1.content, r2.content)
    }
}
