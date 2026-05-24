package log;

public final class Logger
{
    private static final LogWindowSource defaultLogSource;
    private static final ThreadLocal<Boolean> isLogging = ThreadLocal.withInitial(() -> false);

    static {
        defaultLogSource = new LogWindowSource(100);
    }

    private Logger()
    {
    }

    public static void debug(String strMessage)
    {
        defaultLogSource.append(LogLevel.Debug, strMessage);
    }

    public static void error(String strMessage)
    {
        defaultLogSource.append(LogLevel.Error, strMessage);
    }

    public static void logFunction(String functionName)
    {
        if (isLogging.get()) {
            return;
        }
        isLogging.set(true);
        try {
            defaultLogSource.append(LogLevel.Info, functionName);
        } finally {
            isLogging.set(false);
        }
    }

    public static LogWindowSource getDefaultLogSource()
    {
        return defaultLogSource;
    }
}