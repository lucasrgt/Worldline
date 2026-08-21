package worldline.minimization;

import java.util.Objects;

/** One typed, bounded scenario step in the public adapter-neutral DSL. */
public final class ScenarioStep {
    /** Bounded step verbs shared by every conforming adapter. */
    public enum Kind { TICK, RESEED, TAP, OBSERVE, BLOCK }

    public static final int MAX_TICKS = 4096, MAX_KEY = 255;
    public static final int MAX_COORDINATE = 33_554_432, MAX_BLOCK_ID = 255, MAX_METADATA = 15;
    private static final String NUMBER = "-?(0|[1-9][0-9]*)";

    private final Kind kind;
    private final int first, second, third, fourth, fifth;
    private final long value;
    private final String label;

    private ScenarioStep(Kind kind, int first, int second, int third, int fourth, int fifth,
            long value, String label) {
        this.kind = kind; this.first = first; this.second = second; this.third = third;
        this.fourth = fourth; this.fifth = fifth; this.value = value; this.label = label;
    }

    public static ScenarioStep tick(int count) {
        require(count >= 1 && count <= MAX_TICKS, "invalid tick count");
        return new ScenarioStep(Kind.TICK, count, 0, 0, 0, 0, 0L, null);
    }

    public static ScenarioStep reseed(long seed) {
        return new ScenarioStep(Kind.RESEED, 0, 0, 0, 0, 0, seed, null);
    }

    public static ScenarioStep tap(int key) {
        require(key >= 0 && key <= MAX_KEY, "invalid key code");
        return new ScenarioStep(Kind.TAP, key, 0, 0, 0, 0, 0L, null);
    }

    public static ScenarioStep observe(String label) {
        require(label != null && label.matches("[a-z0-9_]{1,32}"), "invalid observe label");
        return new ScenarioStep(Kind.OBSERVE, 0, 0, 0, 0, 0, 0L, label);
    }

    public static ScenarioStep block(int x, int y, int z, int id, int metadata) {
        require(bounded(x) && bounded(y) && bounded(z), "invalid block coordinate");
        require(id >= 0 && id <= MAX_BLOCK_ID, "invalid legacy block id");
        require(metadata >= 0 && metadata <= MAX_METADATA, "invalid block metadata");
        return new ScenarioStep(Kind.BLOCK, x, y, z, id, metadata, 0L, null);
    }

    public Kind kind() { return kind; }
    public int count() { require(kind == Kind.TICK, "count requires a tick step"); return first; }
    public long seed() { require(kind == Kind.RESEED, "seed requires a reseed step"); return value; }
    public int key() { require(kind == Kind.TAP, "key requires a tap step"); return first; }
    public String label() { require(kind == Kind.OBSERVE, "label requires an observe step"); return label; }
    public int x() { require(kind == Kind.BLOCK, "x requires a block step"); return first; }
    public int y() { require(kind == Kind.BLOCK, "y requires a block step"); return second; }
    public int z() { require(kind == Kind.BLOCK, "z requires a block step"); return third; }
    public int blockId() { require(kind == Kind.BLOCK, "id requires a block step"); return fourth; }
    public int metadata() { require(kind == Kind.BLOCK, "metadata requires a block step"); return fifth; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof ScenarioStep)) return false;
        ScenarioStep step = (ScenarioStep) other;
        return kind == step.kind && first == step.first && second == step.second
                && third == step.third && fourth == step.fourth && fifth == step.fifth
                && value == step.value && Objects.equals(label, step.label);
    }

    @Override public int hashCode() {
        return Objects.hash(kind, first, second, third, fourth, fifth, value, label);
    }

    private static boolean bounded(int coordinate) {
        return coordinate >= -MAX_COORDINATE && coordinate <= MAX_COORDINATE;
    }

    static boolean number(String text) { return text.matches(NUMBER); }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
