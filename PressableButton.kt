package com.gamecontrol.app.ui.controller

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.gamecontrol.app.model.ControllerButton
import com.gamecontrol.app.model.ControllerUiState
import com.gamecontrol.app.ui.theme.ColorOnPrimaryContainer
import com.gamecontrol.app.ui.theme.ColorOnSurface
import com.gamecontrol.app.ui.theme.ColorOutlineVariant
import com.gamecontrol.app.ui.theme.ColorPrimaryContainer
import com.gamecontrol.app.ui.theme.ColorSurfaceHigh

/**
 * A momentary button. Pressed while a finger is on it.
 * If that finger slides off, the button releases. It does not jump to a neighbor.
 */
@Composable
fun PressableButton(
    button: ControllerButton,
    state: ControllerUiState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .onSizeChanged { boxSize = it }
            .pointerInput(button) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pointerId = down.id
                    state.setButton(button, true)
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) break
                            change.consume()
                            val inside =
                                change.position.x in 0f..boxSize.width.toFloat() &&
                                    change.position.y in 0f..boxSize.height.toFloat()
                            state.setButton(button, inside)
                        }
                    } finally {
                        state.setButton(button, false)
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** Labeled rectangular control: shoulders, triggers, Share, Options, L3 and R3. */
@Composable
fun ControlButton(
    label: String,
    button: ControllerButton,
    state: ControllerUiState,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    pressedColor: Color = ColorPrimaryContainer,
    pressedTextColor: Color = ColorOnPrimaryContainer,
    showPercent: Boolean = false,
) {
    val pressed = state.pressed.contains(button)
    val textColor = if (pressed) pressedTextColor else ColorOnSurface

    PressableButton(
        button = button,
        state = state,
        modifier = modifier
            .clip(shape)
            .background(if (pressed) pressedColor else ColorSurfaceHigh)
            .border(1.dp, ColorOutlineVariant.copy(alpha = 0.5f), shape),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
            )
            if (showPercent) {
                Text(
                    text = if (pressed) "100%" else "0%",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.8f),
                )
            }
        }
    }
}
