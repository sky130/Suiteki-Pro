package com.github.sky130.suiteki.pro

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager
import com.github.sky130.suiteki.pro.logic.handler.CrashHandler


@SuppressLint("StaticFieldLeak")
class MainApplication : Application() {
    companion object {
        private var mContext: Context? = null
        val context get() = mContext!!

        fun openUrl(url:String){
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
        com.clj.fastble.BleManager.getInstance().enableLog(true).init(this)
        SuitekiManager.init()
        CrashHandler.instance.init(this)
    }
}