package com.kunjika.app.core.security

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import android.content.ClipboardManager

object ClipboardHelper {
    fun copyToClipboard(
        context: Context,
        label: String,
        text: String,
        isSensitive: Boolean = true
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)

        // This suppresses previews in supporting system UI. It cannot prevent clipboard readers/history.
        if (isSensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val bundle = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
            clip.description.extras = bundle
        }

        clipboard.setPrimaryClip(clip)
        // Do not schedule clipboard clearing: apps cannot reliably detect identical newer
        // clipboard content, and clearing can erase a user's later copy. Clipboard readers
        // and managers may already have captured the value before this app can react.
    }
}
