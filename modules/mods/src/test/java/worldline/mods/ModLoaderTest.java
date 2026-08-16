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

    private interface Checked { void run() throws Exception; }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
