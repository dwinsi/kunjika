package com.kunjika.app.core.security

import android.content.Context
import android.util.Base64
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityServiceException
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.model.IntegrityErrorCode
import com.kunjika.app.core.util.KLog
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.SecureRandom
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Result wrapper for Play Integrity token generation.
 */
data class IntegrityTokenResult(
    val token: String,
    val nonce: String
)

/**
 * Manager for interacting with Google Play Integrity API.
 *
 * Requests an integrity token from Google Play Services on the device.
 * The generated token contains attestation verdicts (device integrity, app recognition,
 * licensing status) and MUST be sent to a backend server or Firebase App Check to be
 * decrypted and verified against Google's servers.
 */
class PlayIntegrityManager(private val context: Context) {

    private val integrityManager: IntegrityManager by lazy {
        IntegrityManagerFactory.create(context.applicationContext)
    }

    /**
     * Generates a cryptographically secure, URL-safe Base64 nonce without padding.
     *
     * @param length Number of random bytes (default 24 bytes).
     */
    fun generateNonce(length: Int = 24): String {
        val randomBytes = ByteArray(length)
        SecureRandom().nextBytes(randomBytes)
        return Base64.encodeToString(
            randomBytes,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

    /**
     * Requests a Play Integrity Token.
     *
     * @param nonce A unique cryptographic nonce. Should ideally originate from your backend server.
     * @param cloudProjectNumber Optional Google Cloud Project Number (if using unlinked/custom GCP project).
     * @return [Result] containing [IntegrityTokenResult] with token and nonce, or error.
     */
    suspend fun requestIntegrityToken(
        nonce: String = generateNonce(),
        cloudProjectNumber: Long? = null
    ): Result<IntegrityTokenResult> {
        return runCatching {
            KLog.d("[PlayIntegrityManager] Requesting integrity token with nonce length=${nonce.length}")

            val requestBuilder = IntegrityTokenRequest.builder()
                .setNonce(nonce)

            if (cloudProjectNumber != null) {
                requestBuilder.setCloudProjectNumber(cloudProjectNumber)
            }

            val request = requestBuilder.build()

            val tokenResponse = suspendCancellableCoroutine { continuation ->
                integrityManager.requestIntegrityToken(request)
                    .addOnSuccessListener { response ->
                        if (continuation.isActive) {
                            continuation.resume(response)
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) {
                            continuation.resumeWithException(exception)
                        }
                    }
            }

            val token = tokenResponse.token()
            KLog.d("[PlayIntegrityManager] Integrity token retrieved successfully (length=${token.length})")
            IntegrityTokenResult(token = token, nonce = nonce)
        }.onFailure { exception ->
            val readableError = getReadableErrorMessage(exception)
            KLog.e("[PlayIntegrityManager] Failed to get Play Integrity token: $readableError", exception)
        }
    }

    /**
     * Helper to translate Play Integrity error codes to human-readable strings.
     */
    fun getReadableErrorMessage(throwable: Throwable): String {
        if (throwable !is IntegrityServiceException) {
            return throwable.localizedMessage ?: "Unknown error occurred"
        }

        return when (throwable.errorCode) {
            IntegrityErrorCode.API_NOT_AVAILABLE -> "Play Integrity API is not available on this device. Ensure Google Play Services is updated."
            IntegrityErrorCode.NO_ERROR -> "No error"
            IntegrityErrorCode.PLAY_STORE_NOT_FOUND -> "Google Play Store app was not found or is disabled."
            IntegrityErrorCode.NETWORK_ERROR -> "Network error while connecting to Google Play Services."
            IntegrityErrorCode.PLAY_STORE_ACCOUNT_NOT_FOUND -> "No Google account found on device Play Store."
            IntegrityErrorCode.APP_NOT_INSTALLED -> "App is not recognized by Google Play."
            IntegrityErrorCode.CANNOT_BIND_TO_SERVICE -> "Cannot bind to Google Play Services."
            IntegrityErrorCode.NONCE_TOO_SHORT -> "Provided nonce is too short. It must be at least 16 bytes URL-safe Base64."
            IntegrityErrorCode.NONCE_TOO_LONG -> "Provided nonce is too long. Maximum allowed length is 500 characters."
            IntegrityErrorCode.GOOGLE_SERVER_UNAVAILABLE -> "Google Play Integrity servers are temporarily unavailable."
            IntegrityErrorCode.NONCE_IS_NOT_BASE64 -> "Nonce must be URL-safe Base64 encoded without padding."
            IntegrityErrorCode.PLAY_STORE_VERSION_OUTDATED -> "Google Play Store app is outdated and needs an update."
            IntegrityErrorCode.PLAY_SERVICES_NOT_FOUND -> "Google Play Services is missing or disabled."
            IntegrityErrorCode.CLOUD_PROJECT_NUMBER_IS_INVALID -> "Invalid Google Cloud Project Number provided."
            IntegrityErrorCode.CLIENT_TRANSIENT_ERROR -> "Transient client error. Please retry after a brief pause."
            IntegrityErrorCode.INTERNAL_ERROR -> "Internal Play Integrity error."
            else -> "Play Integrity Error (Code: ${throwable.errorCode})"
        }
    }
}
