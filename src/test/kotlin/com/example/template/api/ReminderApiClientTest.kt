package com.example.template.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ReminderApiClientTest {

    @Test
    fun `can instantiate`() {
        val client = ReminderApiClient()
        assertNotNull(client)
    }
}
