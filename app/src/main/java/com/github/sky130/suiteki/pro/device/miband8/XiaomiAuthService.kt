package com.github.sky130.suiteki.pro.device.miband8

import android.os.Build
import com.github.sky130.suiteki.pro.logic.ble.AbstractAuthService
import com.github.sky130.suiteki.pro.logic.ble.AuthStatus
import com.github.sky130.suiteki.pro.logic.ble.BleSupport
import com.github.sky130.suiteki.pro.logic.ble.UUIDS
import com.github.sky130.suiteki.pro.proto.xiaomi.XiaomiProto
import com.github.sky130.suiteki.pro.util.BytesUtils
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.MutableStateFlow
import org.apache.commons.lang3.ArrayUtils
import org.bouncycastle.shaded.crypto.CryptoException
import org.bouncycastle.shaded.crypto.engines.AESEngine
import org.bouncycastle.shaded.crypto.modes.CCMBlockCipher
import org.bouncycastle.shaded.crypto.params.AEADParameters
import org.bouncycastle.shaded.crypto.params.KeyParameter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.Arrays
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.math.ceil

class XiaomiAuthService(val support: BleSupport) : AbstractAuthService() {

    companion object {
        const val COMMAND_TYPE: Int = 1
        const val CMD_SEND_USERID: Int = 5
        const val CMD_NONCE: Int = 26
        const val CMD_AUTH: Int = 27
    }

    override val status = MutableStateFlow(AuthStatus.Authing)
    private val secretKey = ByteArray(16)
    private val nonce = ByteArray(16)
    private val encryptionKey = ByteArray(16)
    private val decryptionKey = ByteArray(16)
    private val encryptionNonce = ByteArray(4)
    private val decryptionNonce = ByteArray(4)
    private var encryptedIndex = 1
    private var encryptionInitialized = false


    override fun startAuth(key: String) {
        System.arraycopy(
            BytesUtils.getSecretKey(key), 0, secretKey, 0, 16
        )
        SecureRandom().nextBytes(nonce)
        sendCommand(buildNonceCommand(nonce).toByteArray())
    }

    private fun handleWatchNonce(watchNonce: XiaomiProto.WatchNonce): XiaomiProto.Command? {
        val step2hmac: ByteArray = computeAuthStep3Hmac(
            secretKey, nonce, watchNonce.getNonce().toByteArray()
        )

        System.arraycopy(step2hmac, 0, decryptionKey, 0, 16)
        System.arraycopy(step2hmac, 16, encryptionKey, 0, 16)
        System.arraycopy(step2hmac, 32, decryptionNonce, 0, 4)
        System.arraycopy(step2hmac, 36, encryptionNonce, 0, 4)


        val decryptionConfirmation: ByteArray = hmacSHA256(
            decryptionKey, ArrayUtils.addAll(watchNonce.nonce.toByteArray(), *nonce)
        )
        if (!Arrays.equals(decryptionConfirmation, watchNonce.getHmac().toByteArray())) {
            return null
        }

        val authDeviceInfo: XiaomiProto.AuthDeviceInfo =
            XiaomiProto.AuthDeviceInfo.newBuilder().setUnknown1(0) // TODO ?
                .setPhoneApiLevel(Build.VERSION.SDK_INT.toFloat()).setPhoneName(Build.MODEL)
                .setUnknown3(224) // TODO ?
                // TODO region should be actual device region?
                .setRegion(Locale.getDefault().language.substring(0, 2).uppercase()).build()

        val encryptedNonces: ByteArray = hmacSHA256(
            encryptionKey, ArrayUtils.addAll(nonce, *watchNonce.nonce.toByteArray())
        )
        val encryptedDeviceInfo = encrypt(authDeviceInfo.toByteArray(), 0)
        val authStep3: XiaomiProto.AuthStep3 = XiaomiProto.AuthStep3.newBuilder()
            .setEncryptedNonces(ByteString.copyFrom(encryptedNonces))
            .setEncryptedDeviceInfo(ByteString.copyFrom(encryptedDeviceInfo)).build()

        val cmd = XiaomiProto.Command.newBuilder()
        cmd.setType(COMMAND_TYPE)
        cmd.setSubtype(CMD_AUTH)

        val auth = XiaomiProto.Auth.newBuilder()
        auth.setAuthStep3(authStep3)

        return cmd.setAuth(auth.build()).build()
    }

    fun buildNonceCommand(nonce: ByteArray?): XiaomiProto.Command {
        val phoneNonce = XiaomiProto.PhoneNonce.newBuilder()
        phoneNonce.setNonce(ByteString.copyFrom(nonce))

        val auth = XiaomiProto.Auth.newBuilder()
        auth.setPhoneNonce(phoneNonce.build())

        val command = XiaomiProto.Command.newBuilder()
        command.setType(COMMAND_TYPE)
        command.setSubtype(CMD_NONCE)
        command.setAuth(auth.build())
        return command.build()
    }


