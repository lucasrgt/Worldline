package worldline.api;

/** Controlled runtime that can capture a durable logical snapshot. */
public interface SnapshotMinecraftRuntime extends AutomatedMinecraftRuntime {
    RuntimeSnapshot snapshot();
}
