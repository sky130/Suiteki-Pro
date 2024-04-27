package com.github.sky130.suiteki.pro.device.miband8

import com.github.sky130.suiteki.pro.logic.ble.AbstractBleService
import com.github.sky130.suiteki.pro.logic.ble.UUIDS

class XiaomiBleService:AbstractBleService() {
    override fun installFile(
        bytes: ByteArray,
        onProgress: (Int) -> Unit,
        onAuthSuccess: () -> Unit,
        onAuthFailure: () -> Unit
    ) {

    }

    override fun write(bytes: ByteArray, uuid: UUIDS) {

    }

    override fun onHandle(bytes: ByteArray, uuid: UUIDS) {

    }
}