package worldline.api;

/** Incremental remote-world session that sustains the vanilla play heartbeat. */
public interface SustainedRemoteWorldMultiplayerSession extends IncrementalRemoteWorldMultiplayerSession {
    RemoteWorldView sustainTicks(int ticks);
}
