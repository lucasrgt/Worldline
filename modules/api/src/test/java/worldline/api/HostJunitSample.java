package worldline.api;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import worldline.api.query.ClientTopology;
import worldline.api.query.EntityQuery;
import worldline.api.query.EventQuery;
import worldline.api.query.WeatherQuery;

/** Real host tests discovered by the JUnit engine. */
public final class HostJunitSample {
    public HostJunitSample() { }

    @Test
    public void sha256KnownFixture() throws Exception {
        String actual = WorldlineJunitEngine.sha256Text("abc");
        if (!"ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad".equals(actual))
            throw new AssertionError("shipped SHA-256 helper drifted: " + actual);
    }

    @Test
    public void apiSurfaceIsPartitioned() {
        ApiSurfaceDoctor.verify();
        EntityQuery entities = EntityQuery.ofType("minecraft:pig");
        EventQuery events = EventQuery.ofEffect(1002);
        WeatherQuery weather = WeatherQuery.clear();
        ClientTopology topology = ClientTopology.of("alpha", "beta");
        if (!entities.isEmpty() || events.size() != 0 || !weather.clearSkies()
                || topology.size() != 2)
            throw new AssertionError("general query primitives drifted");
    }

    @Test
    public void plantedDuplicationFailsClosed() throws Exception {
        Path root = Files.createTempDirectory("worldline-dup-plant-");
        try {
            Path planted = root.resolve("smokes").resolve("new").resolve("Bad.java");
            Files.createDirectories(planted.getParent());
            Files.writeString(planted, "class Bad { void x() throws Exception {\n"
                    + "MessageDigest.getInstance(\"SHA-" + "256\");\n"
                    + "for (int lift = 0; lift < " + "8; lift++) {}\n"
                    + "Thread." + "sleep(1L);\n"
                    + "new B173Dedicated" + "Server(jar, dir, 1, 1L, t, 3, true);\n"
                    + "}}\n", StandardCharsets.UTF_8);
            Process process = new ProcessBuilder("java",
                    "-cp", Path.of(".worldline/gate/classes").toString(),
                    "DuplicatePatternCheck", root.toString(), "--plant-scan")
                    .redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
            if (process.waitFor() == 0)
                throw new AssertionError("planted duplication was accepted: " + output);
            if (!output.contains("planted duplication survived"))
                throw new AssertionError("plant-scan did not fail closed: " + output);
        } finally {
            Files.walk(root).sorted((a, b) -> b.compareTo(a)).forEach(path -> {
                try { Files.deleteIfExists(path); } catch (Exception ignored) { }
            });
        }
    }
}
