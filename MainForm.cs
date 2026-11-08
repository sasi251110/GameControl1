using System.Diagnostics;
using System.Drawing;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Windows.Forms;
using GameControl.Core;

namespace GameControl.Companion;

public sealed class MainForm : Form
{
    private const int Port = 27015;

    private readonly string _pairingCode;
    private readonly CompanionServer _server;
    private readonly CancellationTokenSource _cts = new();
    private readonly object _padLock = new();
    private IVirtualGamepad? _gamepad;
    private volatile bool _sessionActive;
    private long _lastFrameTicks;

    private readonly Label _codeValue = new();
    private readonly Label _addressValue = new();
    private readonly Label _statusValue = new();
    private readonly Label _stateValue = new();
    private readonly Label _ageValue = new();
    private readonly Label _logValue = new();
    private readonly CheckBox _outputCheck = new();
    private readonly System.Windows.Forms.Timer _ageTimer = new();

    public MainForm()
    {
        Text = "GameControl Companion";
        StartPosition = FormStartPosition.CenterScreen;
        FormBorderStyle = FormBorderStyle.FixedSingle;
        MaximizeBox = false;
        ClientSize = new Size(580, 380);
        Font = new Font("Segoe UI", 10f);

        _pairingCode = RandomNumberGenerator.GetInt32(0, 1_000_000).ToString("D6");
        _server = new CompanionServer(Port, _pairingCode);

        Controls.Add(MakeLabel("PAIRING CODE", 20, 14, 300, 20, 9f, Color.Gray));
        _codeValue.Text = _pairingCode;
        Place(_codeValue, 20, 34, 300, 60);
        _codeValue.Font = new Font("Consolas", 34f, FontStyle.Bold);

        Place(_addressValue, 20, 104, 540, 22);
        _addressValue.Text = $"Address: {LanAddresses()}    Port: {Port}";

        Place(_statusValue, 20, 134, 540, 22);
        _statusValue.Text = "Waiting for a phone to pair...";

        Place(_stateValue, 20, 166, 540, 22);
        _stateValue.Font = new Font("Consolas", 9.5f);
        _stateValue.Text = "Buttons 0x0000  (no phone connected)";

        Place(_ageValue, 20, 192, 540, 22);

        Place(_outputCheck, 20, 232, 540, 24);
        _outputCheck.Text = "Create a virtual Xbox 360 controller (needs the ViGEmBus driver)";

        Place(_logValue, 20, 290, 540, 60);
        _logValue.ForeColor = Color.Gray;
        _logValue.Text = "";

        Controls.AddRange(new Control[]
        {
            _codeValue, _addressValue, _statusValue, _stateValue,
            _ageValue, _outputCheck, _logValue,
        });

        _server.Logged += message => Post(() => _logValue.Text = message);
        _server.StateReceived += OnStateFromPhone;
        _server.SessionChanged += (connected, peer) => Post(() => ShowSession(connected, peer));
        _outputCheck.CheckedChanged += (_, _) => ToggleOutput();

        Shown += (_, _) => _ = RunServerAsync();
        FormClosing += (_, _) =>
        {
            _cts.Cancel();
            lock (_padLock)
            {
                _gamepad?.Dispose();
                _gamepad = null;
            }
        };

        _ageTimer.Interval = 200;
        _ageTimer.Tick += (_, _) => RefreshAge();
        _ageTimer.Start();
    }

    private static Label MakeLabel(string text, int x, int y, int w, int h, float size, Color color)
    {
        var label = new Label { Text = text, ForeColor = color, Font = new Font("Segoe UI", size) };
        Place(label, x, y, w, h);
        return label;
    }

    private static void Place(Control control, int x, int y, int w, int h)
    {
        control.Location = new Point(x, y);
        control.Size = new Size(w, h);
    }

    private async Task RunServerAsync()
    {
        try
        {
            await _server.RunAsync(_cts.Token);
        }
        catch (Exception ex)
        {
            Post(() => MessageBox.Show(this,
                $"Could not start the server on port {Port}.\n\n{ex.Message}",
                "GameControl", MessageBoxButtons.OK, MessageBoxIcon.Error));
        }
    }

    private void OnStateFromPhone(GamepadReport report)
    {
        lock (_padLock)
        {
            try
            {
                _gamepad?.Submit(report);
            }
            catch (Exception ex)
            {
                Post(() => _logValue.Text = $"Virtual controller error: {ex.Message}");
            }
        }

        Interlocked.Exchange(ref _lastFrameTicks, Stopwatch.GetTimestamp());
        Post(() => _stateValue.Text = FormatReport(report));
    }

    private void ShowSession(bool connected, string peer)
    {
        _sessionActive = connected;
        _statusValue.Text = connected ? $"Phone connected from {peer}" : "Waiting for a phone to pair...";
        if (!connected)
        {
            _stateValue.Text = "Buttons 0x0000  (no phone connected)";
        }
    }

    private void ToggleOutput()
    {
        string? error = null;
        lock (_padLock)
        {
            if (_outputCheck.Checked)
            {
                try
                {
                    var pad = new ViGEmXbox360Gamepad();
                    pad.Connect();
                    _gamepad = pad;
                }
                catch (Exception ex)
                {
                    error = ex.Message;
                }
            }
            else
            {
                _gamepad?.Dispose();
                _gamepad = null;
            }
        }

        if (error is not null)
        {
            _outputCheck.Checked = false;
            MessageBox.Show(this,
                "Could not create the virtual Xbox 360 controller.\n\n" + error +
                "\n\nInstall the ViGEmBus driver (see windows/README.md) and try again.",
                "GameControl", MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }
    }

    private void RefreshAge()
    {
        if (!_sessionActive)
        {
            _ageValue.Text = "";
            return;
        }

        var last = Interlocked.Read(ref _lastFrameTicks);
        var ms = last == 0 ? 0 : (Stopwatch.GetTimestamp() - last) * 1000.0 / Stopwatch.Frequency;
        _ageValue.Text = $"Last packet: {ms:0} ms ago";
    }

    private void Post(Action action)
    {
        if (IsDisposed || !IsHandleCreated)
        {
            return;
        }

        BeginInvoke(action);
    }

    private static string FormatReport(GamepadReport r) =>
        $"Buttons 0x{r.Buttons:X4}   LX {r.LeftX,6}  LY {r.LeftY,6}   RX {r.RightX,6}  RY {r.RightY,6}   LT {r.LeftTrigger,3}  RT {r.RightTrigger,3}";

    private static string LanAddresses()
    {
        try
        {
            var addresses = Dns.GetHostEntry(Dns.GetHostName()).AddressList
                .Where(a => a.AddressFamily == AddressFamily.InterNetwork && !IPAddress.IsLoopback(a))
                .Select(a => a.ToString())
                .ToArray();
            return addresses.Length > 0 ? string.Join(", ", addresses) : "no LAN address found";
        }
        catch (Exception)
        {
            return "unknown";
        }
    }
}
