import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Validates the proof, documentation, semantic-map, and behavior-atlas contract of one milestone. */
final class MilestoneContract {
    private static final String HASH = "[0-9a-f]{64}";
    private final Path root, directory, build;
    private final String id;
    private final Properties descriptor = new Properties();

    MilestoneContract(Path root, String id, Path build) throws IOException {
        this.root = root; this.id = id; this.build = build;
        this.directory = root.resolve("smokes").resolve(id).normalize();
        load(directory.resolve("smoke.properties"), descriptor);
    }

    void validate() throws Exception {
        require(id.equals(value("id")), "descriptor id does not match " + id);
        boolean strict = strict();
        if (!tooling() || strict) {
            require(signature().matches(HASH), "expected.signature must be a frozen SHA-256");
            require(!value("expected.signal").isBlank() && !"pending".equals(value("expected.signal")),
                    "expected.signal must be frozen");
        }
        validateMappingHashes();
        validateAtlas(build.resolve("classes/api"));
        validateTestKit(build.resolve("classes/api"), build.resolve("classes/testmodel"),
                build.resolve("classes/testapi"));
        if (strict) validateQualification();
        System.out.println("  milestone contract: proof + docs + semantics + Atlas + TestKit validated");
    }

    void validateEvidence(Path log) throws IOException {
        require(Files.isRegularFile(log), "missing milestone runtime log: " + relative(log));
        String output = Files.readString(log, StandardCharsets.UTF_8);
        require(!output.isBlank(), "milestone runtime produced no evidence");
        if (signature().matches(HASH)) require(output.contains(signature()),
                "runtime log does not contain frozen signature " + signature());
        if (descriptor.getProperty("qualification.schema") != null)
            require(output.contains(value("expected.signal")),
                    "runtime log does not contain the frozen semantic signal");
    }

    boolean officialRuntime() {
        return !"tooling-cycle".equals(descriptor.getProperty("qualification.proof"));
    }

    boolean tooling() { return "tooling".equals(descriptor.getProperty("candidate.kind")); }
    String signature() { return descriptor.getProperty("expected.signature", "").trim(); }

    private void validateQualification() throws IOException {
        require("1".equals(value("qualification.schema")), "new milestones require qualification.schema=1");
        String proof = officialRuntime() ? "official-cycle" : "tooling-cycle";
        require(proof.equals(value("qualification.proof")), "qualification.proof must be " + proof);
        if ("tooling-cycle".equals(proof)) require(descriptor.getProperty("server.jar.sha256") == null
                && descriptor.getProperty("client.jar.sha256") == null,
                "tooling-cycle cannot declare official client/server inputs");
        Path document = qualifiedPath("qualification.docs");
        Path cycle = qualifiedPath("qualification.cycle");
        Path semanticMap = qualifiedPath("qualification.semantic-map");
        require(document.startsWith(root.resolve("docs")), "qualification.docs must live under docs/");
        require(cycle.startsWith(root.resolve("docs")), "qualification.cycle must live under docs/");
        require(semanticMap.equals(directory.resolve("MAP.md")),
                "qualification.semantic-map must be smokes/" + id + "/MAP.md");
        for (Path evidence : List.of(document, cycle, semanticMap)) {
            require(Files.isRegularFile(evidence), "missing qualification artifact: " + relative(evidence));
            require(Files.readString(evidence, StandardCharsets.UTF_8).contains(signature()),
                    relative(evidence) + " does not name the frozen signature");
        }
        require(Files.readString(semanticMap, StandardCharsets.UTF_8).contains(value("expected.signal")),
                relative(semanticMap) + " does not contain the frozen semantic signal");
        String behavior = descriptor.getProperty("behavior");
        String atlas = value("qualification.atlas");
        String testkit = value("qualification.testkit");
        if (behavior == null || behavior.isBlank()) {
            require("not-applicable".equals(atlas),
                    "qualification.atlas must be not-applicable when no behavior is published");
            require(!value("qualification.atlas.reason").isBlank(),
                    "Atlas non-applicability requires qualification.atlas.reason");
            require("not-applicable".equals(testkit),
                    "qualification.testkit must be not-applicable when no behavior is published");
            require(!value("qualification.testkit.reason").isBlank(),
                    "TestKit non-applicability requires qualification.testkit.reason");
        } else {
            require(("atlas.scenario." + behavior.trim()).equals(atlas),
                    "qualification.atlas does not match behavior=" + behavior.trim());
            require("behavior-evidence".equals(testkit),
                    "Atlas behaviors require qualification.testkit=behavior-evidence");
        }
    }

