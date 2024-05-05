package com.github.sky130.suiteki.pro.screen.main.more

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LogoDev
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LogoDev
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.sky130.suiteki.pro.MainApplication
import com.github.sky130.suiteki.pro.R
import com.github.sky130.suiteki.pro.logic.database.AppDatabase
import com.github.sky130.suiteki.pro.logic.database.model.Device
import com.github.sky130.suiteki.pro.logic.handler.CrashHandler
import com.github.sky130.suiteki.pro.screen.main.MainGraph
import com.github.sky130.suiteki.pro.ui.theme.SuitekiColor
import com.github.sky130.suiteki.pro.ui.theme.SuitekiTheme
import com.github.sky130.suiteki.pro.ui.widget.BaseSuitekiDialog
import com.github.sky130.suiteki.pro.ui.widget.DialogState
import com.github.sky130.suiteki.pro.ui.widget.SuitekiDialog
import com.github.sky130.suiteki.pro.ui.widget.SuitekiScaffold
import com.github.sky130.suiteki.pro.ui.widget.SuitekiTopBar
import com.github.sky130.suiteki.pro.ui.widget.rememberDialogState
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

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
@Destination<MainGraph>
fun MoreScreen() {
    val logDialogState = rememberDialogState()
    val aboutDialogState = rememberDialogState()
    val helpDialogState = rememberDialogState()

    var hitokotoIndex by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    LaunchedEffect(hitokotoIndex) {
        text = hitokoto.random()
    }
    SuitekiScaffold(topBar = {
        SuitekiTopBar(title = "更多")

    }) {
        LogDialog(logDialogState)
        AboutDialog(aboutDialogState)
        HelpDialog(helpDialogState)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedCard(
                onClick = {
                    hitokotoIndex++
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    AppCard(Modifier, "日志", Icons.Outlined.Code) {
                        logDialogState.show()
                    }
                }

                item {
                    AppCard(Modifier, "帮助", Icons.AutoMirrored.Outlined.HelpOutline) {
                        helpDialogState.show()
                    }
                }

                item {
                    AppCard(Modifier, "设置", Icons.Outlined.Settings) {

                    }
                }

                item {
                    AppCard(Modifier, "关于", Icons.Outlined.Info) {
                        aboutDialogState.show()
                    }
                }

                item {
                    AppCard(Modifier, "更新", Icons.Outlined.Update) {
                        MainApplication.openUrl("https://akidepot.com/s/X0Jtx")
                    }
                }
            }
        }
    }
}


@Composable
fun LogDialog(state: DialogState) {
    if (!state.visible.value) return
    val logText by flow.collectAsState(initial = "Waiting")
    AlertDialog(onDismissRequest = {}, confirmButton = {
        OutlinedButton(onClick = {
            state.dismiss()
        }) {
            Text(text = "关闭")
        }
    }, icon = { Icon(Icons.Filled.Info, null) }, title = { Text(text = "日志") }, text = {
        LazyColumn {
            item {
                Text(text = logText)
            }
        }
    })
}


@Composable
fun AppCard(modifier: Modifier = Modifier, title: String, icon: ImageVector, onClick: () -> Unit) {
    ElevatedCard(onClick, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            Icon(icon, contentDescription = title)
            Spacer(Modifier.height(5.dp))
            Text(title)
        }
    }
}

@Composable
fun AboutDialog(state: DialogState) {
    BaseSuitekiDialog(state = state, onDismissRequest = {
        state.dismiss()
    }, confirmButton = {
    }, text = {
        AboutScreen()
    })
}

@Composable
fun HelpDialog(state: DialogState) {
    BaseSuitekiDialog(state = state, onDismissRequest = {
        state.dismiss()
    }, confirmButton = {
        Button(onClick = { MainApplication.openUrl("https://www.bandbbs.cn/threads/11107/") }) {
            Text(text = "更多帮助")
        }
        Button(onClick = { state.dismiss() }) {
            Text(text = "返回")
        }
    }, text = {

        Column {
            Text(text = "软件目前不适用于小白上手")
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = "蓝牙名称一定要填写完整的设备名称!!!")
        }

    })
}


@Composable
fun AboutScreen() {
    Row(
        modifier = Modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_suiteki),
            contentDescription = null,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .padding(20.dp)
                .size(23.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.width(15.dp))
        Column {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Suiteki",
                style = TextStyle(fontSize = 25.sp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = "by Sky233", style = TextStyle(fontSize = 18.sp))
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = "Thanks for Gadgetbridget and You", style = TextStyle(fontSize = 15.sp))
            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Github",
                style = TextStyle(fontSize = 15.sp), color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { MainApplication.openUrl("https://github.com/sky130/Suiteki-Pro") })

        }
    }
}

val hitokoto = listOf(
    "若汝无介意，愿作耳边墙",
    "思君无情拒，唯吾孤自哀",
    "希愿回心意，眷恋越星海",
    "欲采栀子花，赠予手心中",
    "流水长月，依恋不舍",
    "拾栀雨落，忆梦汐潮",
    "思向呼唤，尔已忘期",
    "汝之憨态，流连忘返",
    "氢氯钠钾银，氧镁钙钡锌",
    "Add! Commit! Push!",
    "喵，喵喵喵! 喵喵!?",
    "别让等待成为遗憾",
    "星垂平野阔，月是故乡明",
    "打起精神，准备出发!",
    "岂曰无衣？与子同袍。",
    "你所热爱的，就是你的生活",
    "我们的目标是星辰大海",
    "Nvidia, fuck you.",
    "Never gonna give you up",
    "出发咯！丢～～～",
    "我很可爱，v我50",
    "听着，所谓杀手",
    "我的热情，无穷无尽",
    "你是不是嫌我傻了吧唧的",
    "早上好 中午好 晚上好",
    "₍˄·͈༝·͈˄*₎◞ ̑̑Nya~",
    "Change the boring world!",
    "Ciallo～(∠・ω&lt; )⌒☆",
    "心灵美，一切都美",
    "你干嘛～哎呦～",
    "安民之道，在察其疾苦",
    "不以物喜，不以己悲",
    "水不在深，有龙则灵",
    "不日新者必日退",
    "一定要好好活着啊喵",
    "祝你快乐，喵喵",
    "这白开水怎么没味儿啊",
    "变亦变，不变亦变",
    "技术宅拯救世界",
)
