/*  Copyright (C) 2015-2024 Andreas Shimokawa, Carsten Pfeiffer, Damien
    Gaignon, Daniele Gobbetti, José Rebelo, Petr Vaněk

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

import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.zip.CRC32
import kotlin.math.max

object CheckSums {
    fun getCRC8(seq: ByteArray): Int {
        var len = seq.size
        var i = 0
        var crc: Byte = 0x00

        while (len-- > 0) {
            var extract = seq[i++]
            for (tempI in 8 downTo 1) {
                var sum = ((crc.toInt() and 0xff) xor (extract.toInt() and 0xff)).toByte()
                sum = ((sum.toInt() and 0xff) and 0x01).toByte()
                crc = ((crc.toInt() and 0xff) ushr 1).toByte()
                if (sum.toInt() != 0) {
                    crc = ((crc.toInt() and 0xff) xor 0x8c).toByte()
                }
                extract = ((extract.toInt() and 0xff) ushr 1).toByte()
            }
        }
        return (crc.toInt() and 0xff)
    }

    //thanks http://stackoverflow.com/questions/13209364/convert-c-crc16-to-java-crc16
    fun getCRC16(seq: ByteArray): Int {
        return getCRC16(seq, 0xFFFF)
    }

    fun getCRC16(seq: ByteArray, crc: Int): Int {
        var crc = crc
        for (b in seq) {
            crc = ((crc ushr 8) or (crc shl 8)) and 0xffff
            crc = crc xor (b.toInt() and 0xff) //byte to int, trunc sign
            crc = crc xor ((crc and 0xff) shr 4)
            crc = crc xor ((crc shl 12) and 0xffff)
            crc = crc xor (((crc and 0xFF) shl 5) and 0xffff)
        }
        crc = crc and 0xffff
        return crc
    }

    fun getCRC16ansi(seq: ByteArray): Int {
        var crc = 0xffff
        val polynomial = 0xA001

        for (i in seq.indices) {
            crc = crc xor (seq[i].toInt() and 0xFF)
            for (j in 0..7) {
                crc = if ((crc and 1) != 0) {
                    crc ushr 1 xor polynomial
                } else {
                    crc ushr 1
                }
            }
        }

        return crc and 0xFFFF
    }

    fun getCRC32(seq: ByteArray?): Int {
        val crc = CRC32()
        crc.update(seq)
        return crc.value.toInt()
    }

    fun getCRC32(seq: ByteArray?, offset: Int, length: Int): Int {
        val crc = CRC32()
        crc.update(seq, offset, length)
        return crc.value.toInt()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        require(!(args == null || args.size == 0)) { "Pass the files to be checksummed as arguments" }
        for (name in args) {
            FileInputStream(name).use { `in` ->
                val bytes = readAll(`in`, (1000 * 1000).toLong())
                println(name + " : " + getCRC16(bytes))
            }
        }
    }

    // copy&paste of FileUtils.readAll() to have it free from Android dependencies
    @Throws(IOException::class)
    private fun readAll(`in`: InputStream, maxLen: Long): ByteArray {
        val out = ByteArrayOutputStream(
            max(8192.0, `in`.available().toDouble()).toInt()
        )
        val buf = ByteArray(8192)
        var read: Int
        var totalRead: Long = 0
        while ((`in`.read(buf).also { read = it }) > 0) {
            out.write(buf, 0, read)
            totalRead += read.toLong()
            if (totalRead > maxLen) {
                throw IOException("Too much data to read into memory. Got already $totalRead")
            }
        }
        return out.toByteArray()
    }

    // https://github.com/ThePBone/GalaxyBudsClient/blob/master/GalaxyBudsClient/Utils/CRC16.cs
    private val Crc16Tab = intArrayOf(
        0, 4129, 8258, 12387, 16516, 20645, 24774, 28903, 33032, 37161, 41290,
        45419, 49548, 53677, 57806, 61935, 4657, 528, 12915, 8786, 21173, 17044, 29431, 25302,
        37689, 33560, 45947, 41818, 54205, 50076, 62463, 58334, 9314, 13379, 1056, 5121, 25830,
        29895, 17572, 21637, 42346, 46411, 34088, 38153, 58862, 62927, 50604, 54669, 13907,
        9842, 5649, 1584, 30423, 26358, 22165, 18100, 46939, 42874, 38681, 34616, 63455, 59390,
        55197, 51132, 18628, 22757, 26758, 30887, 2112, 6241, 10242, 14371, 51660, 55789,
        59790, 63919, 35144, 39273, 43274, 47403, 23285, 19156, 31415, 27286, 6769, 2640,
        14899, 10770, 56317, 52188, 64447, 60318, 39801, 35672, 47931, 43802, 27814, 31879,
        19684, 23749, 11298, 15363, 3168, 7233, 60846, 64911, 52716, 56781, 44330, 48395,
        36200, 40265, 32407, 28342, 24277, 20212, 15891, 11826, 7761, 3696, 65439, 61374,
        57309, 53244, 48923, 44858, 40793, 36728, 37256, 33193, 45514, 41451, 53516, 49453,
        61774, 57711, 4224, 161, 12482, 8419, 20484, 16421, 28742, 24679, 33721, 37784, 41979,
        46042, 49981, 54044, 58239, 62302, 689, 4752, 8947, 13010, 16949, 21012, 25207, 29270,
        46570, 42443, 38312, 34185, 62830, 58703, 54572, 50445, 13538, 9411, 5280, 1153, 29798,
        25671, 21540, 17413, 42971, 47098, 34713, 38840, 59231, 63358, 50973, 55100, 9939,
        14066, 1681, 5808, 26199, 30326, 17941, 22068, 55628, 51565, 63758, 59695, 39368,
        35305, 47498, 43435, 22596, 18533, 30726, 26663, 6336, 2273, 14466, 10403, 52093,
        56156, 60223, 64286, 35833, 39896, 43963, 48026, 19061, 23124, 27191, 31254, 2801,
        6864, 10931, 14994, 64814, 60687, 56684, 52557, 48554, 44427, 40424, 36297, 31782,
        27655, 23652, 19525, 15522, 11395, 7392, 3265, 61215, 65342, 53085, 57212, 44955,
        49082, 36825, 40952, 28183, 32310, 20053, 24180, 11923, 16050, 3793, 7920
    )

    // // https://github.com/ThePBone/GalaxyBudsClient/blob/master/GalaxyBudsClient/Utils/CRC16.cs
    fun crc16_ccitt(data: ByteArray): Int {
        var i2 = 0
        for (i3 in data.indices) i2 =
            Crc16Tab[(i2 shr 8) xor data[i3].toInt() and 255] xor (i2 shl 8)

        return 65535 and i2
    }

    fun md5(data: ByteArray?): ByteArray? {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            return null
        }
        md.update(data)
        return md.digest()
    }
}
