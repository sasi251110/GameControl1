package com.gamecontrol.app.input

import com.gamecontrol.app.model.ControllerButton
import kotlin.math.roundToInt

/**
 * The processed controller state that a PC would receive.
 * Sticks range from -1 to 1 (Y positive is up). Triggers range from 0 to 1.
 */
data class ControllerState(
    val buttons: Set<ControllerButton>,
    val leftX: Float,
    val leftY: Float,
    val rightX: Float,
    val rightY: Float,
    val leftTrigger: Float,
    val rightTrigger: Float,
) {
    fun isPressed(button: ControllerButton): Boolean = button in buttons
}

/** One bit per button, using each button's position in the enum (0 to 15). */
fun ControllerState.buttonMask(): Int =
    buttons.fold(0) { mask, button -> mask or (1 shl button.ordinal) }

/** Stick value as a signed 16-bit number. Symmetric range -32767..32767. */
fun Float.toAxis16(): Int = (this * 32767f).roundToInt().coerceIn(-32767, 32767)

/** Trigger value as an unsigned 8-bit number, 0..255. */
fun Float.toTrigger8(): Int = (this * 255f).roundToInt().coerceIn(0, 255)
