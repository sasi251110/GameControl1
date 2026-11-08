package com.gamecontrol.app.network

import com.gamecontrol.app.input.ControllerState
import com.gamecontrol.app.input.buttonMask
import com.gamecontrol.app.input.toAxis16
import com.gamecontrol.app.input.toTrigger8
import java.io.DataInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Wire protocol shared with the Windows companion. See docs/PROTOCOL.md.
 *
 * Every frame: 4-byte big-endian body length, then body = 1-byte type + payload.
 */
object Protocol {
    const val VERSION = 1

    const val TYPE_HELLO = 0x01
    const val TYPE_CHALLENGE = 0x02
    const val TYPE_AUTH = 0x03
    const val TYPE_AUTH_OK = 0x04
    const val TYPE_AUTH_FAIL = 0x05
    const val TYPE_STATE = 0x10
    const val TYPE_PING = 0x20
    const val TYPE_PONG = 0x21

    const val NONCE_LENGTH = 16
    const val HMAC_LENGTH = 32
    const val STATE_PAYLOAD_LENGTH = 20
    const val MAX_BODY_LENGTH = 1024

    private const val PBKDF2_ITERATIONS = 100_000
    private val SALT = "gamecontrol-v1".toByteArray(Charsets.UTF_8)

    data class Frame(val type: Int, val payload: ByteArray)

    fun frame(type: Int, payload: ByteArray = ByteArray(0)): ByteArray {
        val body = ByteArray(1 + payload.size)
        body[0] = type.toByte()
        payload.copyInto(body, 1)
        return ByteBuffer.allocate(4 + body.size)
            .order(ByteOrder.BIG_ENDIAN)
            .putInt(body.size)
            .put(body)
            .array()
    }

    fun readFrame(input: DataInputStream): Frame {
        val length = input.readInt()
        require(length in 1..MAX_BODY_LENGTH) { "Bad frame length $length" }
        val body = ByteArray(length)
        input.readFully(body)
        return Frame(body[0].toInt() and 0xFF, body.copyOfRange(1, body.size))
    }

    /** Derives a 32-byte key from the 6-digit pairing code. Slow on purpose, to limit offline guessing. */
    fun pairingKey(code: String): ByteArray {
        val spec = PBEKeySpec(code.toCharArray(), SALT, PBKDF2_ITERATIONS, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    fun hmac(key: ByteArray, vararg parts: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        parts.forEach { mac.update(it) }
        return mac.doFinal()
    }

    /** 20-byte state payload: seq u32, timestamp u32, buttons u16, LX/LY/RX/RY i16, LT/RT u8. */
    fun encodeState(seq: Int, timestampMs: Long, state: ControllerState): ByteArray =
        ByteBuffer.allocate(STATE_PAYLOAD_LENGTH)
            .order(ByteOrder.BIG_ENDIAN)
            .putInt(seq)
            .putInt(timestampMs.toInt())
            .putShort(state.buttonMask().toShort())
            .putShort(state.leftX.toAxis16().toShort())
            .putShort(state.leftY.toAxis16().toShort())
            .putShort(state.rightX.toAxis16().toShort())
            .putShort(state.rightY.toAxis16().toShort())
            .put(state.leftTrigger.toTrigger8().toByte())
            .put(state.rightTrigger.toTrigger8().toByte())
            .array()

    /** A STATE frame: payload followed by an HMAC over (type byte + payload) using the session key. */
    fun stateFrame(sessionKey: ByteArray, seq: Int, timestampMs: Long, state: ControllerState): ByteArray {
        val payload = encodeState(seq, timestampMs, state)
        val mac = hmac(sessionKey, byteArrayOf(TYPE_STATE.toByte()), payload)
        return frame(TYPE_STATE, payload + mac)
    }
}
