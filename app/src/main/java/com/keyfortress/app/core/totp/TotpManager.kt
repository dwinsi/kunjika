package com.keyfortress.app.core.totp

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object TotpManager {

    private const val ALGORITHM = "HmacSHA1"
    private const val DIGITS = 6
    private const val PERIOD = 30L // seconds

    fun generateTotp(secret: String, timestamp: Long = System.currentTimeMillis()): String? {
        val key = try {
            Base32.decode(secret.replace(" ", "").uppercase())
        } catch (e: Exception) {
            return null
        }

        if (key.isEmpty()) return null

        val counter = timestamp / 1000 / PERIOD
        val data = ByteBuffer.allocate(8).putLong(counter).array()

        val mac = Mac.getInstance(ALGORITHM)
        mac.init(SecretKeySpec(key, ALGORITHM))
        val hash = mac.doFinal(data)

        val offset = hash[hash.size - 1].toInt() and 0x0F
        val truncatedHash = ((hash[offset].toInt() and 0x7F shl 24) or
                (hash[offset + 1].toInt() and 0xFF shl 16) or
                (hash[offset + 2].toInt() and 0xFF shl 8) or
                (hash[offset + 3].toInt() and 0xFF))

        val otp = truncatedHash % 10.0.pow(DIGITS.toDouble()).toInt()
        return otp.toString().padStart(DIGITS, '0')
    }

    fun getRemainingSeconds(timestamp: Long = System.currentTimeMillis()): Int {
        return (PERIOD - (timestamp / 1000 % PERIOD)).toInt()
    }

    fun getProgress(timestamp: Long = System.currentTimeMillis()): Float {
        return (timestamp / 1000 % PERIOD).toFloat() / PERIOD.toFloat()
    }

    private object Base32 {
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        private val DECODE_TABLE = IntArray(128) { -1 }

        init {
            for (i in ALPHABET.indices) {
                DECODE_TABLE[ALPHABET[i].code] = i
            }
        }

        fun decode(base32: String): ByteArray {
            val cleaned = base32.filter { it != '=' }
            val outLen = cleaned.length * 5 / 8
            val bytes = ByteArray(outLen)
            var buffer = 0
            var bitsLeft = 0
            var count = 0
            for (char in cleaned) {
                val value = DECODE_TABLE[char.code]
                if (value == -1) throw IllegalArgumentException("Invalid Base32 character: $char")
                buffer = (buffer shl 5) or value
                bitsLeft += 5
                if (bitsLeft >= 8) {
                    bytes[count++] = (buffer shr (bitsLeft - 8)).toByte()
                    bitsLeft -= 8
                }
            }
            return bytes
        }
    }
}
