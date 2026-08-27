package com.kunjika.app.core.util

import android.util.Log
import com.kunjika.app.BuildConfig

/**
 * Custom logging utility that ensures no logs are emitted in production/release builds.
 */
object KLog {
    private const val TAG = "Kunjika"

    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, message, throwable)
        }
    }

    fun i(message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, message)
        }
    }

    fun w(message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, message)
        }
    }
}
