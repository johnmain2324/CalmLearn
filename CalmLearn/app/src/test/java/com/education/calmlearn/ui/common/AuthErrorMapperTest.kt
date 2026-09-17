package com.education.calmlearn.ui.common

import com.education.calmlearn.data.auth.AuthErrorReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthErrorMapperTest {

    @Test
    fun `every reason maps to a distinct string resource`() {
        val mapped = AuthErrorReason.entries.associateWith { it.toMessageRes() }

        // Khong duoc co reason nao "roi" ve chung 1 chuoi voi reason khac (moi loi phai co thong
        // bao rieng, tranh nguoi dung khong biet chinh xac dang gap loi gi).
        assertEquals(AuthErrorReason.entries.size, mapped.values.toSet().size)
    }

    @Test
    fun `every reason maps to a valid non-zero resource id`() {
        AuthErrorReason.entries.forEach { reason ->
            assertTrue("Thieu string resource cho $reason", reason.toMessageRes() != 0)
        }
    }
}
