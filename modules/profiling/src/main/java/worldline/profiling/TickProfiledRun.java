package worldline.profiling;

import worldline.trace.CanonicalStateDocument;

/** One profiled execution: per-tick timing samples plus its behavioral trace. */
public final class TickProfiledRun {
    private final TickProfile profile;
    private final CanonicalStateDocument trace;

    public TickProfiledRun(TickProfile profile, CanonicalStateDocument trace) {
        if (profile == null || trace == null) throw new NullPointerException("profiled run");
        this.profile = profile; this.trace = trace;
    }

    public TickProfile profile() { return profile; }

    /** Behavioral record of the same execution; timings never enter traces. */
    public CanonicalStateDocument trace() { return trace; }
}
