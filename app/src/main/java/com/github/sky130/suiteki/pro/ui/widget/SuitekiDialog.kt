package com.github.sky130.suiteki.pro.ui.widget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun BaseSuitekiDialog(
    state: DialogState,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties()
) {
    val visible by remember {
        state.visible
    }
    if (!visible) return
    AlertDialog(
        onDismissRequest, confirmButton, modifier, dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = containerColor,
        tonalElevation = tonalElevation,
        // Note that a button content color is provided here from the dialog's token, but in
        // most cases, TextButtons should be used for dismiss and confirm buttons.
        // TextButtons will not consume this provided content color value, and will used their
        // own defined or default colors.
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        properties = properties
    )
}

@Composable
fun SuitekiDialog(
    state: DialogState,
    title: String,
    icon: ImageVector,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    button: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) = BaseSuitekiDialog(
    state = state,
    onDismissRequest = onDismissRequest,
    confirmButton = button,
    modifier = modifier,
    title = { Text(text = title) },
    icon = { Icon(icon, title) },
    text = content
)

@Composable
fun rememberDialogState() = remember { DialogState() }

class DialogState() {
    val visible = mutableStateOf(false)

    fun show() {
        visible.value = true
    }

    fun dismiss() {
        visible.value = false
    }
}