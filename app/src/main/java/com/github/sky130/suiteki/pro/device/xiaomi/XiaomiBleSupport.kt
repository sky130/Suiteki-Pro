package com.github.sky130.suiteki.pro.device.xiaomi

import android.bluetooth.BluetoothGatt
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.github.sky130.suiteki.pro.proto.xiaomi.XiaomiProto
import com.github.sky130.suiteki.pro.proto.xiaomi.XiaomiProto.Command
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.ceil

class XiaomiBleSupport(device: XiaomiDevice) : XiaomiAbstractSupport(device) {

    override fun sendCommand(command: Command) {
        device.authService.apply {
            val currentData = encrypt(command.toByteArray(), encryptedIndex).let {
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
            manager.write(
                bleDevice,
                XiaomiService.UUID_SERVICE,
                XiaomiService.UUID_CHARACTERISTIC_COMMAND_WRITE,
                currentData,
                object : BleWriteCallback() {
                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {

                    }

                    override fun onWriteFailure(exception: BleException?) {

                    }
                }
            )
        }
    }


    override fun sendDataChunk(data: ByteArray, onSend: () -> Unit) {
        manager.write(
            bleDevice,
            XiaomiService.UUID_SERVICE,
            XiaomiService.UUID_CHARACTERISTIC_DATA_UPLOAD,
            data,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    onSend()
                }

                override fun onWriteFailure(exception: BleException?) {

                }
            }
        )
    }

    private lateinit var bleDevice: BleDevice
    private val manager get() = BleManager.getInstance()
    private val job = Job()
    private val scope = CoroutineScope(job)
    private val uuids = mapOf(
        XiaomiService.UUID_SERVICE to listOf(XiaomiService.UUID_CHARACTERISTIC_COMMAND_READ),
    )

    override fun start() {
        manager.connect(device.mac, this)
    }

    override fun onStartConnect() {}

    override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {}

    override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
        scope.launch {
            this@XiaomiBleSupport.bleDevice = bleDevice
            for ((service, characteristics) in uuids) {
                for (i in characteristics) {
                    manager.notify(bleDevice, service, i, object : BleNotifyCallback() {
                        override fun onNotifySuccess() {
                        }

                        override fun onNotifyFailure(exception: BleException) {
                        }

                        override fun onCharacteristicChanged(data: ByteArray) {
                            device.apply {
                                handleCommand(
                                    XiaomiProto.Command.parseFrom(
                                        if (isEncrypted) {
                                            authService.decrypt(data)
                                        } else {
                                            data
                                        }
                                    )
                                )
                            }
                        }

                    })
                    delay(250)
                }
            }
            device.auth()
        }
    }

    override fun onDisConnected(
        isActiveDisConnected: Boolean, bleDevice: BleDevice, gatt: BluetoothGatt, status: Int
    ) {
        device.onDisconnect()
    }


    fun write(data: ByteArray, service: String, characteristics: String) {
        manager.write(bleDevice, service, characteristics, data, object : BleWriteCallback() {
            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {

            }

            override fun onWriteFailure(exception: BleException?) {

            }
        })
    }

    fun disconnect() {
        manager.disconnect(bleDevice)
    }
}