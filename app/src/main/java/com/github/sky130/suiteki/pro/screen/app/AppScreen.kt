package com.github.sky130.suiteki.pro.screen.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager
import com.github.sky130.suiteki.pro.screen.main.home.DisconnectScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination<RootGraph>
fun AppScreen(navigator: DestinationsNavigator) {
    val device by SuitekiManager.bleDevice.collectAsState()

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { device?.requestAppList() }) {
            Icon(Icons.Default.Refresh, contentDescription = null)
        }
    }, topBar = {
        TopAppBar(title = { Text("应用管理") }, navigationIcon = {
            IconButton(onClick = { navigator.popBackStack() }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
            }
        })
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            if (device == null) {
                DisconnectScreen()
            } else {
                val ble by SuitekiManager.bleDevice.collectAsState()
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(device!!.appList) {
                        ElevatedCard(onClick = {

                        }, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth()
                            ) {
                                Column {
                                    Text(it.name)
                                    Spacer(Modifier.height(5.dp))
                                    Text(it.id)
                                }

                                Row(
                                    modifier = Modifier.align(
                                        Alignment.CenterEnd
                                    ), horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {

                                    IconButton(
                                        onClick = { ble?.deleteApp(it.id) },
                                    ) {
                                        Icon(Icons.Default.Delete, null)
                                    }
                                    IconButton(
                                        onClick = { ble?.launchApp(it.id) },
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Launch, null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}