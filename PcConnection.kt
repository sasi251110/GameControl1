package com.gamecontrol.app.network

import com.gamecontrol.app.input.ControllerState
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class LinkStatus {
    data object Idle : LinkStatus()
    data object Connecting : LinkStatus()
    data object Authenticating : LinkStatus()
    data class Connected(val latencyMs: Int?) : LinkStatus()
    data class Failed(val reason: String) : LinkStatus()
}

fun LinkStatus.label(): String = when (this) {
    LinkStatus.Idle -> "NOT CONNECTED"
    LinkStatus.Connecting -> "CONNECTING"
    LinkStatus.Authenticating -> "PAIRING"
    is LinkStatus.Connected -> if (latencyMs != null) "CONNECTED $latencyMs MS" else "CONNECTED"
    is LinkStatus.Failed -> "FAILED"
}

/**
 * Sends controller state to the PC companion over TCP.
 *
 * [source] is called every few milliseconds and must be fast and thread-safe.
 * A state is sent whenever it changes, and at least every 100 ms as a heartbeat.
 */
class PcConnection(
    private val scope: CoroutineScope,
    private val source: () -> ControllerState,
) {
    private val _status = MutableStateFlow<LinkStatus>(LinkStatus.Idle)
    val status: StateFlow<LinkStatus> = _status.asStateFlow()

    private val pingSentAt = ConcurrentHashMap<Int, Long>()
    private var job: Job? = null

    fun connect(host: String, port: Int, code: String) {
        disconnect()
        job = scope.launch(Dispatchers.IO) { runSession(host, port, code) }
    }

    fun disconnect() {
        job?.cancel()
        job = null
        _status.value = LinkStatus.Idle
    }

    private suspend fun runSession(host: String, port: Int, code: String) {
        val socket = Socket()
        try {
            _status.value = LinkStatus.Connecting
            socket.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
            socket.tcpNoDelay = true
            socket.soTimeout = HANDSHAKE_TIMEOUT_MS
            val input = DataInputStream(BufferedInputStream(socket.getInputStream()))
            val output = DataOutputStream(BufferedOutputStream(socket.getOutputStream()))

            _status.value = LinkStatus.Authenticating
            output.write(
                Protocol.frame(
                    Protocol.TYPE_HELLO,
                    byteArrayOf('G'.code.toByte(), 'C'.code.toByte(), Protocol.VERSION.toByte()),
                ),
            )
            output.flush()

            val challenge = Protocol.readFrame(input)
            check(challenge.type == Protocol.TYPE_CHALLENGE && challenge.payload.size == Protocol.NONCE_LENGTH) {
                "Unexpected reply from the PC"
            }
            val key = Protocol.pairingKey(code)
            val proof = Protocol.hmac(key, challenge.payload, "phone".toByteArray(Charsets.UTF_8))
            output.write(Protocol.frame(Protocol.TYPE_AUTH, proof))
            output.flush()

            val reply = Protocol.readFrame(input)
            if (reply.type != Protocol.TYPE_AUTH_OK) {
                throw SecurityException("Wrong pairing code, or the PC is busy or locked")
            }
            val sessionKey = Protocol.hmac(key, challenge.payload, "session".toByteArray(Charsets.UTF_8))

            socket.soTimeout = 0
            pingSentAt.clear()
            _status.value = LinkStatus.Connected(null)

            coroutineScope {
                launch(Dispatchers.IO) { readReplies(input) }
                try {
                    writeStates(output, sessionKey)
                } finally {
                    // Closing the socket also unblocks the reader coroutine.
                    socket.close()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SecurityException) {
            _status.value = LinkStatus.Failed(e.message ?: "Pairing failed")
        } catch (e: Exception) {
            _status.value = LinkStatus.Failed(e.message ?: "Connection lost")
        } finally {
            socket.close()
        }
    }

    private suspend fun writeStates(output: DataOutputStream, sessionKey: ByteArray) {
        var seq = 0
        var lastState: ControllerState? = null
        var lastSentAt = 0L
        var nextPingAt = 0L
        var pingId = 0
        try {
            while (true) {
                val now = System.currentTimeMillis()
                val state = source()
                if (state != lastState || now - lastSentAt >= HEARTBEAT_MS) {
                    seq++
                    output.write(Protocol.stateFrame(sessionKey, seq, now, state))
                    output.flush()
                    lastState = state
                    lastSentAt = now
                }
                if (now >= nextPingAt) {
                    pingId++
                    pingSentAt[pingId] = now
                    output.write(Protocol.frame(Protocol.TYPE_PING, ByteBuffer.allocate(4).putInt(pingId).array()))
                    output.flush()
                    nextPingAt = now + PING_INTERVAL_MS
                }
                delay(POLL_MS)
            }
        } finally {
            // Best effort: tell the PC that every button is released before the link closes.
            try {
                seq++
                output.write(Protocol.stateFrame(sessionKey, seq, System.currentTimeMillis(), NEUTRAL))
                output.flush()
            } catch (e: IOException) {
                // The link is already gone. The PC releases its buttons on timeout.
            }
        }
    }

    private fun readReplies(input: DataInputStream) {
        try {
            while (true) {
                val frame = Protocol.readFrame(input)
                if (frame.type == Protocol.TYPE_PONG && frame.payload.size == 4) {
                    val id = ByteBuffer.wrap(frame.payload).int
                    val sentAt = pingSentAt.remove(id) ?: continue
                    val rttMs = (System.currentTimeMillis() - sentAt).toInt()
                    _status.update { if (it is LinkStatus.Connected) LinkStatus.Connected(rttMs) else it }
                }
            }
        } catch (e: Exception) {
            // Socket closed or the PC stopped replying. The writer reports the failure.
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 5_000
        const val HANDSHAKE_TIMEOUT_MS = 5_000
        const val HEARTBEAT_MS = 100L
        const val PING_INTERVAL_MS = 1_000L
        const val POLL_MS = 4L
        val NEUTRAL = ControllerState(emptySet(), 0f, 0f, 0f, 0f, 0f, 0f)
    }
}
