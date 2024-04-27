package com.github.sky130.suiteki.pro.basic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable

abstract class SuitekiActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        enableEdgeToEdge()
        setContent {
            Content()
        }
    }

    @Composable
    abstract fun Content()

    open fun init(){}

}