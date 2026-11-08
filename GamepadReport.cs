namespace GameControl.Core;

/// <summary>Button bit positions. They match the order of the Android ControllerButton enum.</summary>
public static class ButtonBits
{
    public const int DpadUp = 0;
    public const int DpadDown = 1;
    public const int DpadLeft = 2;
    public const int DpadRight = 3;
    public const int Cross = 4;
    public const int Circle = 5;
    public const int Square = 6;
    public const int Triangle = 7;
    public const int L1 = 8;
    public const int R1 = 9;
    public const int L2 = 10;
    public const int R2 = 11;
    public const int L3 = 12;
    public const int R3 = 13;
    public const int Options = 14;
    public const int Share = 15;
}

/// <summary>One controller state as received from the phone.</summary>
public readonly record struct GamepadReport(
    ushort Buttons,
    short LeftX,
    short LeftY,
    short RightX,
    short RightY,
    byte LeftTrigger,
    byte RightTrigger)
{
    public static readonly GamepadReport Neutral = new(0, 0, 0, 0, 0, 0, 0);

    public bool IsPressed(int bit) => (Buttons & (1 << bit)) != 0;
}
