package com.liteweight.session.data

import com.liteweight.session.data.local.SessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SessionStatusTest {
    @Test
    fun completedStatusDiffersFromDraft() {
        assertNotEquals(SessionStatus.DRAFT, SessionStatus.COMPLETED)
        assertEquals("COMPLETED", SessionStatus.COMPLETED.name)
    }
}
