package worldline.modtest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import worldline.mods.ModArtifact;
import worldline.mods.ModLoader;
import worldline.trace.CanonicalStateDocument;

public final class ModTestResultTest {
    private ModTestResultTest() {}

    public static void main(String[] arguments) throws Exception {
        Path firstJar = jar("1.0.0", "b1.7.3"), secondJar = jar("1.1.0", "b1.7.3");
        Path incompatibleJar = jar("2.0.0", "b1.8");
        try {
            ModArtifact first = ModLoader.inspect(firstJar, "b1.7.3", "1");
            ModArtifact second = ModLoader.inspect(secondJar, "b1.7.3", "1");
            CanonicalStateDocument left = trace(1), right = trace(9);
            ModTestResult created = ModTestResult.create(first, left);
            ModTestResult parsed = ModTestResult.parse(created.bytes());
            require(created.equals(parsed) && parsed.modId().equals("worldline.version-probe")
                    && parsed.modVersion().equals("1.0.0")
                    && parsed.artifactSha256().equals(first.sha256())
                    && parsed.trace().signature().equals(left.signature()), "result round trip failed");
            ModTestComparison equal = ModTestComparison.compare(created, parsed);
            require(!equal.behaviorDiverged() && equal.sameMod() && equal.sameVersion(),
                    "equal result comparison failed");
            ModTestComparison changed = ModTestComparison.compare(created,
                    ModTestResult.create(second, right));
            require(changed.behaviorDiverged() && changed.sameMod() && !changed.sameVersion()
                    && changed.traceDiff().recordIndex() == 0
                    && changed.traceDiff().field().equals("value"), "version diff failed");
            byte[] corrupt = created.bytes(); corrupt[corrupt.length - 3] ^= 1;
            rejects(() -> ModTestResult.parse(corrupt));
            rejects(() -> ModTestResult.parse(new byte[0]));
            rejects(() -> ModTestResult.create(
                    ModLoader.inspect(incompatibleJar, "b1.7.3", "1"), left));
        } finally {
            Files.deleteIfExists(firstJar); Files.deleteIfExists(secondJar);
            Files.deleteIfExists(incompatibleJar);
        }
        System.out.println("ModTestResultTest passed");
    }

    private static CanonicalStateDocument trace(long value) {
        return CanonicalStateDocument.parse("v2|seed=7|schema=value|tick0=" + value);
    }

    private static Path jar(String version, String runtime) throws Exception {
        Path path = Files.createTempFile("worldline-mod-result-test", ".jar");
        String descriptor = "format=1\nid=worldline.version-probe\nversion=" + version
                + "\nentrypoint=worldline.benchmark.VersionMod\nworldline.api=1\nruntime=" + runtime + "\n";
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(path))) {
            output.putNextEntry(new JarEntry("META-INF/worldline-mod.properties"));
            output.write(descriptor.getBytes(StandardCharsets.UTF_8)); output.closeEntry();
        }
        return path;
    }

    private static void rejects(Checked action) {
        try { action.run(); throw new AssertionError("invalid result was accepted"); }
        catch (AssertionError error) { throw error; }
        catch (Exception expected) { }
    }
    private interface Checked { void run() throws Exception; }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
