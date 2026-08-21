package worldline.smoke.m13;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import worldline.mods.ModArtifact;
import worldline.mods.ModGraph;
import worldline.mods.ModLoader;

/** Proves deterministic multi-mod ordering and fail-closed graph rejection. */
public final class M13ModGraphSmoke {
    private static final String RUNTIME = "b1.7.3", API = "1";

    private M13ModGraphSmoke() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 1 && "run".equals(arguments[0]), "expected mode run");
        Path lib = jar("worldline.lib", "1.4.0", "requires=\n");
        Path core = jar("worldline.core", "2.1.0", "requires=worldline.lib>=1.0.0\n");
        Path app = jar("worldline.app", "0.3.0", "requires=worldline.core,worldline.lib\n");
        Path missing = jar("worldline.missing", "1.0.0", "requires=worldline.absent\n");
        Path old = jar("worldline.old", "1.0.0", "requires=worldline.lib>=9.0.0\n");
        Path self = jar("worldline.self", "1.0.0", "requires=worldline.self\n");
        Path cycleA = jar("worldline.cycle-a", "1.0.0", "requires=worldline.cycle-b\n");
        Path cycleB = jar("worldline.cycle-b", "1.0.0", "requires=worldline.cycle-a\n");
        try {
            List<String> forward = ids(ModGraph.order(Arrays.asList(
                    inspect(app), inspect(core), inspect(lib))));
            List<String> reverse = ids(ModGraph.order(Arrays.asList(
                    inspect(lib), inspect(app), inspect(core))));
            require(forward.equals(Arrays.asList("worldline.lib", "worldline.core", "worldline.app")),
                    "unexpected forward order: " + forward);
            require(reverse.equals(forward), "ordering depends on input order");
            reject(() -> ModGraph.order(Arrays.asList(inspect(missing))), "missing accepted");
            reject(() -> ModGraph.order(Arrays.asList(inspect(lib), inspect(old))), "old accepted");
            reject(() -> ModGraph.order(Arrays.asList(inspect(self))), "self accepted");
            reject(() -> ModGraph.order(Arrays.asList(inspect(cycleA), inspect(cycleB))),
                    "cycle accepted");
            System.out.println("WORLDLINE_M13_ORDER=" + String.join(",", forward));
            System.out.println("WORLDLINE_M13_REJECTIONS=missing,version,self,cycle");
            System.out.println("WORLDLINE_M13_GRAPH=PASS");
        } finally {
            for (Path path : Arrays.asList(lib, core, app, missing, old, self, cycleA, cycleB))
                Files.deleteIfExists(path);
        }
    }

    private static List<String> ids(List<ModArtifact> artifacts) {
        String[] names = new String[artifacts.size()];
        for (int index = 0; index < names.length; index++) {
            names[index] = artifacts.get(index).descriptor().id();
        }
        return Arrays.asList(names);
    }

    private static void reject(Checked action, String label) {
        try { action.run(); throw new AssertionError(label); }
        catch (AssertionError error) { throw error; }
        catch (Exception expected) { }
    }

    private interface Checked { void run() throws Exception; }

    private static ModArtifact inspect(Path path) throws Exception {
        return ModLoader.inspect(path, RUNTIME, API);
    }

    private static Path jar(String id, String version, String extra) throws Exception {
        StringBuilder descriptor = new StringBuilder("format=2\nid=").append(id)
                .append("\nversion=").append(version)
                .append("\nentrypoint=worldline.benchmark.ProbeMod\nworldline.api=1\nruntime=b1.7.3\n")
                .append(extra);
        Path path = Files.createTempFile("worldline-m13", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(path))) {
            output.putNextEntry(new JarEntry(worldline.mods.ModDescriptor.ENTRY));
            output.write(descriptor.toString().getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return path;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
