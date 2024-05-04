package com.github.sky130.suiteki.pro.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.github.sky130.suiteki.pro.MainApplication.Companion.context

object TextUtils {

    fun String.copyText(): Boolean {
        return try {
            val clipboard: ClipboardManager =
                (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?)!!
            val clipData = ClipData.newPlainText(null, this)
            clipboard.setPrimaryClip(clipData)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}