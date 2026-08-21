package worldline.test;

import worldline.api.AutomatedMinecraftRuntime;

/** One fresh runtime and its adapter-owned resources. */
public interface TestRuntimeSession extends AutoCloseable {
    AutomatedMinecraftRuntime runtime();
    @Override void close();
}