    private void validateMappingHashes() {
        for (String key : descriptor.stringPropertyNames()) {
            String normalized = key.toLowerCase();
            if (normalized.contains("mapping") && normalized.endsWith(".sha256"))
                require(value(key).matches(HASH), key + " must be a frozen SHA-256");
        }
    }

    void validateAtlas(Path apiOutput) throws Exception {
        require(Files.isDirectory(apiOutput), "candidate API output is missing");
        try (URLClassLoader loader = new URLClassLoader(new URL[] {apiOutput.toUri().toURL()}, null)) {
            Class<?> type = Class.forName("worldline.api.WorldlineBehavior", true, loader);
            Method require = type.getMethod("require", String.class);
            Object progress = atlasValue(require, id);
            String explicit = descriptor.getProperty("behavior");
            if (progress != null) require(explicit != null && !explicit.isBlank(),
                    id + " resolves in the behavior Atlas but smoke.properties has no behavior key");
            if (explicit == null || explicit.isBlank()) return;
            Object behavior = atlasValue(require, explicit.trim());
            require(behavior != null, "behavior Atlas has no token " + explicit.trim());
            require(progress != null && progress.equals(behavior),
                    "progress id does not resolve to its Atlas behavior");
            String atlas = (String) type.getMethod("atlasId").invoke(behavior);
            require(atlas.equals("atlas.scenario." + explicit.trim()), "behavior Atlas id drifted: " + atlas);
        }
    }

    void validateTestKit(Path api, Path testmodel, Path testapi) throws Exception {
        String behavior = descriptor.getProperty("behavior");
        if (behavior == null || behavior.isBlank()) return;
        require("behavior-evidence".equals(value("qualification.testkit")),
                "Atlas behavior must declare qualification.testkit=behavior-evidence");
        URL[] urls = {api.toUri().toURL(), testmodel.toUri().toURL(), testapi.toUri().toURL()};
        try (URLClassLoader loader = new URLClassLoader(urls, null)) {
            Class<?> expectation = Class.forName("worldline.test.BehaviorExpectation", true, loader);
            Class<?> atlas = Class.forName("worldline.api.WorldlineBehavior", true, loader);
            expectation.getMethod("toMatchVanilla", atlas, String.class, String.class);
            expectation.getMethod("toMatchVanilla", String.class, String.class, String.class);
        }
    }

    private static Object atlasValue(Method method, String value) throws Exception {
        try { return method.invoke(null, value); }
        catch (InvocationTargetException error) {
            if (error.getCause() instanceof IllegalArgumentException) return null;
            throw error;
        }
    }

    private boolean strict() throws Exception {
        if (descriptor.getProperty("qualification.schema") != null) return true;
        int ordinal = ordinal();
        if (ordinal >= 470) return true;
        String base = System.getenv("WORLDLINE_CANDIDATE_BASE");
        if (base == null || base.isBlank()) return false;
        return status(List.of("git", "cat-file", "-e",
                base + ":smokes/" + id + "/smoke.properties")) != 0;
    }

    private int ordinal() {
        if (!id.startsWith("m")) return -1;
        int end = 1; while (end < id.length() && Character.isDigit(id.charAt(end))) end++;
        try { return Integer.parseInt(id.substring(1, end)); }
        catch (NumberFormatException error) { return -1; }
    }

    private Path qualifiedPath(String key) {
        Path path = root.resolve(value(key)).normalize();
        require(path.startsWith(root), key + " escapes the repository");
        return path;
    }

    private int status(List<String> command) throws Exception {
        Process process = new ProcessBuilder(new ArrayList<>(command)).directory(root.toFile())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            process.destroyForcibly(); throw new IllegalStateException(command.get(0) + " timed out");
        }
        return process.exitValue();
    }

    private String value(String key) { return descriptor.getProperty(key, "").trim(); }
    private String relative(Path path) { return root.relativize(path).toString().replace('\\', '/'); }
    private static void load(Path path, Properties target) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { target.load(reader); }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
