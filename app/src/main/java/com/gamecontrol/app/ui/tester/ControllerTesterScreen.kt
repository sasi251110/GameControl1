package com.gamecontrol.app.ui.tester

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.gamecontrol.app.input.ControllerState
import com.gamecontrol.app.input.InputConfig
import com.gamecontrol.app.input.InputEngine
import com.gamecontrol.app.input.buttonMask
import com.gamecontrol.app.input.toAxis16
import com.gamecontrol.app.input.toTrigger8
import com.gamecontrol.app.model.ControllerButton
import com.gamecontrol.app.model.ControllerUiState
import com.gamecontrol.app.ui.theme.ColorOnPrimaryContainer
import com.gamecontrol.app.ui.theme.ColorOnSurface
import com.gamecontrol.app.ui.theme.ColorOnSurfaceVariant
import com.gamecontrol.app.ui.theme.ColorOutline
import com.gamecontrol.app.ui.theme.ColorPrimaryContainer
import com.gamecontrol.app.ui.theme.ColorSurfaceBase
import com.gamecontrol.app.ui.theme.ColorSurfaceHigh
import com.gamecontrol.app.ui.theme.ColorSurfaceLow
import com.gamecontrol.app.ui.theme.ColorTertiary
import java.util.Locale

/**
 * Shows the processed controller state, the values a PC would receive,
 * and sliders for dead zone and sensitivity.
 */
@Composable
fun ControllerTesterScreen(
    uiState: ControllerUiState,
    engine: InputEngine,
    config: InputConfig,
    onConfigChange: (InputConfig) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val raw = uiState.snapshot()
    val state = engine.process(raw)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSurfaceBase)
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Header(onBack = onBack)

        Section("Buttons") {
            ButtonGrid(state)
        }

        Section("Sticks") {
            StickLine("LEFT", raw.leftX, raw.leftY, state.leftX, state.leftY)
            StickLine("RIGHT", raw.rightX, raw.rightY, state.rightX, state.rightY)
        }

        Section("Triggers") {
            TriggerLine("L2", state.leftTrigger)
            TriggerLine("R2", state.rightTrigger)
        }

        Section("Settings") {
            SettingSlider(
                title = "Dead zone",
                value = config.deadZone,
                range = 0f..0.5f,
                onValueChange = { onConfigChange(config.copy(deadZone = it)) },
            )
            SettingSlider(
                title = "Sensitivity",
                value = config.sensitivity,
                range = 0.5f..3f,
                onValueChange = { onConfigChange(config.copy(sensitivity = it)) },
            )
        }

        Section("What the PC receives") {
            PacketLine("buttons", String.format(Locale.US, "0x%04X", state.buttonMask()))
            PacketLine("LX", state.leftX.toAxis16().toString())
            PacketLine("LY", state.leftY.toAxis16().toString())
            PacketLine("RX", state.rightX.toAxis16().toString())
            PacketLine("RY", state.rightY.toAxis16().toString())
            PacketLine("LT", state.leftTrigger.toTrigger8().toString())
            PacketLine("RT", state.rightTrigger.toTrigger8().toString())
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Controller Tester",
            style = MaterialTheme.typography.headlineSmall,
            color = ColorOnSurface,
        )
        Box(
            modifier = Modifier
                .height(44.dp)
                .clip(CircleShape)
                .background(ColorSurfaceHigh)
                .clickable(onClick = onBack)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "BACK",
                style = MaterialTheme.typography.labelSmall,
                color = ColorOnSurface,
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorSurfaceLow)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title.uppercase(Locale.US),
            style = MaterialTheme.typography.labelSmall,
            color = ColorOutline,
        )
        content()
    }
}

@Composable
private fun ButtonGrid(state: ControllerState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ControllerButton.values().toList().chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row.forEach { button ->
                    ButtonChip(
                        label = button.name,
                        pressed = state.isPressed(button),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ButtonChip(label: String, pressed: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (pressed) ColorPrimaryContainer else ColorSurfaceHigh),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (pressed) ColorOnPrimaryContainer else ColorOnSurfaceVariant,
        )
    }
}

@Composable
private fun StickLine(label: String, rawX: Float, rawY: Float, outX: Float, outY: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "$label stick",
            style = MaterialTheme.typography.labelMedium,
            color = ColorOnSurface,
        )
        Text(
            text = "raw  x ${signed(rawX)}  y ${signed(rawY)}",
            style = MaterialTheme.typography.labelMedium,
            color = ColorOnSurfaceVariant,
        )
        Text(
            text = "out  x ${signed(outX)}  y ${signed(outY)}",
            style = MaterialTheme.typography.labelMedium,
            color = ColorTertiary,
        )
    }
}

@Composable
private fun TriggerLine(label: String, value: Float) {
    val status = if (value > 0f) "PRESSED" else "RELEASED"
    Text(
        text = "$label  $status  value ${String.format(Locale.US, "%.2f", value)}",
        style = MaterialTheme.typography.labelMedium,
        color = ColorOnSurface,
    )
}

@Composable
private fun SettingSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = ColorOnSurface,
            )
            Text(
                text = String.format(Locale.US, "%.2f", value),
                style = MaterialTheme.typography.labelMedium,
                color = ColorTertiary,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
        )
    }
}

@Composable
private fun PacketLine(name: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = ColorOutline,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = ColorOnSurface,
        )
    }
}

private fun signed(value: Float): String = String.format(Locale.US, "%+.2f", value)
