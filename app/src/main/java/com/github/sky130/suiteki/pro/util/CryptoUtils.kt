package com.github.sky130.suiteki.pro.util

import android.annotation.SuppressLint
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    @Throws(
        InvalidKeyException::class,
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )
    fun encryptAES(value: ByteArray?, secretKey: ByteArray?): ByteArray {
        @SuppressLint("GetInstance") val ecipher = Cipher.getInstance("AES/ECB/NoPadding")
        val newKey = SecretKeySpec(secretKey, "AES")
        ecipher.init(Cipher.ENCRYPT_MODE, newKey)
        return ecipher.doFinal(value)
    }

    @Throws(
        InvalidKeyException::class,
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )
    fun decryptAES(value: ByteArray?, secretKey: ByteArray?): ByteArray {
        @SuppressLint("GetInstance") val ecipher = Cipher.getInstance("AES/ECB/NoPadding")
        val newKey = SecretKeySpec(secretKey, "AES")
        ecipher.init(Cipher.DECRYPT_MODE, newKey)
        return ecipher.doFinal(value)
    }
}