using System.Security.Cryptography;
using System.Text;

namespace GameControl.Core;

/// <summary>Pairing-code key derivation and HMAC helpers. See docs/PROTOCOL.md.</summary>
public static class PairingAuth
{
    public const int Iterations = 100_000;

    private static readonly byte[] Salt = "gamecontrol-v1"u8.ToArray();

    public static byte[] DeriveKey(string pairingCode) =>
        Rfc2898DeriveBytes.Pbkdf2(pairingCode, Salt, Iterations, HashAlgorithmName.SHA256, 32);

    public static byte[] Hmac(byte[] key, byte[] data) => HMACSHA256.HashData(key, data);

    public static byte[] NewNonce() => RandomNumberGenerator.GetBytes(Protocol.NonceLength);

    public static bool Equal(ReadOnlySpan<byte> a, ReadOnlySpan<byte> b) =>
        CryptographicOperations.FixedTimeEquals(a, b);

    /// <summary>Appends an ASCII tag, for example "phone" or "session", to a nonce.</summary>
    public static byte[] Join(byte[] nonce, string tag)
    {
        byte[] suffix = Encoding.ASCII.GetBytes(tag);
        return [.. nonce, .. suffix];
    }
}
