package com.aliothmoon.maameow.automation.ipc

import org.junit.Assert.assertEquals
import org.junit.Test

class ParcelableRoundTripTest {
    @Test
    fun `session request preserves opaque payload fields`() {
        val request = RemoteSessionRequest("sample", "start", 2, "{\"private\":true}")
        val copied = request.copy()
        assertEquals(request, copied)
        assertEquals("{\"private\":true}", copied.payloadJson)
    }

    @Test
    fun `permission request defaults remain stable`() {
        val request = PermissionGrantRequest("com.example")
        assertEquals(PermissionGrantRequest.PERM_ALL, request.permissions)
    }
}
