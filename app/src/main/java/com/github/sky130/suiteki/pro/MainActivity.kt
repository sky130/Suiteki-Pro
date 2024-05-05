package com.github.sky130.suiteki.pro

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.sky130.suiteki.pro.basic.SuitekiActivity
import com.github.sky130.suiteki.pro.ui.theme.SuitekiTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import dev.shreyaspatil.permissionFlow.utils.launch
import dev.shreyaspatil.permissionflow.compose.rememberMultiplePermissionState
import dev.shreyaspatil.permissionflow.compose.rememberPermissionFlowRequestLauncher
import kotlinx.coroutines.delay


class MainActivity : SuitekiActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    @Preview
    override fun Content() {
        SuitekiTheme {
            val permissionLauncher = rememberPermissionFlowRequestLauncher()
            val list = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } else {
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            }
            val state by rememberMultiplePermissionState(*list)
            LaunchedEffect(Unit) {
                requestExternalStorage()
            }
            if (state.allGranted) {
                if (isExternalStorageState) {
                    DestinationsNavHost(navGraph = NavGraphs.root)
                }
            } else {
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.background))
                LaunchedEffect(Unit) {
                    delay(100)
                    permissionLauncher.launch(*list)
                }
                return@SuitekiTheme
            }
        }
    }

    private var isExternalStorageState by mutableStateOf(false)

    override fun onResume() {
        super.onResume()
        requestExternalStorage()
    }

    private fun requestExternalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.setData(Uri.parse("package:$packageName"))
                startActivity(intent)
                isExternalStorageState = false
            } else {
                isExternalStorageState = true
            }
        }
        isExternalStorageState = true
    }

    override fun init() {

    }

}