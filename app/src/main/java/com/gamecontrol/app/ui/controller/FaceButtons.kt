package com.gamecontrol.app.ui.controller

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.gamecontrol.app.model.ControllerButton
import com.gamecontrol.app.model.ControllerUiState
import com.gamecontrol.app.ui.theme.ColorError
import com.gamecontrol.app.ui.theme.ColorOutlineVariant
import com.gamecontrol.app.ui.theme.ColorPrimary
import com.gamecontrol.app.ui.theme.ColorSecondary
import com.gamecontrol.app.ui.theme.ColorSurfaceBase
import com.gamecontrol.app.ui.theme.ColorSurfaceHigh
import com.gamecontrol.app.ui.theme.ColorTertiary

/** Four face buttons in a diamond: triangle green, square violet, circle red, cross blue. */
@Composable
fun FaceButtons(
    state: ControllerUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FaceButton(ControllerButton.TRIANGLE, state, ColorTertiary)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FaceButton(ControllerButton.SQUARE, state, ColorPrimary)
            Spacer(Modifier.size(48.dp))
            FaceButton(ControllerButton.CIRCLE, state, ColorError)
        }
        FaceButton(ControllerButton.CROSS, state, ColorSecondary)
    }
}

@Composable
private fun FaceButton(
    button: ControllerButton,
    state: ControllerUiState,
    accent: Color,
) {
    val pressed = state.pressed.contains(button)
    PressableButton(
        button = button,
        state = state,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (pressed) accent else ColorSurfaceHigh)
            .border(1.dp, ColorOutlineVariant.copy(alpha = 0.5f), CircleShape),
    ) {
        FaceGlyph(button = button, color = if (pressed) ColorSurfaceBase else accent)
    }
}

@Composable
private fun FaceGlyph(button: ControllerButton, color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = Stroke(
            width = 2.5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val inset = stroke.width / 2f
        val w = size.width
        val h = size.height

        when (button) {
            ControllerButton.TRIANGLE -> drawPath(
                path = Path().apply {
                    moveTo(w / 2f, inset)
                    lineTo(w - inset, h - inset)
                    lineTo(inset, h - inset)
                    close()
                },
                color = color,
                style = stroke,
            )
            ControllerButton.SQUARE -> drawRoundRect(
                color = color,
                topLeft = Offset(inset, inset),
                size = Size(w - 2 * inset, h - 2 * inset),
                cornerRadius = CornerRadius(3.dp.toPx()),
                style = stroke,
            )
            ControllerButton.CIRCLE -> drawCircle(
                color = color,
                radius = w / 2f - inset,
                style = stroke,
            )
            ControllerButton.CROSS -> {
                drawLine(
                    color = color,
                    start = Offset(inset, inset),
                    end = Offset(w - inset, h - inset),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = color,
                    start = Offset(w - inset, inset),
                    end = Offset(inset, h - inset),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
            }
            else -> Unit
        }
    }
}
