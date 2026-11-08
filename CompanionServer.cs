using System.Net;
using System.Net.Sockets;

namespace GameControl.Core;

internal readonly record struct Frame(byte Type, byte[] Payload);

/// <summary>
/// Listens for one phone at a time. Authenticates it with the pairing code,
/// then receives controller states. Releases everything when the phone goes quiet.
/// </summary>
public sealed class CompanionServer
{
    private static readonly TimeSpan HandshakeTimeout = TimeSpan.FromSeconds(5);
    private static readonly TimeSpan ReceiveTimeout = TimeSpan.FromSeconds(1);

    private readonly int _port;
    private readonly byte[] _pairingKey;
    private readonly FailureTracker _failures = new();

    public CompanionServer(int port, string pairingCode)
    {
        _port = port;
        _pairingKey = PairingAuth.DeriveKey(pairingCode);
    }

    /// <summary>Human-readable log lines.</summary>
    public event Action<string>? Logged;

    /// <summary>Each verified controller state. A neutral report is sent when the session ends.</summary>
    public event Action<GamepadReport>? StateReceived;

    /// <summary>True when a phone is authenticated, false when its session ends.</summary>
    public event Action<bool, string>? SessionChanged;

    public async Task RunAsync(CancellationToken ct)
    {
        var listener = new TcpListener(IPAddress.Any, _port);
        listener.Start();
        Logged?.Invoke($"Listening on port {_port}.");

        try
        {
            while (!ct.IsCancellationRequested)
            {
                TcpClient client;
                try
                {
                    client = await listener.AcceptTcpClientAsync(ct);
                }
                catch (OperationCanceledException)
                {
                    break;
                }

                using (client)
                {
                    await HandleClientAsync(client, ct);
                }
            }
        }
        finally
        {
            listener.Stop();
        }
    }

    private async Task HandleClientAsync(TcpClient client, CancellationToken ct)
    {
        client.NoDelay = true;
        var peer = client.Client.RemoteEndPoint?.ToString() ?? "unknown";
        var stream = client.GetStream();
        var sessionOpen = false;

        try
        {
            if (_failures.IsLocked(DateTimeOffset.UtcNow))
            {
                await WriteFrameAsync(stream, Protocol.TypeAuthFail, [], ct);
                Logged?.Invoke($"Refused {peer}: too many wrong codes. Waiting one minute.");
                return;
            }

            var hello = await ReadFrameAsync(stream, HandshakeTimeout, ct);
            if (hello.Type != Protocol.TypeHello
                || hello.Payload.Length != 3
                || hello.Payload[0] != (byte)'G'
                || hello.Payload[1] != (byte)'C'
                || hello.Payload[2] != Protocol.Version)
            {
                await WriteFrameAsync(stream, Protocol.TypeAuthFail, [], ct);
                Logged?.Invoke($"Refused {peer}: not a GameControl phone.");
                return;
            }

            var nonce = PairingAuth.NewNonce();
            await WriteFrameAsync(stream, Protocol.TypeChallenge, nonce, ct);

            var auth = await ReadFrameAsync(stream, HandshakeTimeout, ct);
            var expected = PairingAuth.Hmac(_pairingKey, PairingAuth.Join(nonce, "phone"));
            if (auth.Type != Protocol.TypeAuth || !PairingAuth.Equal(auth.Payload, expected))
            {
                _failures.RecordFailure(DateTimeOffset.UtcNow);
                await WriteFrameAsync(stream, Protocol.TypeAuthFail, [], ct);
                Logged?.Invoke($"Wrong pairing code from {peer}.");
                return;
            }

            _failures.Reset();
            var sessionKey = PairingAuth.Hmac(_pairingKey, PairingAuth.Join(nonce, "session"));
            await WriteFrameAsync(stream, Protocol.TypeAuthOk, [], ct);
            sessionOpen = true;
            SessionChanged?.Invoke(true, peer);
            Logged?.Invoke($"Phone paired from {peer}.");

            uint? lastSeq = null;
            while (!ct.IsCancellationRequested)
            {
                var frame = await ReadFrameAsync(stream, ReceiveTimeout, ct);

                if (frame.Type == Protocol.TypePing && frame.Payload.Length == 4)
                {
                    await WriteFrameAsync(stream, Protocol.TypePong, frame.Payload, ct);
                    continue;
                }

                if (frame.Type != Protocol.TypeState
                    || frame.Payload.Length != Protocol.StatePayloadLength + Protocol.HmacLength)
                {
                    continue;
                }

                var signed = new byte[1 + Protocol.StatePayloadLength];
                signed[0] = Protocol.TypeState;
                frame.Payload.AsSpan(0, Protocol.StatePayloadLength).CopyTo(signed.AsSpan(1));
                var mac = PairingAuth.Hmac(sessionKey, signed);
                if (!PairingAuth.Equal(mac, frame.Payload.AsSpan(Protocol.StatePayloadLength)))
                {
                    Logged?.Invoke("Dropped a state packet with a bad signature.");
                    continue;
                }

                var (seq, report) = Protocol.DecodeState(frame.Payload.AsSpan(0, Protocol.StatePayloadLength));
                if (lastSeq is uint last && seq <= last)
                {
                    continue; // old or replayed packet
                }

                lastSeq = seq;
                StateReceived?.Invoke(report);
            }
        }
        catch (OperationCanceledException) when (ct.IsCancellationRequested)
        {
            // Server is shutting down.
        }
        catch (Exception ex)
        {
            Logged?.Invoke($"Session with {peer} ended: {ex.Message}");
        }
        finally
        {
            if (sessionOpen)
            {
                StateReceived?.Invoke(GamepadReport.Neutral);
                SessionChanged?.Invoke(false, peer);
            }
        }
    }

    private static async Task WriteFrameAsync(Stream stream, byte type, byte[] payload, CancellationToken ct)
    {
        await stream.WriteAsync(Protocol.EncodeFrame(type, payload), ct);
        await stream.FlushAsync(ct);
    }

    private static async Task<Frame> ReadFrameAsync(Stream stream, TimeSpan timeout, CancellationToken ct)
    {
        using var timeoutSource = CancellationTokenSource.CreateLinkedTokenSource(ct);
        timeoutSource.CancelAfter(timeout);
        try
        {
            var lengthBytes = new byte[4];
            await stream.ReadExactlyAsync(lengthBytes, timeoutSource.Token);
            var length = System.Buffers.Binary.BinaryPrimitives.ReadInt32BigEndian(lengthBytes);
            if (length < 1 || length > Protocol.MaxBodyLength)
            {
                throw new InvalidDataException($"Bad frame length {length}.");
            }

            var body = new byte[length];
            await stream.ReadExactlyAsync(body, timeoutSource.Token);
            return new Frame(body[0], body[1..]);
        }
        catch (OperationCanceledException) when (!ct.IsCancellationRequested)
        {
            throw new TimeoutException("No data received from the phone in time.");
        }
    }
}
