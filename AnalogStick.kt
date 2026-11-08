package com.gamecontrol.app.ui.controller

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.gamecontrol.app.ui.theme.ColorOutlineVariant
import com.gamecontrol.app.ui.theme.ColorSurfaceHighest
import com.gamecontrol.app.ui.theme.ColorSurfaceLow
import kotlin.math.sqrt

private const val KNOB_SIZE_DP = 44f
private const val KNOB_TRAVEL_DP = 28f

/** Analog stick. Reports x and y in -1..1 (y positive upward). Springs back to center. */
@Composable
fun AnalogStick(
    x: Float,
    y: Float,
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .onSizeChanged { boxSize = it }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pointerId = down.id
                    try {
                        val start = normalizeStick(down.position, boxSize)
                        onMove(start.x, start.y)
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) break
                            change.consume()
                            val p = normalizeStick(change.position, boxSize)
                            onMove(p.x, p.y)
                        }
                    } finally {
                        onMove(0f, 0f)
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            drawCircle(color = ColorSurfaceLow, radius = radius)
            drawCircle(
                color = ColorOutlineVariant.copy(alpha = 0.5f),
                radius = radius - 0.5.dp.toPx(),
                style = Stroke(width = 1.dp.toPx()),
            )
            drawLine(
                color = ColorOutlineVariant.copy(alpha = 0.3f),
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 1.dp.toPx(),
            )
            drawLine(
                color = ColorOutlineVariant.copy(alpha = 0.3f),
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = 1.dp.toPx(),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (x * KNOB_TRAVEL_DP).dp, y = (-y * KNOB_TRAVEL_DP).dp)
                .size(KNOB_SIZE_DP.dp)
                .clip(CircleShape)
                .background(ColorSurfaceHighest)
                .border(1.dp, ColorOutlineVariant.copy(alpha = 0.6f), CircleShape),
        )
    }
}

/** Converts a touch position to a stick vector in -1..1, clamped to the unit circle. */
private fun normalizeStick(position: Offset, size: IntSize): Offset {
    if (size.width == 0 || size.height == 0) return Offset.Zero
    val halfW = size.width / 2f
    val halfH = size.height / 2f
    var nx = (position.x - halfW) / halfW
    var ny = -(position.y - halfH) / halfH
    val length = sqrt(nx * nx + ny * ny)
    if (length > 1f) {
        nx /= length
        ny /= length
    }
    return Offset(nx, ny)
}
