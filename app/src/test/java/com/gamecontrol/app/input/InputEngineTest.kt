package com.gamecontrol.app.input

import com.gamecontrol.app.model.ControllerButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class InputEngineTest {

    private val idle = RawInput(emptySet(), 0f, 0f, 0f, 0f)

    @Test
    fun idleInputProducesZeroState() {
        val state = InputEngine().process(idle)
        assertTrue(state.buttons.isEmpty())
        assertEquals(0f, state.leftX, 0f)
        assertEquals(0f, state.leftY, 0f)
        assertEquals(0f, state.rightX, 0f)
        assertEquals(0f, state.rightY, 0f)
        assertEquals(0f, state.leftTrigger, 0f)
        assertEquals(0f, state.rightTrigger, 0f)
    }

    @Test
    fun pressedButtonsAreReported() {
        val raw = idle.copy(buttons = setOf(ControllerButton.CROSS, ControllerButton.DPAD_UP))
        val state = InputEngine().process(raw)
        assertTrue(state.isPressed(ControllerButton.CROSS))
        assertTrue(state.isPressed(ControllerButton.DPAD_UP))
        assertFalse(state.isPressed(ControllerButton.CIRCLE))
    }

    @Test
    fun l2AndR2BecomeFullTriggers() {
        val state = InputEngine().process(idle.copy(buttons = setOf(ControllerButton.L2)))
        assertEquals(1f, state.leftTrigger, 0f)
        assertEquals(0f, state.rightTrigger, 0f)
    }

    @Test
    fun stickInsideDeadZoneIsZero() {
        val engine = InputEngine(InputConfig(deadZone = 0.12f))
        val state = engine.process(idle.copy(leftX = 0.05f, leftY = 0.05f))
        assertEquals(0f, state.leftX, 0f)
        assertEquals(0f, state.leftY, 0f)
    }

    @Test
    fun fullDeflectionReachesOne() {
        val engine = InputEngine(InputConfig(deadZone = 0.1f, sensitivity = 1f))
        val state = engine.process(idle.copy(leftX = 1f, leftY = 0f))
        assertEquals(1f, state.leftX, 0.001f)
        assertEquals(0f, state.leftY, 0.001f)
    }

    @Test
    fun outputStartsAtZeroJustOutsideDeadZone() {
        val engine = InputEngine(InputConfig(deadZone = 0.1f, sensitivity = 1f))
        val state = engine.process(idle.copy(leftX = 0.1001f, leftY = 0f))
        assertTrue(state.leftX < 0.001f)
    }

    @Test
    fun diagonalFullDeflectionStaysInsideUnitCircle() {
        val engine = InputEngine(InputConfig(deadZone = 0.1f))
        val state = engine.process(idle.copy(leftX = 1f, leftY = 1f))
        val magnitude = sqrt(state.leftX * state.leftX + state.leftY * state.leftY)
        assertTrue(magnitude <= 1.0001f)
    }

    @Test
    fun directionIsPreserved() {
        val engine = InputEngine(InputConfig(deadZone = 0.1f))
        val state = engine.process(idle.copy(rightX = 0.6f, rightY = -0.8f))
        assertTrue(state.rightX > 0f)
        assertTrue(state.rightY < 0f)
    }

    @Test
    fun sensitivityAboveOneGivesSmallerMidValues() {
        val linear = InputEngine(InputConfig(deadZone = 0f, sensitivity = 1f))
        val softer = InputEngine(InputConfig(deadZone = 0f, sensitivity = 2f))
        val raw = idle.copy(leftX = 0.5f, leftY = 0f)
        assertEquals(0.5f, linear.process(raw).leftX, 0.001f)
        assertEquals(0.25f, softer.process(raw).leftX, 0.001f)
    }

    @Test
    fun invalidConfigIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            InputConfig(deadZone = 1f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            InputConfig(sensitivity = 10f)
        }
    }

    @Test
    fun axisConversionIsSymmetric() {
        assertEquals(32767, 1f.toAxis16())
        assertEquals(-32767, (-1f).toAxis16())
        assertEquals(0, 0f.toAxis16())
    }

    @Test
    fun triggerConversionCoversFullRange() {
        assertEquals(255, 1f.toTrigger8())
        assertEquals(0, 0f.toTrigger8())
    }

    @Test
    fun buttonMaskUsesEnumPosition() {
        val state = InputEngine().process(idle.copy(buttons = setOf(ControllerButton.CROSS)))
        assertEquals(1 shl ControllerButton.CROSS.ordinal, state.buttonMask())
    }
}
