using System.Buffers.Binary;

namespace GameControl.Core;

/// <summary>
/// Wire protocol shared with the Android app. See docs/PROTOCOL.md.
/// Every frame: 4-byte big-endian body length, then body = 1-byte type + payload.
/// </summary>
public static class Protocol
{
    public const int Version = 1;

    public const byte TypeHello = 0x01;
    public const byte TypeChallenge = 0x02;
    public const byte TypeAuth = 0x03;
    public const byte TypeAuthOk = 0x04;
    public const byte TypeAuthFail = 0x05;
    public const byte TypeState = 0x10;
    public const byte TypePing = 0x20;
    public const byte TypePong = 0x21;

    public const int NonceLength = 16;
    public const int HmacLength = 32;
    public const int StatePayloadLength = 20;
    public const int MaxBodyLength = 1024;

    public static byte[] EncodeFrame(byte type, ReadOnlySpan<byte> payload)
    {
        var body = new byte[1 + payload.Length];
        body[0] = type;
        payload.CopyTo(body.AsSpan(1));

        var frame = new byte[4 + body.Length];
        BinaryPrimitives.WriteInt32BigEndian(frame, body.Length);
        body.CopyTo(frame, 4);
        return frame;
    }

    /// <summary>Builds the 20-byte state payload. The phone does this too, so tests use it as the reference.</summary>
    public static byte[] EncodeState(uint seq, uint timestampMs, GamepadReport report)
    {
        var payload = new byte[StatePayloadLength];
        BinaryPrimitives.WriteUInt32BigEndian(payload.AsSpan(0), seq);
        BinaryPrimitives.WriteUInt32BigEndian(payload.AsSpan(4), timestampMs);
        BinaryPrimitives.WriteUInt16BigEndian(payload.AsSpan(8), report.Buttons);
        BinaryPrimitives.WriteInt16BigEndian(payload.AsSpan(10), report.LeftX);
        BinaryPrimitives.WriteInt16BigEndian(payload.AsSpan(12), report.LeftY);
        BinaryPrimitives.WriteInt16BigEndian(payload.AsSpan(14), report.RightX);
        BinaryPrimitives.WriteInt16BigEndian(payload.AsSpan(16), report.RightY);
        payload[18] = report.LeftTrigger;
        payload[19] = report.RightTrigger;
        return payload;
    }

    public static (uint Seq, GamepadReport Report) DecodeState(ReadOnlySpan<byte> payload)
    {
        if (payload.Length != StatePayloadLength)
        {
            throw new ArgumentException($"State payload must be {StatePayloadLength} bytes.", nameof(payload));
        }

        var seq = BinaryPrimitives.ReadUInt32BigEndian(payload);
        var report = new GamepadReport(
            BinaryPrimitives.ReadUInt16BigEndian(payload[8..]),
            BinaryPrimitives.ReadInt16BigEndian(payload[10..]),
            BinaryPrimitives.ReadInt16BigEndian(payload[12..]),
            BinaryPrimitives.ReadInt16BigEndian(payload[14..]),
            BinaryPrimitives.ReadInt16BigEndian(payload[16..]),
            payload[18],
            payload[19]);
        return (seq, report);
    }
}
