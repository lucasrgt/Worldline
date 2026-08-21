package worldline.api;

import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Fail-closed Equatable for one vanilla behavior pin or run. Equality is the
 * semantic behavior token plus SHA-256. Lane is provenance only. Milestone
 * numbers never appear on this type.
 */
public final class WorldlineEvidence {
    public static final String VANILLA = "vanilla", MOD = "mod";
    private static final Pattern LANE = Pattern.compile("vanilla|mod");
    private static final Pattern SIGNATURE = Pattern.compile("[a-f0-9]{64}");
    private final WorldlineBehavior behavior;
    private final String lane, signal, signature;

    private WorldlineEvidence(WorldlineBehavior behavior, String lane, String signal, String signature) {
        if (behavior == null) throw new NullPointerException("behavior");
        this.behavior = behavior;
        if (lane == null || !LANE.matcher(lane).matches()) throw new IllegalArgumentException("invalid evidence lane");
        this.lane = lane;
        if (signal == null || signal.isEmpty() || signal.indexOf('\r') >= 0 || signal.indexOf('\n') >= 0
                || "pending".equals(signal))
            throw new IllegalArgumentException("invalid evidence signal");
        this.signal = signal;
        if (signature == null || !SIGNATURE.matcher(signature).matches() || "pending".equals(signature))
            throw new IllegalArgumentException("invalid evidence signature");
        this.signature = signature;
    }

    public static WorldlineEvidence pin(WorldlineBehavior behavior, String signal, String signature) {
        return new WorldlineEvidence(behavior, VANILLA, signal, signature);
    }

    public static WorldlineEvidence pin(String behaviorOrProgress, String signal, String signature) {
        return pin(WorldlineBehavior.require(behaviorOrProgress), signal, signature);
    }

    public static WorldlineEvidence pin(Properties smoke) {
        if (smoke == null) throw new NullPointerException("smoke");
        String named = smoke.getProperty("behavior");
        String progress = smoke.getProperty("id");
        String key = named != null && !named.trim().isEmpty() ? named.trim() : progress;
        return pin(key, property(smoke, "expected.signal"), property(smoke, "expected.signature"));
    }

    public static WorldlineEvidence of(WorldlineBehavior behavior, String lane, String signal, String signature) {
        return new WorldlineEvidence(behavior, lane, signal, signature);
    }

    public static WorldlineEvidence of(String behaviorOrProgress, String lane, String signal, String signature) {
        return of(WorldlineBehavior.require(behaviorOrProgress), lane, signal, signature);
    }

    public WorldlineBehavior behavior() { return behavior; }
    public String token() { return behavior.token(); }
    public String lane() { return lane; }
    public String signal() { return signal; }
    public String signature() { return signature; }

    public WorldlineEvidenceDiff compare(WorldlineEvidence other) {
        if (other == null) throw new NullPointerException("evidence");
        return new WorldlineEvidenceDiff(this, other);
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof WorldlineEvidence)) return false;
        WorldlineEvidence value = (WorldlineEvidence) other;
        return behavior.equals(value.behavior) && signature.equals(value.signature);
    }

    @Override public int hashCode() { return Objects.hash(behavior, signature); }

    private static String property(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("missing " + key);
        return value.trim();
    }
}
