package com.gamecontrol.app.ui.controller

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.gamecontrol.app.model.ControllerButton
import com.gamecontrol.app.model.ControllerUiState
import com.gamecontrol.app.ui.theme.ColorOnPrimaryContainer
import com.gamecontrol.app.ui.theme.ColorOnSurfaceVariant
import com.gamecontrol.app.ui.theme.ColorPrimaryContainer
import com.gamecontrol.app.ui.theme.ColorSurfaceHigh
import com.gamecontrol.app.ui.theme.ColorSurfaceHighest

/**
 * One finger controls the D-pad. Upper half presses Up, and so on.
 * Between two arms presses both directions (diagonal).
 */
@Composable
fun DPad(
    state: ControllerUiState,
    modifier: Modifier = Modifier,
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Canvas(
        modifier = modifier
            .onSizeChanged { boxSize = it }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pointerId = down.id
                    var active = directionsAt(down.position, boxSize)
                    applyDirections(state, emptySet(), active)
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) break
                            change.consume()
                            val next = directionsAt(change.position, boxSize)
                            applyDirections(state, active, next)
                            active = next
                        }
                    } finally {
                        applyDirections(state, active, emptySet())
                    }
                }
            },
    ) {
        drawDpad(state)
    }
}

private fun directionsAt(position: Offset, size: IntSize): Set<ControllerButton> {
    if (size.width == 0 || size.height == 0) return emptySet()
    val w = size.width.toFloat()
    val h = size.height.toFloat()
    if (position.x < 0f || position.y < 0f || position.x > w || position.y > h) {
        return emptySet()
    }
    val dx = (position.x - w / 2f) / (w / 2f)
    val dy = (position.y - h / 2f) / (h / 2f)
    val deadZone = 0.25f
    return buildSet {
        if (dy < -deadZone) add(ControllerButton.DPAD_UP)
        if (dy > deadZone) add(ControllerButton.DPAD_DOWN)
        if (dx < -deadZone) add(ControllerButton.DPAD_LEFT)
        if (dx > deadZone) add(ControllerButton.DPAD_RIGHT)
    }
}

private fun applyDirections(
    state: ControllerUiState,
    from: Set<ControllerButton>,
    to: Set<ControllerButton>,
) {
    (from - to).forEach { state.setButton(it, false) }
    (to - from).forEach { state.setButton(it, true) }
}

private data class DpadArm(
    val button: ControllerButton,
    val column: Int,
    val row: Int,
    val ux: Float,
    val uy: Float,
)

private val DpadArms = listOf(
    DpadArm(ControllerButton.DPAD_UP, column = 1, row = 0, ux = 0f, uy = -1f),
    DpadArm(ControllerButton.DPAD_LEFT, column = 0, row = 1, ux = -1f, uy = 0f),
    DpadArm(ControllerButton.DPAD_RIGHT, column = 2, row = 1, ux = 1f, uy = 0f),
    DpadArm(ControllerButton.DPAD_DOWN, column = 1, row = 2, ux = 0f, uy = 1f),
)

private fun DrawScope.drawDpad(state: ControllerUiState) {
    val cell = minOf(size.width, size.height) / 3f
    val gap = 3.dp.toPx()
    val corner = CornerRadius(10.dp.toPx())

    for (arm in DpadArms) {
        val pressed = state.pressed.contains(arm.button)
        val topLeft = Offset(arm.column * cell + gap / 2f, arm.row * cell + gap / 2f)
        drawRoundRect(
            color = if (pressed) ColorPrimaryContainer else ColorSurfaceHigh,
            topLeft = topLeft,
            size = Size(cell - gap, cell - gap),
            cornerRadius = corner,
        )
        drawArrow(
            center = Offset(arm.column * cell + cell / 2f, arm.row * cell + cell / 2f),
            ux = arm.ux,
            uy = arm.uy,
            arrowSize = cell * 0.22f,
            color = if (pressed) ColorOnPrimaryContainer else ColorOnSurfaceVariant,
        )
    }

    drawCircle(
        color = ColorSurfaceHighest.copy(alpha = 0.7f),
        radius = cell * 0.18f,
        center = Offset(size.width / 2f, size.height / 2f),
    )
}

private fun DrawScope.drawArrow(
    center: Offset,
    ux: Float,
    uy: Float,
    arrowSize: Float,
    color: Color,
) {
    val tip = Offset(center.x + ux * arrowSize, center.y + uy * arrowSize)
    val baseX = center.x - ux * arrowSize * 0.5f
    val baseY = center.y - uy * arrowSize * 0.5f
    val perpX = -uy * arrowSize * 0.8f
    val perpY = ux * arrowSize * 0.8f
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(baseX + perpX, baseY + perpY)
        lineTo(baseX - perpX, baseY - perpY)
        close()
    }
    drawPath(path = path, color = color)
}
