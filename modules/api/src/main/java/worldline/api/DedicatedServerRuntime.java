package worldline.api;

/** Caller-controlled lifecycle and persistence boundary for a dedicated server. */
public interface DedicatedServerRuntime extends AutoCloseable {
    void boot();

    void setTime(long worldTime);

    void save();

    ServerState state();

    @Override
    void close();
}
