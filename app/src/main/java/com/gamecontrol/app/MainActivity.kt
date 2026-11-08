package com.gamecontrol.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.gamecontrol.app.input.InputConfig
import com.gamecontrol.app.input.InputEngine
import com.gamecontrol.app.model.ControllerUiState
import com.gamecontrol.app.ui.controller.ControllerScreen
import com.gamecontrol.app.ui.tester.ControllerTesterScreen
import com.gamecontrol.app.ui.theme.GameControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameControlTheme {
                val uiState = remember { ControllerUiState() }
                var config by remember { mutableStateOf(InputConfig()) }
                var screen by remember { mutableStateOf(AppScreen.CONTROLLER) }
                val engine = remember(config) { InputEngine(config) }

                // Android back gesture returns from the tester to the controller.
                BackHandler(enabled = screen == AppScreen.TESTER) {
                    screen = AppScreen.CONTROLLER
                }

                when (screen) {
                    AppScreen.CONTROLLER -> ControllerScreen(
                        state = uiState,
                        onOpenTester = { screen = AppScreen.TESTER },
                    )
                    AppScreen.TESTER -> ControllerTesterScreen(
                        uiState = uiState,
                        engine = engine,
                        config = config,
                        onConfigChange = { config = it },
                        onBack = { screen = AppScreen.CONTROLLER },
                    )
                }
            }
        }
    }
}

private enum class AppScreen { CONTROLLER, TESTER }
