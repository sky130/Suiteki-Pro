/*  Copyright (C) 2017-2024 Andreas Shimokawa, Arjan Schrijver, Carsten
    Pfeiffer, Daniel Dakhno, Daniele Gobbetti, João Paulo Barraca, José Rebelo,
    Nephiel, Roi Greenberg, Taavi Eomäe, Zhong Jianxin

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package com.github.sky130.suiteki.pro.util

import org.apache.commons.lang3.ArrayUtils
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import kotlin.math.min

object StringUtils {
    fun truncate(s: String?, maxLength: Int): String {
        if (s == null) {
            return ""
        }

        val length = min(s.length.toDouble(), maxLength.toDouble()).toInt()
        if (length < 0) {
            return ""
        }

        return s.substring(0, length)
    }

    /**
     * Truncate a string to a certain maximum number of bytes, assuming UTF-8 encoding.
     * Does not include the null terminator. Due to multi-byte characters, it's possible
     * that the resulting array is smaller than len, but never larger.
     */
    fun truncateToBytes(s: String, len: Int): ByteArray {
        if (isNullOrEmpty(s)) {
            return byteArrayOf()
        }

        var i = 0
        while (++i < s.length) {
            val subString = s.substring(0, i + 1)
            if (subString.toByteArray(StandardCharsets.UTF_8).size > len) {
                break
            }
        }

        return s.substring(0, i).toByteArray(StandardCharsets.UTF_8)
    }

    fun utf8ByteLength(string: String?, length: Int): Int {
        if (string == null) {
            return 0
        }
        val outBuf = ByteBuffer.allocate(length)
        val inBuf = CharBuffer.wrap(string.toCharArray())
        StandardCharsets.UTF_8.newEncoder().encode(inBuf, outBuf, true)
        return outBuf.position()
    }

    @JvmOverloads
    fun pad(s: String?, length: Int, padChar: Char = ' '): String {
        var s = s
        val sBuilder = StringBuilder(s)
        while (sBuilder.length < length) {
            sBuilder.append(padChar)
        }
        s = sBuilder.toString()
        return s
    }

    /**
     * Joins the given elements and adds a separator between each element in the resulting string.
     * There will be no separator at the start or end of the string. There will be no consecutive
     * separators (even in case an element is null or empty).
     * @param separator the separator string
     * @param elements the elements to concatenate to a new string
     * @return the joined strings, separated by the separator
     */
    fun join(separator: String?, vararg elements: String?): StringBuilder {
        val builder = StringBuilder()
        if (elements == null) {
            return builder
        }
        var hasAdded = false
        for (element in elements) {
            if (element != null && element.length > 0) {
                if (hasAdded) {
                    builder.append(separator)
                }
                builder.append(element)
                hasAdded = true
            }
        }
        return builder
    }

    fun getFirstOf(first: String, second: String): String {
        if (first != null && first.length > 0) {
            return first
        }
        if (second != null) {
            return second
        }
        return ""
    }

    fun isNullOrEmpty(string: String?): Boolean {
        return string == null || string.isEmpty()
    }

    fun isEmpty(string: String?): Boolean {
        return string != null && string.length == 0
    }

    fun ensureNotNull(message: String?): String {
        if (message != null) {
            return message
        }
        return ""
    }

    fun terminateNull(input: String?): String {
        if (input == null || input.length == 0) {
            return String(byteArrayOf(0.toByte()))
        }
        val lastChar = input[input.length - 1]
        if (lastChar.code == 0) return input

        val newArray = ByteArray(input.toByteArray().size + 1)
        System.arraycopy(input.toByteArray(), 0, newArray, 0, input.toByteArray().size)

        newArray[newArray.size - 1] = 0

        return String(newArray)
    }

    fun untilNullTerminator(bytes: ByteArray, startOffset: Int): String? {
        for (i in startOffset until bytes.size) {
            if (bytes[i].toInt() == 0) {
                return String(ArrayUtils.subarray(bytes, startOffset, i))
            }
        }

        return null
    }

    fun untilNullTerminator(buf: ByteBuffer): String? {
        val baos = ByteArrayOutputStream()

        while (buf.position() < buf.limit()) {
            val b = buf.get()

            if (b.toInt() == 0) {
                return baos.toString()
            }

            baos.write(b.toInt())
        }

        return null
    }


    /**
     * Creates a shortened version of an Android package name by using only the first
     * character of every non-last part of the package name.
     * Example: "nodomain.freeyourgadget.gadgetbridge" is shortened to "n.f.gadgetbridge"
     * @param packageName the original package name
     * @return the shortened package name
     */
    fun shortenPackageName(packageName: String): String {
        val parts = packageName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val result = StringBuilder()
        for (index in parts.indices) {
            if (index == parts.size - 1) {
                result.append(parts[index])
                break
            }
            result.append(parts[index][0]).append(".")
        }
        return result.toString()
    }
}
