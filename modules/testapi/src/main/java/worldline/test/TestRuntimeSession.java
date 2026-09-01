package worldline.test;

import worldline.api.AutomatedMinecraftRuntime;

/** One fresh runtime and its adapter-owned resources. */
public interface TestRuntimeSession extends AutoCloseable {
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
