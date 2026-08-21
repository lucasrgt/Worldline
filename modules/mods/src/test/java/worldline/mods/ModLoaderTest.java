package worldline.mods;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public final class ModLoaderTest {
    private static final String VALID = "format=1\nid=worldline.probe\nversion=1.0.0\n"
            + "entrypoint=worldline.benchmark.ProbeMod\nworldline.api=1\nruntime=b1.7.3\n";

    private ModLoaderTest() {}

    public static void main(String[] arguments) throws Exception {
        Path valid = jar(VALID);
        Path invalid = jar(VALID.replace("format=1\n", "format=1\r\n"));
        try {
            ModArtifact artifact = ModLoader.inspect(valid, "b1.7.3", "1");
            require(artifact.compatible() && artifact.descriptor().id().equals("worldline.probe")
                    && artifact.descriptor().version().equals("1.0.0")
                    && artifact.descriptor().entrypoint().equals("worldline.benchmark.ProbeMod")
                    && artifact.sha256().matches("[0-9a-f]{64}"), "valid inspection failed");
            require(ModLoader.inspect(valid, "b1.8", "1").compatibility()
                    == ModCompatibility.RUNTIME_MISMATCH, "runtime mismatch not reported");
            require(ModLoader.inspect(valid, "b1.7.3", "2").compatibility()
                    == ModCompatibility.WORLDLINE_API_MISMATCH, "API mismatch not reported");
            require(ModLoader.inspect(valid, "b1.8", "2").compatibility()
                    == ModCompatibility.RUNTIME_AND_API_MISMATCH, "combined mismatch not reported");
            rejects(() -> ModLoader.inspect(invalid, "b1.7.3", "1"));
            rejects(() -> parse(VALID.replace("id=worldline.probe\nversion=1.0.0",
                    "version=1.0.0\nid=worldline.probe")));
            rejects(() -> parse(VALID.replace("id=worldline.probe\n",
                    "id=worldline.probe\nid=worldline.duplicate\n")));
            rejects(() -> parse(VALID.replace("version=1.0.0\n", "unknown=value\n")));
            rejects(() -> ModDescriptor.parse(new byte[] {(byte) 0xc3, (byte) 0x28}));
            rejects(() -> ModLoader.load(valid, "b1.8", "1", Runnable.class));
            formatTwo();
        } finally {
            Files.deleteIfExists(valid); Files.deleteIfExists(invalid);
        }
        System.out.println("ModLoaderTest passed");
    }

    private static Path jar(String descriptor) throws Exception {
        Path path = Files.createTempFile("worldline-mod-test", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(path))) {
            output.putNextEntry(new JarEntry(ModDescriptor.ENTRY));
            output.write(descriptor.getBytes(StandardCharsets.UTF_8)); output.closeEntry();
        }
        return path;
    }

    private static ModDescriptor parse(String descriptor) {
        return ModDescriptor.parse(descriptor.getBytes(StandardCharsets.UTF_8));
    }

    private static void rejects(Checked action) {
        try { action.run(); throw new AssertionError("invalid mod was accepted"); }
        catch (AssertionError error) { throw error; }
        catch (Exception expected) { }
    }

    private static void formatTwo() {
        String base = "format=2\nid=worldline.probe\nversion=1.0.0\n"
                + "entrypoint=worldline.benchmark.ProbeMod\nworldline.api=1\nruntime=b1.7.3\n";
        ModDescriptor none = parse(base + "requires=\n");
        require(none.requires().isEmpty(), "empty requires must stay empty");
        ModDependency first = parse(base + "requires=worldline.lib,other.mod>=1.2.0\n").requires().get(1);
        require(first.id().equals("other.mod") && first.minVersion().equals("1.2.0")
                && !first.satisfiedBy("1.1.9") && first.satisfiedBy("1.2.0")
                && first.satisfiedBy("2.0.0-beta"), "dependency bounds failed");
        require(parse(base + "requires=worldline.lib\n").requires().get(0).minVersion() == null,
                "bare dependency lost");
        rejects(() -> parse(base.replace("format=2", "format=3") + "requires=\n"));
        rejects(() -> parse(base + "requires=\nextra=x\n"));
        rejects(() -> parse(base));
        rejects(() -> parse(base + "requires=Worldline.Lib\n"));
        rejects(() -> parse(base + "requires=worldline.lib>=not.a.version\n"));
        rejects(() -> parse(base + "requires=worldline.lib,worldline.lib\n"));
    }

    private interface Checked { void run() throws Exception; }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
