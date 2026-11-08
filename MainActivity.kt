package com.gamecontrol.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.gamecontrol.app.input.ControllerState
import com.gamecontrol.app.input.InputConfig
import com.gamecontrol.app.input.InputEngine
import com.gamecontrol.app.model.ControllerUiState
import com.gamecontrol.app.network.PcConnection
import com.gamecontrol.app.ui.connect.ConnectScreen
import com.gamecontrol.app.ui.controller.ControllerScreen
import com.gamecontrol.app.ui.tester.ControllerTesterScreen
import com.gamecontrol.app.ui.theme.GameControlTheme
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.delay

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

                // Sampled on the main thread. The network thread only reads this value.
                val latest = remember {
                    AtomicReference(ControllerState(emptySet(), 0f, 0f, 0f, 0f, 0f, 0f))
                }
                LaunchedEffect(engine) {
                    while (true) {
                        latest.set(engine.process(uiState.snapshot()))
                        delay(2)
                    }
                }

                val scope = rememberCoroutineScope()
                val connection = remember { PcConnection(scope) { latest.get() } }
                val linkStatus by connection.status.collectAsState()
                DisposableEffect(Unit) {
                    onDispose { connection.disconnect() }
                }

                // Android back gesture returns from any sub-screen to the controller.
                BackHandler(enabled = screen != AppScreen.CONTROLLER) {
                    screen = AppScreen.CONTROLLER
                }

                when (screen) {
                    AppScreen.CONTROLLER -> ControllerScreen(
                        state = uiState,
                        linkStatus = linkStatus,
                        onOpenTester = { screen = AppScreen.TESTER },
                        onOpenConnection = { screen = AppScreen.CONNECT },
                    )
                    AppScreen.TESTER -> ControllerTesterScreen(
                        uiState = uiState,
                        engine = engine,
                        config = config,
                        onConfigChange = { config = it },
                        onBack = { screen = AppScreen.CONTROLLER },
                    )
                    AppScreen.CONNECT -> ConnectScreen(
                        status = linkStatus,
                        onConnect = { host, port, code -> connection.connect(host, port, code) },
                        onDisconnect = { connection.disconnect() },
                        onBack = { screen = AppScreen.CONTROLLER },
                    )
                }
            }
        }
    }
}

private enum class AppScreen { CONTROLLER, TESTER, CONNECT }
