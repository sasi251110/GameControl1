package com.gamecontrol.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.gamecontrol.app.model.ControllerUiState
import com.gamecontrol.app.ui.controller.ControllerScreen
import com.gamecontrol.app.ui.theme.GameControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameControlTheme {
                val state = remember { ControllerUiState() }
                ControllerScreen(state = state)
            }
        }
    }
}
