import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** Partitions smoke misses into pool-safe and chain-bound work under one runtime lease. */
final class SmokeSuiteScheduler {
    private SmokeSuiteScheduler() { }

    record Plan(List<SmokeDiscovery.Entry> pooled, List<SmokeDiscovery.Entry> chained) { }

    /** Keeps every smoke that declares or serves a runtime prerequisite on the serial chain. */
    static Plan plan(Path root, List<SmokeDiscovery.Entry> catalog,
            List<SmokeDiscovery.Entry> misses) throws Exception {
        Set<String> chainMembers = new HashSet<>();
        for (SmokeDiscovery.Entry smoke : catalog) {
            String required = descriptor(root, smoke.id).getProperty("runtime.requires", "").trim();
            if (required.isEmpty()) continue;
            chainMembers.add(smoke.id);
            chainMembers.add(required);
        }
        List<SmokeDiscovery.Entry> pooled = new ArrayList<>(), chained = new ArrayList<>();
        for (SmokeDiscovery.Entry smoke : misses)
            (chainMembers.contains(smoke.id) ? chained : pooled).add(smoke);
        return new Plan(List.copyOf(pooled), List.copyOf(chained));
    }

    /** Bounded width from the reviewed host-pool admission; anything doubtful stays serial. */
    static int width(Path root, int eligible) {
        if (eligible < 2) return 1;
        Properties pool;
        try {
            pool = StrictProperties.load(root.resolve("tools/containers/host-pool.properties"));
        } catch (Exception error) {
            return 1;
        }
        int ceiling = bounded(pool.getProperty("max.parallelism", "25"), 25, "max.parallelism");
        int admitted = bounded(pool.getProperty("parallelism", "1"), ceiling, "parallelism");
        String override = System.getenv("WORLDLINE_SMOKE_PARALLELISM");
        int width = override == null || override.isBlank() ? admitted
                : bounded(override, ceiling, "WORLDLINE_SMOKE_PARALLELISM");
        return Math.min(width, eligible);
    }

    private static int bounded(String value, int ceiling, String name) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed >= 1 && parsed <= ceiling) return parsed;
        } catch (NumberFormatException ignored) { }
        throw new IllegalStateException(name + " must be an integer between 1 and " + ceiling);
    }

    private static Properties descriptor(Path root, String id) throws Exception {
        return StrictProperties.load(root.resolve("smokes").resolve(id).resolve("smoke.properties"));
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-smoke-scheduler-");
        try {
            write(root, "m1-alpha", "");
            write(root, "m2-beta", "runtime.requires=m3-gamma\nruntime.requires.outputs=out\n");
            write(root, "m3-gamma", "");
            write(root, "m4-delta", "");
            List<SmokeDiscovery.Entry> catalog = List.of(entry("m1-alpha"), entry("m2-beta"),
                    entry("m3-gamma"), entry("m4-delta"));
            Plan plan = plan(root, catalog, catalog);
            require(identifiers(plan.pooled()).equals(List.of("m1-alpha", "m4-delta"))
                            && identifiers(plan.chained()).equals(List.of("m2-beta", "m3-gamma")),
                    "smoke chain partition drifted");
            Plan partial = plan(root, catalog, List.of(entry("m3-gamma"), entry("m4-delta")));
            require(identifiers(partial.pooled()).equals(List.of("m4-delta"))
                            && identifiers(partial.chained()).equals(List.of("m3-gamma")),
                    "prerequisite target escaped the serial chain");
            Files.writeString(root.resolve("tools/containers/host-pool.properties"),
                    "parallelism=10\nmax.parallelism=25\n", StandardCharsets.UTF_8);
            require(width(root, 1) == 1 && width(root, 4) == 4 && width(root, 100) == 10,
                    "smoke pool width admission drifted");
            boolean rejected = false;
            try {
                bounded("26", 25, "max.parallelism");
            } catch (IllegalStateException expected) {
                rejected = true;
            }
            require(rejected, "out-of-bounds pool width was accepted");
            System.out.println("  smoke suite scheduler self-test: passed");
        } finally {
            SafeTreeDelete.delete(root);
        }
    }

    private static List<String> identifiers(List<SmokeDiscovery.Entry> entries) {
        return entries.stream().map(entry -> entry.id).toList();
    }

    private static SmokeDiscovery.Entry entry(String id) {
        return new SmokeDiscovery.Entry(id, "tools/smoke/DataDrivenCycle.java");
    }

    private static void write(Path root, String id, String extra) throws Exception {
        Path directory = root.resolve("smokes").resolve(id);
        Files.createDirectories(directory);
        Files.createDirectories(root.resolve("tools/containers"));
        Files.writeString(directory.resolve("smoke.properties"),
                "schema=1\n" + extra, StandardCharsets.UTF_8);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
