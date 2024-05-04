package com.github.sky130.suiteki.pro.screen.main.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager
import com.github.sky130.suiteki.pro.logic.database.AppDatabase
import com.github.sky130.suiteki.pro.logic.database.model.Device
import com.github.sky130.suiteki.pro.screen.main.MainGraph
import com.github.sky130.suiteki.pro.ui.widget.DialogState
import com.github.sky130.suiteki.pro.ui.widget.SuitekiScaffold
import com.github.sky130.suiteki.pro.ui.widget.SuitekiDialog
import com.github.sky130.suiteki.pro.ui.widget.SuitekiTopBar
import com.github.sky130.suiteki.pro.ui.widget.rememberDialogState
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination<MainGraph>
fun DeviceScreen() {
    val dialogState = rememberDialogState()
    SuitekiScaffold(
        fab = {
            FloatingActionButton(onClick = { dialogState.show() }) {
                Icon(Icons.Filled.Add, null)
            }
        },
        topBar = {
            SuitekiTopBar(title ="设备")
        }
    ) {
        DeviceLogDialog(dialogState)
        Column {
            val list by AppDatabase.instance.device().getList().collectAsState(initial = listOf())
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                items(list) {
                    ElevatedCard(onClick = {
                        SuitekiManager.connect(it)
                    }) {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            Text(it.name)
                            Spacer(Modifier.height(5.dp))
                            Text(it.mac)
                        }
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceLogDialog(dialogState: DialogState) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var mac by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    SuitekiDialog(
        state = dialogState,
        onDismissRequest = {},
        button = {
            Button(onClick = {
                scope.launch {
                    AppDatabase.instance.device().insert(Device(name, mac, key))
                }
                dialogState.dismiss()
            }) {
                Text(text = "确定")
            }
            OutlinedButton(onClick = { dialogState.dismiss() }) {
                Text(text = "取消")
            }
        },
        icon = Icons.Filled.Watch,
        title = "添加设备",
    ){
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TextField(value = name, onValueChange = { name = it }, label = { Text("设备名") })
            TextField(
                value = mac,
                onValueChange = { mac = it.replace("：", ":") },
                label = { Text("MAC地址") })
            TextField(value = key, onValueChange = { key = it }, label = { Text("密钥") })
        }
    }
}


@Composable
fun DeviceInstallDialog(dialogState: DialogState){

}
