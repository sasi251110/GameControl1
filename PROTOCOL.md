# GameControl wire protocol (version 1)

Used between the Android app (client) and the Windows companion (server) over TCP on the local network.

## Framing

Every message is a frame:

| Field | Size | Notes |
|---|---|---|
| body length | 4 bytes | big-endian, length of the body below |
| type | 1 byte | message type |
| payload | n bytes | depends on the type |

Bodies are at most 1024 bytes.

## Handshake

1. Phone sends `HELLO` (0x01), payload `G C 0x01`.
2. PC replies `CHALLENGE` (0x02), payload is a random 16-byte nonce.
3. Phone sends `AUTH` (0x03), payload is `HMAC-SHA256(key, nonce || "phone")`.
4. PC replies `AUTH_OK` (0x04) or `AUTH_FAIL` (0x05).

The key is `PBKDF2-HMAC-SHA256(pairing code, salt = "gamecontrol-v1", iterations = 100000, 32 bytes)`.

After `AUTH_OK`, both sides use the session key `HMAC-SHA256(key, nonce || "session")`.

## Session messages

| Type | Direction | Payload |
|---|---|---|
| `STATE` (0x10) | phone to PC | 20-byte state, then a 32-byte HMAC over (type byte + state) using the session key |
| `PING` (0x20) | phone to PC | 4-byte id, sent about once a second |
| `PONG` (0x21) | PC to phone | the same 4-byte id, echoed |

State payload (20 bytes, big-endian): `seq u32`, `timestamp_ms u32`, `buttons u16`, `LX i16`, `LY i16`, `RX i16`, `RY i16`, `LT u8`, `RT u8`.

Button bits follow the Android `ControllerButton` order: bit 0 is DPAD_UP through bit 15 is SHARE.

## Rules

- The phone sends a state when it changes and at least every 100 ms.
- The PC ignores frames with a bad HMAC, and frames whose `seq` is not greater than the last one (replay protection).
- If no valid frame arrives for 1 second, the PC releases all buttons.
- The phone sends a neutral state before it closes the link.
- After 5 wrong pairing codes, the PC refuses new attempts for 60 seconds.

## Security limits (honest summary)

- This is **authentication**, not encryption. Controller states travel in clear text on the local network.
- A 6-digit code has only a million possibilities. The PBKDF2 step makes offline guessing slow, but it does not make it impossible. Pair only on a network you trust.
- Encryption (for example, Noise or TLS) is planned as later hardening.
