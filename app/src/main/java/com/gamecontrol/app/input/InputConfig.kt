package com.gamecontrol.app.input

/**
 * Tuning values for the input engine.
 *
 * deadZone: stick radius below which input is treated as zero (0 to 0.9).
 * sensitivity: response curve exponent. 1.0 is linear. Above 1.0 is softer
 * near the center. Below 1.0 is more aggressive near the center.
 */
data class InputConfig(
    val deadZone: Float = 0.12f,
    val sensitivity: Float = 1.0f,
) {
    init {
        require(deadZone in 0f..0.9f) { "deadZone must be between 0 and 0.9" }
        require(sensitivity in 0.5f..3f) { "sensitivity must be between 0.5 and 3" }
    }
}
