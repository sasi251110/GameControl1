package com.gamecontrol.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue

/**
 * Holds what the controller UI is currently pressing and where the sticks are.
 * Temporary for Phase 1. Phase 2 replaces it with a proper input engine.
 */
class ControllerUiState {

    /** Buttons currently held down. Compose observes this list for changes. */
    val pressed = mutableStateListOf<ControllerButton>()

    var leftX by mutableFloatStateOf(0f)
        private set
    var leftY by mutableFloatStateOf(0f)
        private set
    var rightX by mutableFloatStateOf(0f)
        private set
    var rightY by mutableFloatStateOf(0f)
        private set

    fun setButton(button: ControllerButton, isPressed: Boolean) {
        val alreadyPressed = pressed.contains(button)
        if (isPressed && !alreadyPressed) {
            pressed.add(button)
        } else if (!isPressed && alreadyPressed) {
            pressed.remove(button)
        }
    }

    fun setStick(left: Boolean, x: Float, y: Float) {
        if (left) {
            leftX = x
            leftY = y
        } else {
            rightX = x
            rightY = y
        }
    }

    /** Releases everything. Used when the app is paused so nothing stays stuck. */
    fun releaseAll() {
        pressed.clear()
        leftX = 0f
        leftY = 0f
        rightX = 0f
        rightY = 0f
    }
}
