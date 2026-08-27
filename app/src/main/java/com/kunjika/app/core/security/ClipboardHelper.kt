package com.kunjika.app.core.security

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ClipboardHelper {
    private var autoClearJob: Job? = null

    fun copyToClipboard(
        context: Context,
        label: String,
        text: String,
        isSensitive: Boolean = true,
        autoClearSeconds: Long = 30L
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)

        // Mark sensitive for Android 13+ (API 33+) so keyboards & clip managers don't record or show preview
        if (isSensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val bundle = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
            clip.description.extras = bundle
        }

        clipboard.setPrimaryClip(clip)

        // Cancel previous auto-clear job and launch a new countdown
        autoClearJob?.cancel()
        if (autoClearSeconds > 0) {
            autoClearJob = CoroutineScope(Dispatchers.Main).launch {
                delay(autoClearSeconds * 1000L)
                if (clipboard.hasPrimaryClip()) {
                    val primaryClip = clipboard.primaryClip
                    if (primaryClip != null && primaryClip.itemCount > 0) {
                        val currentText = primaryClip.getItemAt(0).text?.toString()
                        if (currentText == text) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                clipboard.clearPrimaryClip()
                            } else {
                                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                            }
                        }
                    }
                }
            }
        }
    }
}
