package worldline.mods;

import java.util.Objects;

/** One declared mod dependency: a required id with an optional minimum version. */
public final class ModDependency {
    private static final String MINIMUM = ">=";
    private final String id;
    private final String minVersion;

    private ModDependency(String id, String minVersion) {
        this.id = id; this.minVersion = minVersion;
    }

    /** Parses one canonical {@code id} or {@code id>=x.y.z} token. */
    public static ModDependency parse(String token) {
        if (token == null) throw new NullPointerException("dependency token");
        int cut = token.indexOf(MINIMUM);
        String id = cut < 0 ? token : token.substring(0, cut);
        String minimum = cut < 0 ? null : token.substring(cut + MINIMUM.length());
        require(!id.isEmpty() && ModDescriptor.validId(id), "invalid dependency id");
        if (minimum == null) return new ModDependency(id, null);
        require(ModDescriptor.validVersion(minimum), "invalid dependency minimum version");
        return new ModDependency(id, minimum);
    }

    public String id() { return id; }

    /** Exact declared minimum version, or null when any version satisfies it. */
    public String minVersion() { return minVersion; }

    /** True when {@code version} is present and at least the declared minimum. */
    public boolean satisfiedBy(String version) {
        if (version == null) throw new NullPointerException("version");
        return minVersion == null || compare(version, minVersion) >= 0;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof ModDependency)) return false;
        ModDependency dependency = (ModDependency) other;
        return id.equals(dependency.id)
                && Objects.equals(minVersion, dependency.minVersion);
    }

    @Override public int hashCode() { return Objects.hash(id, minVersion); }

    @Override public String toString() { return minVersion == null ? id : id + MINIMUM + minVersion; }

    static int compare(String left, String right) {
        String[] first = core(left), second = core(right);
        for (int index = 0; index < 3; index++) {
            int difference = Integer.compare(Integer.parseInt(first[index]),
                    Integer.parseInt(second[index]));
            if (difference != 0) return difference;
        }
        boolean leftPre = left.indexOf('-') >= 0, rightPre = right.indexOf('-') >= 0;
        return leftPre == rightPre ? 0 : leftPre ? -1 : 1;
    }

    private static String[] core(String version) {
        String body = version.split("-", -1)[0];
        String[] parts = body.split("\\.", -1);
        require(parts.length == 3, "invalid semantic version: " + version);
        return parts;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
