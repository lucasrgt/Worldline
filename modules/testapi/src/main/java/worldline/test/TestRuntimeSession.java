package worldline.test;

import worldline.api.AutomatedMinecraftRuntime;

/** One fresh runtime and its adapter-owned resources. */
public interface TestRuntimeSession extends AutoCloseable {
    /** Optional diagnostic HUD in controlled graphical sessions; never advances a game tick. */
    default void showProgress(java.util.List<String> lines) {}

    default AutomatedMinecraftRuntime runtime() {
        throw new IllegalStateException("runtime session does not expose AutomatedMinecraftRuntime");
    }
    default <T> T capability(Class<T> type) {
        if (type == null) throw new NullPointerException("type");
        AutomatedMinecraftRuntime value;
        try { value = runtime(); }
        catch (IllegalStateException unavailable) {
            throw new IllegalStateException("runtime session does not expose capability "
                    + type.getName(), unavailable);
        }
        if (!type.isInstance(value)) {
            throw new IllegalStateException("runtime session does not expose capability " + type.getName());
        }
        return type.cast(value);
    }
    @Override void close();
}
