package worldline.profiling;

import java.util.Objects;

/** One typed, owned metric in a Worldline Profiler frame schema. */
public final class ProfilerMetric {
    public enum Unit { NANOSECONDS, BYTES, COUNT, RATIO_PPM }
    public enum Kind { DURATION, DELTA, GAUGE }
    public enum Causality { ROOT, TOP_LEVEL, NESTED, DIAGNOSTIC }
    public enum Category { FRAME, CLIENT, WORLD, CHUNK, RENDER, DISPLAY, JVM, IO,
        NETWORK, THREAD, STREAMING, GPU, AUDIO, INPUT, TASK, MOD }

    private final String name, owner;
    private final Unit unit;
    private final Kind kind;
    private final Causality causality;

    private ProfilerMetric(String name, String owner, Unit unit, Kind kind,
            Causality causality) {
        this.name = name; this.owner = owner; this.unit = unit;
        this.kind = kind; this.causality = causality;
    }

    public static ProfilerMetric of(String name, String owner, Unit unit, Kind kind,
            Causality causality) {
        require(name != null && name.matches("[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)*"),
                "invalid profiler metric name: " + name);
        require(owner != null && owner.matches("[a-z][a-z0-9-]{0,63}"),
                "invalid profiler metric owner: " + owner);
        require("worldline".equals(owner) || name.startsWith("mod."),
                "non-Worldline metric must use the mod namespace: " + name);
        if (unit == null || kind == null || causality == null)
            throw new NullPointerException("profiler metric metadata");
        require(kind != Kind.DURATION || unit == Unit.NANOSECONDS,
                "duration metric must use nanoseconds: " + name);
        require(causality != Causality.TOP_LEVEL || kind == Kind.DURATION,
                "top-level metric must be a duration: " + name);
        return new ProfilerMetric(name, owner, unit, kind, causality);
    }

    public String name() { return name; }
    public String owner() { return owner; }
    public Unit unit() { return unit; }
    public Kind kind() { return kind; }
    public Causality causality() { return causality; }
    public boolean extensionOwned() { return !"worldline".equals(owner); }
    public Category category() {
        if (extensionOwned()) return Category.MOD;
        String prefix = name.substring(0, name.indexOf('.'));
        if ("frame".equals(prefix)) return Category.FRAME;
        if ("client".equals(prefix)) return Category.CLIENT;
        if ("world".equals(prefix)) return Category.WORLD;
        if ("chunk".equals(prefix)) return Category.CHUNK;
        if ("render".equals(prefix)) return Category.RENDER;
        if ("display".equals(prefix)) return Category.DISPLAY;
        if ("jvm".equals(prefix)) return Category.JVM;
        if ("io".equals(prefix)) return Category.IO;
        if ("network".equals(prefix)) return Category.NETWORK;
        if ("thread".equals(prefix)) return Category.THREAD;
        if ("streaming".equals(prefix)) return Category.STREAMING;
        if ("gpu".equals(prefix)) return Category.GPU;
        if ("audio".equals(prefix)) return Category.AUDIO;
        if ("input".equals(prefix)) return Category.INPUT;
        if ("task".equals(prefix)) return Category.TASK;
        throw new IllegalStateException("uncategorized Worldline metric: " + name);
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProfilerMetric)) return false;
        ProfilerMetric value = (ProfilerMetric) other;
        return name.equals(value.name) && owner.equals(value.owner) && unit == value.unit
                && kind == value.kind && causality == value.causality;
    }

    @Override public int hashCode() { return Objects.hash(name, owner, unit, kind, causality); }
    @Override public String toString() { return name + "[" + unit + "," + owner + "]"; }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
