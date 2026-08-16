package worldline.reproduction;

/** Immutable result of restoring and validating a reproduction bundle. */
public final class ReplayReport {
    private final String runtimeId;
    private final int tick;
    private final String state;

    public ReplayReport(String runtimeId, int tick, String state) {
        if (runtimeId == null) throw new NullPointerException("runtimeId");
        if (state == null) throw new NullPointerException("state");
        if (runtimeId.isEmpty() || tick < 0 || state.isEmpty()) {
            throw new IllegalArgumentException("invalid replay report");
        }
        this.runtimeId = runtimeId; this.tick = tick; this.state = state;
    }

    public String runtimeId() { return runtimeId; }
    public int tick() { return tick; }
    public String state() { return state; }
}
