package worldline.mods;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/** Proves deterministic dependency ordering and fail-closed graph rejection. */
public final class ModGraphTest {
    private ModGraphTest() {}

    public static void main(String[] arguments) throws Exception {
        Path lib = jar("worldline.lib", "1.0.0", "");
        Path core = jar("worldline.core", "2.1.0", "requires=worldline.lib>=1.0.0\n");
        Path app = jar("worldline.app", "0.3.0", "requires=worldline.core,worldline.lib\n");
        Path missing = jar("worldline.missing", "1.0.0", "requires=worldline.absent\n");
        Path old = jar("worldline.old", "1.0.0", "requires=worldline.lib>=2.0.0\n");
        Path self = jar("worldline.self", "1.0.0", "requires=worldline.self\n");
        Path cycleA = jar("worldline.cycle-a", "1.0.0", "requires=worldline.cycle-b\n");
        Path cycleB = jar("worldline.cycle-b", "1.0.0", "requires=worldline.cycle-a\n");
        try {
            List<ModArtifact> resolved = ModGraph.order(Arrays.asList(
                    inspect(app), inspect(core), inspect(lib)));
            require(resolved.size() == 3
                    && resolved.get(0).descriptor().id().equals("worldline.lib")
                    && resolved.get(1).descriptor().id().equals("worldline.core")
                    && resolved.get(2).descriptor().id().equals("worldline.app"),
                    "dependency order was not topological");
            List<ModArtifact> again = ModGraph.order(Arrays.asList(
                    inspect(lib), inspect(app), inspect(core)));
            require(ids(again).equals(ids(resolved)), "ordering is input dependent");
            rejects(() -> ModGraph.order(Arrays.asList(inspect(missing))),
                    "missing dependency was accepted");
            rejects(() -> ModGraph.order(Arrays.asList(inspect(lib), inspect(old))),
                    "unmet minimum version was accepted");
            rejects(() -> ModGraph.order(Arrays.asList(inspect(self))),
                    "self dependency was accepted");
            rejects(() -> ModGraph.order(Arrays.asList(inspect(cycleA), inspect(cycleB))),
                    "dependency cycle was accepted");
            rejects(() -> ModGraph.order(Arrays.asList(inspect(app), inspect(core), inspect(lib),
                    inspect(app))), "duplicate id was accepted");
            rejects(() -> ModGraph.order(null), "null artifact list was accepted");
        } finally {
            for (Path path : Arrays.asList(lib, core, app, missing, old, self, cycleA, cycleB))
                Files.deleteIfExists(path);
        }
        System.out.println("ModGraphTest passed");
    }

    private static List<String> ids(List<ModArtifact> artifacts) {
        return Arrays.asList(artifacts.get(0).descriptor().id(),
                artifacts.get(1).descriptor().id(), artifacts.get(2).descriptor().id());
    }

    private static ModArtifact inspect(Path path) throws Exception {
        return ModLoader.inspect(path, "b1.7.3", "1");
    }

    private static Path jar(String id, String version, String extra) throws Exception {
        StringBuilder descriptor = new StringBuilder("format=2\nid=").append(id)
                .append("\nversion=").append(version)
                .append("\nentrypoint=worldline.benchmark.ProbeMod\nworldline.api=1\nruntime=b1.7.3\n")
                .append(extra.isEmpty() ? "requires=\n" : extra);
        Path path = Files.createTempFile("worldline-graph-test", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(path))) {
            output.putNextEntry(new JarEntry(ModDescriptor.ENTRY));
            output.write(descriptor.toString().getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return path;
    }

    private static void rejects(Checked action, String message) {
        try { action.run(); throw new AssertionError(message); }
        catch (AssertionError error) { throw error; }
        catch (Exception expected) { }
    }

    private interface Checked { void run() throws Exception; }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
