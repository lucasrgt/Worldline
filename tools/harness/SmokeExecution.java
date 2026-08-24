import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Observes one canonical smoke execution without changing its behavioral fingerprint. */
final class SmokeExecution {
    private SmokeExecution() { }

    static long run(Path root, SmokeDiscovery.Entry smoke) throws Exception {
        return run(root, smoke, null);
    }

    static long run(Path root, SmokeDiscovery.Entry smoke, Path productRoot) throws Exception {
        return run(root, smoke, productRoot, new HashSet<String>());
    }

    private static long run(Path root, SmokeDiscovery.Entry smoke, Path productRoot,
            Set<String> active) throws Exception {
        if (!active.add(smoke.id)) throw new IllegalStateException(
                "cyclic smoke runtime prerequisite: " + smoke.id);
        preparePrerequisite(root, smoke, productRoot, active);
        long started = System.nanoTime();
        SmokeProcess process = new SmokeProcess(root, productRoot);
        try {
            long duration = process.run(smoke);
            new SmokeScheduleHistory(root).observed(smoke.id, true, duration, process.telemetry());
            return duration;
        } catch (Exception error) {
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            new SmokeScheduleHistory(root).observed(smoke.id, false, elapsed, process.telemetry());
            throw error;
        } finally { active.remove(smoke.id); }
    }

    private static void preparePrerequisite(Path root, SmokeDiscovery.Entry smoke,
            Path productRoot, Set<String> active) throws Exception {
        Properties descriptor = new Properties();
        Path descriptorPath = root.resolve("smokes").resolve(smoke.id).resolve("smoke.properties");
        try (Reader reader = Files.newBufferedReader(descriptorPath, StandardCharsets.UTF_8)) {
            descriptor.load(reader);
        }
        String required = descriptor.getProperty("runtime.requires", "").trim();
        if (required.isEmpty()) return;
        if (!required.matches("[a-z0-9]+(?:-[a-z0-9]+)*"))
            throw new IllegalStateException("invalid runtime prerequisite: " + required);
        Path dependencyBuild = root.resolve(".worldline/smokes").resolve(required).normalize();
        String[] outputs = descriptor.getProperty("runtime.requires.outputs", "").split(",");
        boolean ready = outputs.length > 0;
        for (String output : outputs) {
            String name = output.trim();
            if (name.isEmpty() || name.contains("/") || name.contains("\\"))
                throw new IllegalStateException("invalid runtime prerequisite output: " + output);
            ready &= Files.exists(dependencyBuild.resolve(name));
        }
        if (ready) return;
        System.out.println("  preparing runtime prerequisite: " + required + " -> " + smoke.id);
        run(root, SmokeDiscovery.require(root, required), productRoot, active);
    }
}
