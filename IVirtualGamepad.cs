namespace GameControl.Core;

/// <summary>
/// A virtual controller that Windows and games can see. The Xbox 360 implementation
/// (ViGEmBus) lives in the companion app, so this library has no driver dependency.
/// </summary>
public interface IVirtualGamepad : IDisposable
{
    bool IsConnected { get; }

    void Connect();

    void Submit(GamepadReport report);
}
