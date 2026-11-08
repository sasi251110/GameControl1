package com.gamecontrol.app.input

import com.gamecontrol.app.model.ControllerButton
import kotlin.math.pow
import kotlin.math.sqrt

/** Raw values from the touch UI, before any tuning is applied. */
data class RawInput(
    val buttons: Set<ControllerButton>,
    val leftX: Float,
    val leftY: Float,
    val rightX: Float,
    val rightY: Float,
)

/**
 * Converts raw touch input into a ControllerState.
 *
 * Stateless: the same input and config always give the same output.
 * That makes it easy to test.
 */
class InputEngine(private val config: InputConfig = InputConfig()) {

    fun process(raw: RawInput): ControllerState {
        val (lx, ly) = shapeStick(raw.leftX, raw.leftY)
        val (rx, ry) = shapeStick(raw.rightX, raw.rightY)
        return ControllerState(
            buttons = raw.buttons,
            leftX = lx,
            leftY = ly,
            rightX = rx,
            rightY = ry,
            leftTrigger = if (ControllerButton.L2 in raw.buttons) 1f else 0f,
            rightTrigger = if (ControllerButton.R2 in raw.buttons) 1f else 0f,
        )
    }

    /**
     * Radial dead zone, then a rescale so output starts at zero right outside the
     * dead zone (no jump), then the sensitivity curve. Direction is preserved.
     */
    private fun shapeStick(x: Float, y: Float): Pair<Float, Float> {
        val magnitude = sqrt(x * x + y * y)
        if (magnitude <= config.deadZone) return 0f to 0f

        val scaled = ((magnitude - config.deadZone) / (1f - config.deadZone)).coerceIn(0f, 1f)
        val curved = scaled.pow(config.sensitivity)
        val factor = curved / magnitude

        return (x * factor).coerceIn(-1f, 1f) to (y * factor).coerceIn(-1f, 1f)
    }
}
