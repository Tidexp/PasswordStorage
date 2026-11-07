package com.example.passwordstorage.data.security

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper

object ClipboardHelper {

    private val handler = Handler(Looper.getMainLooper())
    private var clearRunnable: Runnable? = null

    fun copyToClipboard(context: Context, label: String, text: String, autoClearSeconds: Int = 30, onCleared: (() -> Unit)? = null) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        
        // On Android 13+, sensitive content flag can be set
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            clip.description.extras = android.os.PersistableBundle().apply {
                putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }

        clipboard.setPrimaryClip(clip)

        // Cancel previous clear job if any
        clearRunnable?.let { handler.removeCallbacks(it) }

        // Schedule clipboard clear after 30 seconds
        clearRunnable = Runnable {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    clipboard.clearPrimaryClip()
                } else {
                    val emptyClip = ClipData.newPlainText("", "")
                    clipboard.setPrimaryClip(emptyClip)
                }
                onCleared?.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        handler.postDelayed(clearRunnable!!, autoClearSeconds * 1000L)
    }
}
