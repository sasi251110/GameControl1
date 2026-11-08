namespace GameControl.Core;

/// <summary>After repeated wrong pairing codes, refuses new attempts for a short time.</summary>
public sealed class FailureTracker
{
    public const int MaxFailures = 5;
    public static readonly TimeSpan LockoutDuration = TimeSpan.FromSeconds(60);

    private readonly object _gate = new();
    private int _failures;
    private DateTimeOffset _lockedUntil = DateTimeOffset.MinValue;

    public bool IsLocked(DateTimeOffset now)
    {
        lock (_gate)
        {
            return now < _lockedUntil;
        }
    }

    public void RecordFailure(DateTimeOffset now)
    {
        lock (_gate)
        {
            _failures++;
            if (_failures >= MaxFailures)
            {
                _lockedUntil = now + LockoutDuration;
                _failures = 0;
            }
        }
    }

    public void Reset()
    {
        lock (_gate)
        {
            _failures = 0;
        }
    }
}
