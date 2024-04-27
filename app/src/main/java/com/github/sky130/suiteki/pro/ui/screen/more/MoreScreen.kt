package com.github.sky130.suiteki.pro.ui.screen.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.sky130.suiteki.pro.ui.theme.SuitekiTheme

@Composable
@Preview
fun MoreScreen() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
    ) {
//        AppCard("测试", Icons.Default.Info){}
//        AppCard("测试", Icons.Default.Info){}
//        AppCard("测试", Icons.Default.Info){}
    }
}

@Composable
fun AppCard(modifier:Modifier = Modifier,title: String, icon: ImageVector,onClick:()->Unit) {
    ElevatedCard(onClick,modifier=modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(15.dp)) {
            Icon(icon, contentDescription = title)
            Spacer(Modifier.height(5.dp))
            Text(title)
        }
    }
}