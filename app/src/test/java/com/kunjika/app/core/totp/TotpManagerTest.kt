package com.kunjika.app.core.totp

import org.junit.Assert.*
import org.junit.Test

class TotpManagerTest {

    private val testSecret = "JBSWY3DPEHPK3PXP" // "Hello!" in Base32

    @Test
    fun `generateTotp returns 6 digit string`() {
        val code = TotpManager.generateTotp(testSecret)
        assertNotNull(code)
        assertEquals(6, code?.length)
        assertTrue(code?.all { it.isDigit() } == true)
    }

    @Test
    fun `generateTotp returns null for invalid secret`() {
        val code = TotpManager.generateTotp("invalid!!!")
        assertNull(code)
    }

    @Test
    fun `generateTotp produces same code for same 30s window`() {
        val now = System.currentTimeMillis()
        val startOfWindow = (now / 30000) * 30000
        
        val code1 = TotpManager.generateTotp(testSecret, startOfWindow)
        val code2 = TotpManager.generateTotp(testSecret, startOfWindow + 15000) // 15s later
        
        assertEquals(code1, code2)
    }

    @Test
    fun `generateTotp produces different code for different windows`() {
        val now = System.currentTimeMillis()
        val code1 = TotpManager.generateTotp(testSecret, now)
        val code2 = TotpManager.generateTotp(testSecret, now + 31000) // 31s later
        
        assertNotEquals(code1, code2)
    }
}
