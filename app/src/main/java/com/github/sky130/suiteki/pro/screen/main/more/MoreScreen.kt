package com.github.sky130.suiteki.pro.screen.main.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.sky130.suiteki.pro.logic.database.AppDatabase
import com.github.sky130.suiteki.pro.logic.database.model.Device
import com.github.sky130.suiteki.pro.logic.handler.CrashHandler
import com.github.sky130.suiteki.pro.screen.main.MainGraph
import com.github.sky130.suiteki.pro.ui.theme.SuitekiTheme
import com.github.sky130.suiteki.pro.ui.widget.SuitekiScaffold
import com.github.sky130.suiteki.pro.ui.widget.SuitekiTopBar
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File

val flow = flow {
    File(CrashHandler.PATH).apply {
        if (isDirectory) {
            val list = listFiles() ?: return@apply
            list.sortedBy { it.nameWithoutExtension }
            emit(list.last().readText())
        }
    }
}.flowOn(Dispatchers.IO)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination<MainGraph>
fun MoreScreen() {
    SuitekiScaffold(topBar = {
        SuitekiTopBar(title ="更多")

    }) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            val visible = remember {
                mutableStateOf(false)
            }
            LogDialog(visible)
            AppCard(Modifier,"日志", Icons.Default.Info){
                visible.value = true
            }
        }
    }
}


@Composable
fun LogDialog(visible: MutableState<Boolean>) {
    var enable by visible
    if (!enable) return
    val logText by flow.collectAsState(initial = "Waiting")
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            OutlinedButton(onClick = {
                enable = false
            }) {
                Text(text = "关闭")
            }
        },
        icon = { Icon(Icons.Filled.Info, null) },
        title = { Text(text = "日志") },
        text = {
            LazyColumn {
                item{
                    Text(text = logText)
                }
            }
        }
    )
}



@Composable
fun AppCard(modifier:Modifier = Modifier,title: String, icon: ImageVector,onClick:()->Unit) {
    ElevatedCard(onClick,modifier=modifier) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)) {
            Icon(icon, contentDescription = title)
            Spacer(Modifier.height(5.dp))
            Text(title)
        }
    }
}