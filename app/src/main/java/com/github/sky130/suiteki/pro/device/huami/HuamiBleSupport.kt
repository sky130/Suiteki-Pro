package com.github.sky130.suiteki.pro.device.huami

import android.bluetooth.BluetoothGatt
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HuamiBleSupport(private val device: HuamiDevice) : BleGattCallback() {
    private lateinit var bleDevice: BleDevice
    private val manager get() = BleManager.getInstance()
    private val job = Job()
    private val scope = CoroutineScope(job)

    fun start(){
        manager.connect(device.mac, this)
    }

    override fun onStartConnect() {}

    override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {}

    override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
        scope.launch {
            this@HuamiBleSupport.bleDevice = bleDevice
            for ((service, characteristics) in device.uuids) {
                for (i in characteristics) {
                    manager.notify(bleDevice, service, i, object : BleNotifyCallback() {
                        override fun onNotifySuccess() {
                        }

                        override fun onNotifyFailure(exception: BleException) {
                        }

                        override fun onCharacteristicChanged(data: ByteArray) {
                            device.handleChannel(data, service, i)
                        }

                    })
                    delay(250)
                }
            }
            device.auth()
        }
    }

    override fun onDisConnected(
        isActiveDisConnected: Boolean, device: BleDevice, gatt: BluetoothGatt, status: Int
    ) {

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