    fun computeAuthStep3Hmac(
        secretKey: ByteArray?, phoneNonce: ByteArray?, watchNonce: ByteArray
    ): ByteArray {
        val miwearAuthBytes = "miwear-auth".toByteArray()

        val mac: Mac
        try {
            mac = Mac.getInstance("HmacSHA256")
            // Compute the actual key and re-initialize the mac
            mac.init(SecretKeySpec(ArrayUtils.addAll(phoneNonce, *watchNonce), "HmacSHA256"))
            val hmacKeyBytes = mac.doFinal(secretKey)
            val key = SecretKeySpec(hmacKeyBytes, "HmacSHA256")
            mac.init(key)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException("Failed to initialize hmac for auth step 2", e)
        } catch (e: InvalidKeyException) {
            throw IllegalStateException("Failed to initialize hmac for auth step 2", e)
        }

        val output = ByteArray(64)
        var tmp = ByteArray(0)
        var b: Byte = 1
        var i = 0
        while (i < output.size) {
            mac.update(tmp)
            mac.update(miwearAuthBytes)
            mac.update(b)
            tmp = mac.doFinal()
            var j = 0
            while (j < tmp.size && i < output.size) {
                output[i] = tmp[j]
                j++
                i++
            }
            b++
        }
        return output
    }

    fun hmacSHA256(key: ByteArray?, input: ByteArray): ByteArray {
        try {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(key, "HmacSHA256"))
            return mac.doFinal(input)
        } catch (e: Exception) {
            throw java.lang.RuntimeException("Failed to hmac", e)
        }
    }

    fun encrypt(arr: ByteArray, i: Int): ByteArray {
        val packetNonce: ByteBuffer =
            ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN).put(encryptionNonce).putInt(0)
                .putInt(i)

        try {
            return encrypt(encryptionKey, packetNonce.array(), arr)
        } catch (e: CryptoException) {
            throw RuntimeException("failed to encrypt", e)
        }
    }

    fun decrypt(arr: ByteArray): ByteArray {
        val packetNonce = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
        packetNonce.put(decryptionNonce)
        packetNonce.putInt(0)
        packetNonce.putInt(0)

        try {
            return decrypt(decryptionKey, packetNonce.array(), arr)
        } catch (e: CryptoException) {
            throw java.lang.RuntimeException("failed to decrypt", e)
        }
    }

    fun encrypt(key: ByteArray?, nonce: ByteArray?, payload: ByteArray): ByteArray {
        val cipher: CCMBlockCipher = createBlockCipher(true, SecretKeySpec(key, "AES"), nonce)
        val out = ByteArray(cipher.getOutputSize(payload.size))
        val outBytes: Int = cipher.processBytes(payload, 0, payload.size, out, 0)
        cipher.doFinal(out, outBytes)
        return out
    }

    fun decrypt(
        key: ByteArray?, nonce: ByteArray?, encryptedPayload: ByteArray
    ): ByteArray {
        val cipher: CCMBlockCipher = createBlockCipher(false, SecretKeySpec(key, "AES"), nonce)
        val decrypted = ByteArray(cipher.getOutputSize(encryptedPayload.size))
        cipher.doFinal(
            decrypted, cipher.processBytes(encryptedPayload, 0, encryptedPayload.size, decrypted, 0)
        )
        return decrypted
    }

    fun createBlockCipher(
        forEncrypt: Boolean, secretKey: SecretKey, nonce: ByteArray?
    ): CCMBlockCipher {
        val aesFastEngine = AESEngine()
        aesFastEngine.init(forEncrypt, KeyParameter(secretKey.encoded))
        val blockCipher = CCMBlockCipher(aesFastEngine)
        blockCipher.init(
            forEncrypt, AEADParameters(KeyParameter(secretKey.encoded), 32, nonce, null)
        )
        return blockCipher
    }

    fun handleCommand(cmd: XiaomiProto.Command) {
        require(cmd.type === COMMAND_TYPE) { "Not an auth command" }

        when (cmd.subtype) {
            CMD_NONCE -> {
                val command = handleWatchNonce(cmd.auth.watchNonce) ?: return
                sendCommand( command.toByteArray())
            }

            CMD_AUTH, CMD_SEND_USERID -> {
                if (cmd.subtype === CMD_AUTH || cmd.auth.status === 1) {
                    encryptionInitialized = (cmd.subtype === CMD_AUTH)
                    status.value = AuthStatus.Success
                } else {
                    status.value = AuthStatus.Failure
                }
            }

            else -> Unit
        }
    }

    override fun write(bytes: ByteArray, uuid: UUIDS) {
    }

    override fun onHandle(bytes: ByteArray, uuid: UUIDS) {
        if (uuid.second == XiaomiService.UUID_CHARACTERISTIC_COMMAND_READ)
            XiaomiProto.Command.parseFrom(bytes)
    }

    fun sendCommand(data: ByteArray) {
        val currentData = encrypt(data, encryptedIndex).let {
            ByteBuffer.allocate(2 + it.size).order(ByteOrder.LITTLE_ENDIAN)
                .putShort(encryptedIndex++.toShort())
                .put(it)
                .array()
        }.let {
            ByteBuffer.allocate(6).order(ByteOrder.LITTLE_ENDIAN).putShort(0.toShort())
                .put(0.toByte()).put(1.toByte())
                .putShort(
                    ceil((it.size / (244 - 2).toFloat()).toDouble()).toInt().toShort()
                ).array()
        }
        support.write(
            currentData,
            XiaomiService.UUID_SERVICE to XiaomiService.UUID_CHARACTERISTIC_COMMAND_WRITE
        )
    }
}