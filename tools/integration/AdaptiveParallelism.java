import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Computes the largest safe supervised Candidate width while runtime remains lease-serialized. */
final class AdaptiveParallelism {
    private AdaptiveParallelism() { }

    static Decision decide(Path root, boolean correction, boolean systemic, int cleanDelta)
            throws Exception {
        Properties values = new Properties();
        Path path = root.resolve("coordination/swarm/parallelism.properties");
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        require("1".equals(values.getProperty("schema")), "invalid parallelism schema");
        int start = integer(values, "candidate.start"), maximum = integer(values, "candidate.max");
        int cpuUnits = integer(values, "candidate.cpu.units");
        long worker = gib(values, "candidate.memory.gib"), reserve = gib(values,
                "host.memory.reserve.gib");
        int processors = Runtime.getRuntime().availableProcessors();
        long free = freeMemory();
        int cpu = Math.max(1, processors / cpuUnits);
        int memory = Math.max(1, (int) (Math.max(0L, free - reserve) / worker));
        int capacity = Math.max(1, Math.min(maximum, Math.min(cpu, memory)));
        int width = systemic && !correction ? 1 : correction ? Math.min(start, capacity)
                : cleanDelta >= 2 ? capacity : Math.min(start, capacity);
        require(width >= 1 && width <= capacity && maximum <= 25,
                "adaptive Candidate width escaped capacity");
        return new Decision(width, capacity, processors, free, worker, reserve,
                integer(values, "runtime.parallelism"));
    }

    static void selfTest() {
        require(Runtime.getRuntime().availableProcessors() > 0 && freeMemory() > 0,
                "host capacity probe is unavailable");
    }

    private static long freeMemory() {
        var bean = ManagementFactory.getOperatingSystemMXBean();
        if (bean instanceof com.sun.management.OperatingSystemMXBean operating) {
            return operating.getFreeMemorySize();
        }
        return Runtime.getRuntime().freeMemory();
    }
    private static int integer(Properties values, String key) {
        return Integer.parseInt(required(values, key));
    }
    private static long gib(Properties values, String key) {
        return Integer.parseInt(required(values, key)) * (1L << 30);
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key);
        require(value != null && !value.isBlank(), "missing " + key);
        return value.trim();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    record Decision(int width, int capacity, int processors, long freeBytes, long workerBytes,
            long reserveBytes, int runtimeParallelism) { }
}
