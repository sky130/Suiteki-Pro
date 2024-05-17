package com.github.sky130.suiteki.pro.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.github.sky130.suiteki.pro.MainApplication.Companion.context
import java.util.regex.Matcher
import java.util.regex.Pattern


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

    fun getRegexMatchText(str: String, left: String, right: String): String {
        val temp = getRegexMatchTextArray(str, left, right)
        return if (temp.isEmpty()) ""
        else temp[0]!!
    }

    fun getRegexMatchTextArray(str: String, left: String, right: String): Array<out String?> {
        return if ("" != str && "" != left && "" != right) regexMatch(
            str,
            "(?<=\\Q$left\\E).*?(?=\\Q$right\\E)"
        ) else arrayOfNulls(0)
    }

    private fun regexMatch(text: String, statement: String): Array<String> {
        val pn: Pattern = Pattern.compile(statement, 40)
        val mr: Matcher = pn.matcher(text)
        val list = arrayListOf<String>()
        while (mr.find()) {
            list.add(mr.group())
        }
        val strings = arrayOfNulls<String>(list.size)
        return list.toArray(strings)
    }
}