using GameControl.Core;
using Nefarius.ViGEm.Client;
using Nefarius.ViGEm.Client.Targets;
using Nefarius.ViGEm.Client.Targets.Xbox360;

namespace GameControl.Companion;

/// <summary>
/// Virtual Xbox 360 controller through the ViGEmBus driver. Games see it as a real XInput pad.
/// ViGEmBus is archived upstream and needs to be installed on this PC.
/// </summary>
public sealed class ViGEmXbox360Gamepad : IVirtualGamepad
{
    private ViGEmClient? _client;
    private IXbox360Controller? _pad;
    private GamepadReport? _last;

    public bool IsConnected => _pad is not null;

    public void Connect()
    {
        if (_pad is not null)
        {
            return;
        }

        _client = new ViGEmClient();
        _pad = _client.CreateXbox360Controller();
        _pad.Connect();
    }

    public void Submit(GamepadReport r)
    {
        var pad = _pad ?? throw new InvalidOperationException("Virtual controller is not connected.");
        if (_last is GamepadReport previous && previous == r)
        {
            return; // nothing changed, so skip the driver calls
        }

        _last = r;

        pad.SetButtonState(Xbox360Button.Up, r.IsPressed(ButtonBits.DpadUp));
        pad.SetButtonState(Xbox360Button.Down, r.IsPressed(ButtonBits.DpadDown));
        pad.SetButtonState(Xbox360Button.Left, r.IsPressed(ButtonBits.DpadLeft));
        pad.SetButtonState(Xbox360Button.Right, r.IsPressed(ButtonBits.DpadRight));
        pad.SetButtonState(Xbox360Button.A, r.IsPressed(ButtonBits.Cross));
        pad.SetButtonState(Xbox360Button.B, r.IsPressed(ButtonBits.Circle));
        pad.SetButtonState(Xbox360Button.X, r.IsPressed(ButtonBits.Square));
        pad.SetButtonState(Xbox360Button.Y, r.IsPressed(ButtonBits.Triangle));
        pad.SetButtonState(Xbox360Button.LeftShoulder, r.IsPressed(ButtonBits.L1));
        pad.SetButtonState(Xbox360Button.RightShoulder, r.IsPressed(ButtonBits.R1));
        pad.SetButtonState(Xbox360Button.LeftThumb, r.IsPressed(ButtonBits.L3));
        pad.SetButtonState(Xbox360Button.RightThumb, r.IsPressed(ButtonBits.R3));
        pad.SetButtonState(Xbox360Button.Start, r.IsPressed(ButtonBits.Options));
        pad.SetButtonState(Xbox360Button.Back, r.IsPressed(ButtonBits.Share));

        pad.SetAxisValue(Xbox360Axis.LeftThumbX, r.LeftX);
        pad.SetAxisValue(Xbox360Axis.LeftThumbY, r.LeftY);
        pad.SetAxisValue(Xbox360Axis.RightThumbX, r.RightX);
        pad.SetAxisValue(Xbox360Axis.RightThumbY, r.RightY);

        pad.SetSliderValue(Xbox360Slider.LeftTrigger, r.LeftTrigger);
        pad.SetSliderValue(Xbox360Slider.RightTrigger, r.RightTrigger);
    }

    public void Dispose()
    {
        try
        {
            _pad?.Disconnect();
        }
        catch (Exception)
        {
            // The bus may already be gone. Nothing else to clean up.
        }

        _client?.Dispose();
        _pad = null;
        _client = null;
    }
}
