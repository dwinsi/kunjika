package com.kunjika.app.core.security

/** In-process lock state shared with the Android Autofill service. */
object VaultSession {
    @Volatile
    var isUnlocked: Boolean = false

    @Volatile
    private var backgroundedAt: Long = 0L

    fun markUnlocked() {
        backgroundedAt = 0L
        isUnlocked = true
    }

    fun markBackgrounded() {
        backgroundedAt = System.currentTimeMillis()
    }

    fun markForegrounded() {
        backgroundedAt = 0L
    }

    fun isFresh(timeoutSeconds: Int): Boolean {
        if (!isUnlocked) return false
        val backgroundAt = backgroundedAt
        if (timeoutSeconds > 0 && backgroundAt > 0 &&
            System.currentTimeMillis() - backgroundAt >= timeoutSeconds * 1000L
        ) {
            isUnlocked = false
            return false
        }
        return true
    }
}
