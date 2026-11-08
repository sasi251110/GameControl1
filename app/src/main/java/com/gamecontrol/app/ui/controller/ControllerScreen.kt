package com.gamecontrol.app.ui.controller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.gamecontrol.app.model.ControllerButton
import com.gamecontrol.app.model.ControllerUiState
import com.gamecontrol.app.ui.theme.ColorOnSecondaryContainer
import com.gamecontrol.app.ui.theme.ColorOnSurface
import com.gamecontrol.app.ui.theme.ColorOnSurfaceVariant
import com.gamecontrol.app.ui.theme.ColorOutline
import com.gamecontrol.app.ui.theme.ColorSecondaryContainer
import com.gamecontrol.app.ui.theme.ColorSurfaceBase
import com.gamecontrol.app.ui.theme.ColorSurfaceHigh

private val FaceClusterSize = 152.dp
private val StickSize = 104.dp

/**
 * Virtual controller in landscape.
 * Left: shoulders, D-pad, left stick. Center: Share and Options. Right: right stick, face buttons, shoulders.
 */
@Composable
fun ControllerScreen(
    state: ControllerUiState,
    modifier: Modifier = Modifier,
) {
    ReleaseInputsWhenPaused(state)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSurfaceBase)
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        StatusBar()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LeftCluster(state)
            CenterCluster(state)
            RightCluster(state)
        }
    }
}

@Composable
private fun StatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "GameControl",
                style = MaterialTheme.typography.headlineSmall,
                color = ColorOnSurface,
            )
            Text(
                text = "CONTROLLER",
                style = MaterialTheme.typography.labelSmall,
                color = ColorOnSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(ColorSurfaceHigh)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(ColorOutline),
            )
            Text(
                text = "NOT CONNECTED",
                style = MaterialTheme.typography.labelSmall,
                color = ColorOnSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LeftCluster(state: ControllerUiState) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShoulderRow(state, isLeft = true)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DPad(state = state, modifier = Modifier.size(FaceClusterSize))
            StickColumn(state = state, isLeft = true)
        }
    }
}

@Composable
private fun RightCluster(state: ControllerUiState) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShoulderRow(state, isLeft = false)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StickColumn(state = state, isLeft = false)
            FaceButtons(state = state)
        }
    }
}

@Composable
private fun ShoulderRow(state: ControllerUiState, isLeft: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isLeft) {
            ControlButton(
                label = "L2",
                button = ControllerButton.L2,
                state = state,
                modifier = Modifier.size(80.dp, 44.dp),
                showPercent = true,
            )
            ControlButton(
                label = "L1",
                button = ControllerButton.L1,
                state = state,
                modifier = Modifier.size(80.dp, 44.dp),
            )
        } else {
            ControlButton(
                label = "R1",
                button = ControllerButton.R1,
                state = state,
                modifier = Modifier.size(80.dp, 44.dp),
                pressedColor = ColorSecondaryContainer,
                pressedTextColor = ColorOnSecondaryContainer,
            )
            ControlButton(
                label = "R2",
                button = ControllerButton.R2,
                state = state,
                modifier = Modifier.size(80.dp, 44.dp),
                showPercent = true,
                pressedColor = ColorSecondaryContainer,
                pressedTextColor = ColorOnSecondaryContainer,
            )
        }
    }
}

@Composable
private fun StickColumn(state: ControllerUiState, isLeft: Boolean) {
    val x = if (isLeft) state.leftX else state.rightX
    val y = if (isLeft) state.leftY else state.rightY

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AnalogStick(
            x = x,
            y = y,
            onMove = { nx, ny -> state.setStick(isLeft, nx, ny) },
            modifier = Modifier
                .size(StickSize)
                .clip(CircleShape),
        )
        ControlButton(
            label = if (isLeft) "L3" else "R3",
            button = if (isLeft) ControllerButton.L3 else ControllerButton.R3,
            state = state,
            modifier = Modifier.size(72.dp, 48.dp),
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun CenterCluster(state: ControllerUiState) {
    Column(
        modifier = Modifier.width(136.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ControlButton(
                label = "SHARE",
                button = ControllerButton.SHARE,
                state = state,
                modifier = Modifier.size(64.dp, 48.dp),
            )
            ControlButton(
                label = "OPTIONS",
                button = ControllerButton.OPTIONS,
                state = state,
                modifier = Modifier.size(64.dp, 48.dp),
            )
        }
    }
}

/** Clears all pressed inputs when the app leaves the foreground, so nothing stays stuck. */
@Composable
private fun ReleaseInputsWhenPaused(state: ControllerUiState) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, state) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                state.releaseAll()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            state.releaseAll()
        }
    }
}
