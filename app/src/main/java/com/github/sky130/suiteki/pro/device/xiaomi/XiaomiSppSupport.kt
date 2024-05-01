package com.github.sky130.suiteki.pro.device.xiaomi

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.github.sky130.suiteki.pro.device.huami.HuamiService.BASE_UUID
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiSppPacket.Companion.CHANNEL_PROTO_RX
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiSppPacket.Companion.PACKET_PREAMBLE
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager
import com.github.sky130.suiteki.pro.proto.xiaomi.XiaomiProto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.String
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.Volatile

class XiaomiSppSupport(device: XiaomiDevice) : XiaomiAbstractSupport(device), XiaomiChannelHandler {
    private val adapter = BluetoothAdapter.getDefaultAdapter()
    lateinit var dev: BluetoothDevice
    private val frameCounter = AtomicInteger(0)
    private val encryptionCounter = AtomicInteger(0)
    private val serviceUUID = UUID.fromString(String.format(BASE_UUID, "1101"))
    private lateinit var socket: BluetoothSocket
    private val scope get() = device.scope

    @Volatile
    private var mDisposed = false
    private val outputStream get() = socket.outputStream
    private val inputStream get() = socket.inputStream
    private val mChannelHandlers = HashMap<Int, XiaomiChannelHandler>()

    init {
        mChannelHandlers[CHANNEL_PROTO_RX] = this
    }


    override fun handle(payload: ByteArray?) {
        payload?.let {
            try {
                device.handleCommand(XiaomiProto.Command.parseFrom(it))
            } catch (e: Exception) {
                return
            }
        }
    }


    val buffer: ByteArrayOutputStream = ByteArrayOutputStream()

    private suspend fun onSocketRead(data: ByteArray) {
        SuitekiManager.log("onSocketRead", data)
        try {
            buffer.write(data)
        } catch (_: IOException) {
        }
        processBuffer()
    }

    private fun findNextPossiblePreamble(haystack: ByteArray): Int {
        var i = 1
        while (i + 2 < haystack.size) {
            // check if first byte matches
            if (haystack[i] == PACKET_PREAMBLE[0]) {
                return i
            }
            i++
        }

        // did not find preamble
        return -1
    }

    private suspend fun processBuffer() {
        // wait until at least an empty packet is in the buffer
        while (buffer.size() >= 11) {
            // start preamble compare
            val bufferState = buffer.toByteArray()
            val headerBuffer = ByteBuffer.wrap(bufferState, 0, 7).order(ByteOrder.LITTLE_ENDIAN)
            val preamble = ByteArray(PACKET_PREAMBLE.size)
            headerBuffer[preamble]

            if (!Arrays.equals(PACKET_PREAMBLE, preamble)) {
                val preambleOffset: Int = findNextPossiblePreamble(bufferState)

                if (preambleOffset == -1) {
                    buffer.reset()
                } else {
                    val remaining = ByteArray(bufferState.size - preambleOffset)
                    System.arraycopy(bufferState, preambleOffset, remaining, 0, remaining.size)
                    buffer.reset()
                    try {
                        buffer.write(remaining)
                    } catch (_: IOException) {
                    }
                }

                // continue processing at beginning of new buffer
                continue
            }

            headerBuffer.getShort() // skip flags and channel ID
            val payloadSize = headerBuffer.getShort().toInt() and 0xffff
            val packetSize = payloadSize + 8 // payload size includes payload header

            if (bufferState.size < packetSize) {
                return
            }
            val receivedPacket = XiaomiSppPacket.decode(bufferState) // remaining bytes unaffected

            onPacketReceived(receivedPacket)

            // extract remaining bytes from buffer
            val remaining = ByteArray(bufferState.size - packetSize)
            System.arraycopy(bufferState, packetSize, remaining, 0, remaining.size)

            buffer.reset()

            try {
                buffer.write(remaining)
            } catch (_: IOException) {
            }
        }
    }

    private suspend fun onPacketReceived(packet: XiaomiSppPacket?) {
        if (packet == null) {
            return
        }
        var payload: ByteArray = packet.payload!!

        if (packet.dataType == 1) {
            payload = device.authService.decrypt(payload)
        }

        val channel: Int = packet.channel
        mChannelHandlers[channel]?.apply {
            withContext(Dispatchers.Main) {
                handle(payload)
            }
        }
    }

    fun dispose() {
        if (mDisposed) {
            return
        }
        mDisposed = true
    }

    @SuppressLint("MissingPermission")
    override fun start() {
        SuitekiManager.log("onStartConnect")
        scope.launch(Dispatchers.IO) {
            adapter.cancelDiscovery()
            dev = adapter.getRemoteDevice(device.mac)
            socket = dev.createRfcommSocketToServiceRecord(serviceUUID)
            try {
                socket.connect()
                flow {
                    val buffer = ByteArray(1024)
                    var nRead: Int
                    while (!mDisposed) {
                        try {
                            inputStream?.let {
                                nRead = it.read(buffer)
                                if (nRead == -1) {
                                    throw IOException("End of stream")
                                }
                                emit(buffer.copyOf(nRead))
                            }
                        } catch (ex: IOException) {
                            break
                        }
                    }
                }.flowOn(Dispatchers.IO).onEach {
                    onSocketRead(it)
                }.launchIn(scope)
                withContext(Dispatchers.Main) {
                    device.auth()
                }
            } catch (e: Exception) {
                SuitekiManager.log(e.message.toString(),e.stackTrace.joinToString())
                device.onDisconnect()
            }
        }
    }

    override fun sendCommand(command: XiaomiProto.Command) {
        scope.launch(Dispatchers.IO) {
            val packet =
                XiaomiSppPacket.fromXiaomiCommand(command, frameCounter.getAndIncrement(), false)
            outputStream?.let { stream ->
                packet.encode(device.authService, encryptionCounter).let {
                    SuitekiManager.log("sendCommand", it)
                    stream.write(it)
                    stream.flush()
                }
            }
        }
    }

}