package com.github.sky130.suiteki.pro

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager

@SuppressLint("StaticFieldLeak")
class MainApplication : Application() {
    companion object {
        private var mContext: Context? = null
        val context get() = mContext!!
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
        com.clj.fastble.BleManager.getInstance().enableLog(true).init(this)
        SuitekiManager.init()
        "CA:2B:B0:DD:5B:31"
        "Xiaomi Smart Band 7 5B31"
        "3067bf322963fcbd0c931b4e5f174933"
    }
}