import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import worldline.profiling.FrameCensus;
import worldline.profiling.ProfilerArtifacts;
import worldline.profiling.ProfilerRegistry;
import worldline.profiling.ProfilerRun;
import worldline.profiling.ProfilerSchema;

/** Synthetic fail-closed coverage for the legacy runtime qualification driver. */
final class LegacyProfilerQualificationSelfTest {
    private LegacyProfilerQualificationSelfTest() {}

    static void execute(Path repository) throws Exception {
        require(Files.isRegularFile(repository.resolve(LegacyProfilerQualificationConfig.FILE)),
                "qualification manifest is absent");
        Path root = Files.createTempDirectory("worldline-legacy-qualification-");
        try {
            LegacyProfilerQualificationConfig config = config();
            Path options = root.resolve("options.cfg");
            Files.writeString(options, "workingDir=old\nstripgenerics=false\nsource=-1\ntarget=-1\n");
            LegacyLoaderWorkspace.patchOptions(options, root.resolve("workspace"));
            String patched = Files.readString(options);
            require(patched.contains("stripgenerics=true") && patched.contains("source=8")
                    && patched.contains("target=8"), "RetroMCP options were not pinned to Java 8");

            Path vanilla = root.resolve("vanilla.jar"), modLoader = root.resolve("modloader.zip");
            writeZip(vanilla, Map.of("base.class", "vanilla", "META-INF/SIGN.SF", "signature"));
            writeZip(modLoader, Map.of("ModLoader.class", "loader", "base.class", "patched"));
            Path combined = root.resolve("combined.jar");
            LegacyLoaderWorkspace.overlayJar(vanilla, modLoader, null, combined);
            try (ZipFile zip = new ZipFile(combined.toFile())) {
                require(zip.getEntry("ModLoader.class") != null && zip.getEntry("META-INF/SIGN.SF") == null,
                        "loader overlay or signature stripping drifted");
                require(new String(zip.getInputStream(zip.getEntry("base.class")).readAllBytes(),
                        StandardCharsets.UTF_8).equals("patched"), "loader overlay precedence drifted");
            }

            Path artifact = root.resolve("proof.wlpr"); writeArtifact(artifact, config);
            String log = "WORLDLINE_LEGACY_LOADER_BOOT=modloader version=ModLoader Beta 1.7.3\n"
                    + "WORLDLINE_PROFILER_ARTIFACT=" + artifact + " frames=1 metrics=6\n"
                    + "WORLDLINE_LEGACY_LOADER_SHUTDOWN=modloader\n";
            LegacyProfilerQualificationProcess.Proof proof =
                    LegacyProfilerQualificationProcess.validate("modloader", 0, log, artifact, config);
            require(proof.frames() == 1 && proof.metrics() == 6, "runtime proof census drifted");
            rejects(() -> LegacyProfilerQualificationProcess.validate(
                    "modloader", 0, log.replace("SHUTDOWN", "STOP"), artifact, config));
        } finally { SafeTreeDelete.delete(root); }
        System.out.println("  legacy profiler qualification self-test: passed");
    }

    private static LegacyProfilerQualificationConfig config() {
        Properties values = new Properties();
        values.setProperty("schema", "worldline.legacy-profiler-qualification.v1");
        values.setProperty("runtime.version", "b1.7.3");
        values.setProperty("client.sha256", "0".repeat(64));
        values.setProperty("modloader.file", "modloader.zip");
        values.setProperty("modloader.sha256", "1".repeat(64));
        values.setProperty("modloader.version", "ModLoader Beta 1.7.3");
        values.setProperty("forge.file", "forge.zip");
        values.setProperty("forge.sha256", "2".repeat(64));
        values.setProperty("forge.version", "1.0.6");
        values.setProperty("capture.frames", "1"); values.setProperty("timeout.seconds", "30");
        values.setProperty("required.metrics", "frame.wall.nanos,client.tick.total.nanos,"
                + "client.tick.max.nanos,client.tick.calls,render.camera.nanos,render.world.nanos");
        return LegacyProfilerQualificationConfig.from(values);
    }

    private static void writeArtifact(Path artifact, LegacyProfilerQualificationConfig config)
            throws Exception {
        ProfilerRegistry registry = ProfilerRegistry.builder()
                .support(config.requiredMetrics().toArray(new String[0])).build();
        ProfilerSchema schema = registry.schema(); long[] row = new long[schema.size() + 2];
        row[1] = 1L;
        FrameCensus census = FrameCensus.of(schema.metricNames(), new long[][] {row});
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("runtime.version", "b1.7.3"); tags.put("driver.id", "modloader-forge");
        tags.put("loader.id", "modloader"); tags.put("capture.reason", "qualification");
        tags.put("scenario.id", "legacy-loader-boot");
        ProfilerArtifacts.write(artifact,
                ProfilerRun.of(schema, census, ProfilerRun.Mode.MIXED, 1L, 2L, tags));
    }

    private static void writeZip(Path path, Map<String, String> entries) throws Exception {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(path))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
            }
        }
    }

    private static void rejects(Throwing action) throws Exception {
        try { action.run(); throw new IllegalStateException("invalid runtime proof was accepted"); }
        catch (IllegalStateException expected) {
            require(!"invalid runtime proof was accepted".equals(expected.getMessage()), expected.getMessage());
        }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private interface Throwing { void run() throws Exception; }
}
