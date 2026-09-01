import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Java-owned, secret-free policy for one executor environment profile. */
public final class ExecutionEnvironmentManifest {
    private static final String SCHEMA = "worldline.execution-environment-manifest.v1";
    private static final String JAVA_OPTIONS = "-Xms16m -Xmx128m -XX:-UsePerfData";
    private final String profile;
    private final List<Entry> entries;

    private ExecutionEnvironmentManifest(String profile, List<Entry> entries) {
        require(profile.matches("[a-z0-9]+(?:-[a-z0-9]+)*"), "invalid environment profile");
        this.profile = profile; this.entries = entries.stream()
                .sorted(java.util.Comparator.comparing(Entry::name)).toList(); validate();
    }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 0, "usage: ExecutionEnvironmentManifest");
            Path root = Path.of("").toAbsolutePath().normalize();
            System.out.write(tooling(root.resolve(".worldline/executor-environment/query-temp")).descriptor());
        } catch (Exception error) {
            System.err.println("execution environment manifest failed: " + error.getMessage()); System.exit(1);
        }
    }

    static ExecutionEnvironmentManifest tooling(Path temporary) {
        Path clean = temporary.toAbsolutePath().normalize();
        require(temporary.isAbsolute() && clean.equals(temporary), "temporary path is not absolute and normalized");
        return new ExecutionEnvironmentManifest("tooling-cycle", List.of(
                Entry.set("JAVA_TOOL_OPTIONS", Effect.TOOL, JAVA_OPTIONS),
                Entry.set("TEMP", Effect.TOOL, clean.toString()),
                Entry.set("TMP", Effect.TOOL, clean.toString())));
    }

    static ExecutionEnvironmentManifest hostile(Path temporary) {
        Path clean = temporary.toAbsolutePath().normalize();
        require(temporary.isAbsolute() && clean.equals(temporary), "hostile temporary path is not normalized");
        return new ExecutionEnvironmentManifest("hostile-fixtures", List.of(
                Entry.inherit("PATH", Effect.TOOL), Entry.inherit("PATHEXT", Effect.TOOL),
                Entry.inherit("SYSTEMROOT", Effect.TOOL), Entry.inherit("WINDIR", Effect.TOOL),
                Entry.set("TEMP", Effect.TOOL, clean.toString()), Entry.set("TMP", Effect.TOOL, clean.toString()),
                Entry.set("WORLDLINE_FIXTURE_VALUE", Effect.BEHAVIOR, "injected-value")));
    }

    static ExecutionEnvironmentManifest serverHeadless(Path temporary, Path control,
            Path leaseFile, String leasePid, String leaseStarted) {
        Path clean = temporary.toAbsolutePath().normalize();
        require(clean.equals(temporary) && control.isAbsolute() && leaseFile.isAbsolute(),
                "server environment paths are not absolute and normalized");
        require(leasePid.matches("[0-9]+"), "invalid runtime lease identity");
        require(leaseStarted.matches("[0-9]+"), "invalid runtime lease start identity");
        return new ExecutionEnvironmentManifest("server-headless", List.of(
                Entry.inherit("PATH", Effect.TOOL),
                Entry.inherit("PATHEXT", Effect.TOOL),
                Entry.inherit("SYSTEMROOT", Effect.TOOL),
                Entry.inherit("WINDIR", Effect.TOOL),
                Entry.set("JAVA_TOOL_OPTIONS", Effect.TOOL,
                        "-Xms16m -Xmx512m -XX:-UsePerfData"),
                Entry.set("TEMP", Effect.TOOL, clean.toString()),
                Entry.set("TMP", Effect.TOOL, clean.toString()),
                Entry.set("WORLDLINE_CONTROL_DIR", Effect.TOOL, control.toString()),
                Entry.set("WORLDLINE_RUNTIME_DIFFERENTIAL", Effect.BEHAVIOR, "1"),
                Entry.set("WORLDLINE_RUNTIME_PARENT_LEASE", Effect.TOOL, leasePid),
                Entry.set("WORLDLINE_RUNTIME_PARENT_STARTED", Effect.TOOL, leaseStarted),
                Entry.set("WORLDLINE_RUNTIME_POOL_FILE", Effect.TOOL, leaseFile.toString()),
                Entry.set("WORLDLINE_SMOKE_CACHE", Effect.BEHAVIOR, "off")));
    }

    static ExecutionEnvironmentManifest candidate(Path temporary, Path control) {
        Path clean = temporary.toAbsolutePath().normalize();
        require(clean.equals(temporary) && control.isAbsolute() && control.normalize().equals(control),
                "Candidate environment paths are not absolute and normalized");
        return new ExecutionEnvironmentManifest("candidate-worker", List.of(
                Entry.inherit("PATH", Effect.TOOL), Entry.inherit("PATHEXT", Effect.TOOL),
                Entry.inherit("SYSTEMROOT", Effect.TOOL), Entry.inherit("WINDIR", Effect.TOOL),
                Entry.set("JAVA_TOOL_OPTIONS", Effect.TOOL, "-Xms16m -Xmx1024m -XX:-UsePerfData"),
                Entry.set("TEMP", Effect.TOOL, clean.toString()), Entry.set("TMP", Effect.TOOL, clean.toString()),
                Entry.set("WORLDLINE_BUILD_WORKERS", Effect.TOOL, "4"),
                Entry.set("WORLDLINE_CONTROL_DIR", Effect.TOOL, control.toString()),
                Entry.set("WORLDLINE_GATE_WAIT_SECONDS", Effect.TOOL, "600")));
    }

    static ExecutionEnvironmentManifest campaign(Path temporary, Path executorPlan) {
        Path clean = temporary.toAbsolutePath().normalize();
        require(clean.equals(temporary) && executorPlan.isAbsolute()
                && executorPlan.normalize().equals(executorPlan), "campaign environment paths are not normalized");
        List<Entry> values = new ArrayList<>(List.of(Entry.inherit("PATH", Effect.TOOL),
                Entry.set("JAVA_TOOL_OPTIONS", Effect.TOOL,
                        "-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -XX:-UsePerfData"),
                Entry.set("TEMP", Effect.TOOL, clean.toString()), Entry.set("TMP", Effect.TOOL, clean.toString()),
                Entry.set("WORLDLINE_CAMPAIGN_EXECUTOR_PLAN", Effect.OBSERVATIONAL, executorPlan.toString())));
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            values.add(Entry.inherit("PATHEXT", Effect.TOOL)); values.add(Entry.inherit("SYSTEMROOT", Effect.TOOL));
            values.add(Entry.inherit("WINDIR", Effect.TOOL));
        }
        copyNumeric(values, "WORLDLINE_NIGHTLY_FUZZ_CASES");
        copyNumeric(values, "WORLDLINE_NIGHTLY_MUTATION_CASES");
        return new ExecutionEnvironmentManifest("nightly-campaign", values);
    }

    static ExecutionEnvironmentManifest guiNative(Path temporary, Path control,
            Path leaseFile, String leasePid, String leaseStarted) {
        Path clean = temporary.toAbsolutePath().normalize();
        require(clean.equals(temporary) && control.isAbsolute() && leaseFile.isAbsolute(),
                "GUI environment paths are not absolute and normalized");
        require(leasePid.matches("[0-9]+"), "invalid runtime lease identity");
        require(leaseStarted.matches("[0-9]+"), "invalid runtime lease start identity");
        return new ExecutionEnvironmentManifest("gui-native", List.of(
                Entry.inherit("PATH", Effect.TOOL), Entry.inherit("PATHEXT", Effect.TOOL),
                Entry.inherit("SYSTEMROOT", Effect.TOOL), Entry.inherit("WINDIR", Effect.TOOL),
                Entry.set("JAVA_TOOL_OPTIONS", Effect.TOOL, "-Xms16m -Xmx512m -XX:-UsePerfData"),
                Entry.set("TEMP", Effect.TOOL, clean.toString()), Entry.set("TMP", Effect.TOOL, clean.toString()),
                Entry.set("WORLDLINE_CONTROL_DIR", Effect.TOOL, control.toString()),
                Entry.set("WORLDLINE_RUNTIME_DIFFERENTIAL", Effect.BEHAVIOR, "1"),
                Entry.set("WORLDLINE_RUNTIME_PARENT_LEASE", Effect.TOOL, leasePid),
                Entry.set("WORLDLINE_RUNTIME_PARENT_STARTED", Effect.TOOL, leaseStarted),
                Entry.set("WORLDLINE_RUNTIME_POOL_FILE", Effect.TOOL, leaseFile.toString()),
                Entry.set("WORLDLINE_SMOKE_CACHE", Effect.BEHAVIOR, "off")));
    }

    static ExecutionEnvironmentManifest guiAero(Path temporary, Path gradleHome, Path control,
            Path leaseFile, String leasePid, String leaseStarted) {
        Path clean = temporary.toAbsolutePath().normalize();
        require(clean.equals(temporary) && gradleHome.isAbsolute() && gradleHome.normalize().equals(gradleHome)
                && control.isAbsolute() && leaseFile.isAbsolute(),
                "Aero environment paths are not absolute and normalized");
        require(leasePid.matches("[0-9]+"), "invalid runtime lease identity");
        require(leaseStarted.matches("[0-9]+"), "invalid runtime lease start identity");
        return new ExecutionEnvironmentManifest("gui-aero", List.of(
                Entry.inherit("PATH", Effect.TOOL), Entry.inherit("PATHEXT", Effect.TOOL),
                Entry.inherit("SYSTEMROOT", Effect.TOOL), Entry.inherit("WINDIR", Effect.TOOL),
                Entry.set("GRADLE_USER_HOME", Effect.TOOL, gradleHome.toString()),
                Entry.set("JAVA_TOOL_OPTIONS", Effect.TOOL, "-Xms16m -Xmx2048m -XX:-UsePerfData"),
                Entry.set("TEMP", Effect.TOOL, clean.toString()), Entry.set("TMP", Effect.TOOL, clean.toString()),
                Entry.set("WORLDLINE_CONTROL_DIR", Effect.TOOL, control.toString()),
                Entry.set("WORLDLINE_RUNTIME_DIFFERENTIAL", Effect.BEHAVIOR, "1"),
                Entry.set("WORLDLINE_RUNTIME_PARENT_LEASE", Effect.TOOL, leasePid),
                Entry.set("WORLDLINE_RUNTIME_PARENT_STARTED", Effect.TOOL, leaseStarted),
                Entry.set("WORLDLINE_RUNTIME_POOL_FILE", Effect.TOOL, leaseFile.toString()),
                Entry.set("WORLDLINE_SMOKE_CACHE", Effect.BEHAVIOR, "off")));
    }

    WorldlineExecutionPlan.Environment environment() {
        List<String> inherit = new ArrayList<>(), remove = new ArrayList<>();
        Map<String, String> set = new TreeMap<>();
        for (Entry entry : entries) switch (entry.mode) {
            case INHERIT -> inherit.add(entry.name); case REMOVE -> remove.add(entry.name);
            case SET -> set.put(entry.name, entry.value);
        }
        return new WorldlineExecutionPlan.Environment(inherit, remove, set);
    }

    void apply(ProcessBuilder builder) {
        Map<String, String> environment = builder.environment(); environment.clear();
        for (Entry entry : entries) switch (entry.mode) {
            case INHERIT -> { String value = System.getenv(entry.name);
                require(value != null, "required inherited environment is absent: " + entry.name);
                environment.put(entry.name, value); }
            case REMOVE -> environment.remove(entry.name); case SET -> environment.put(entry.name, entry.value);
        }
    }

    byte[] descriptor() {
        StringBuilder json = new StringBuilder("{\"schema\":").append(ExecutionJson.quote(SCHEMA))
                .append(",\"profile\":").append(ExecutionJson.quote(profile)).append(",\"entries\":[");
        for (int index = 0; index < entries.size(); index++) {
            Entry entry = entries.get(index); if (index > 0) json.append(',');
            json.append("{\"name\":").append(ExecutionJson.quote(entry.name))
                    .append(",\"mode\":").append(ExecutionJson.quote(entry.mode.text))
                    .append(",\"effect\":").append(ExecutionJson.quote(entry.effect.text)).append('}');
        }
        return (json.append("]}\n").toString()).getBytes(StandardCharsets.UTF_8);
    }

    static void selfTest() {
        Path temporary = Path.of("C:/worldline/private-fixture").toAbsolutePath().normalize();
        ExecutionEnvironmentManifest manifest = tooling(temporary);
        String descriptor = new String(manifest.descriptor(), StandardCharsets.UTF_8);
        require(descriptor.startsWith("{\"schema\":\"" + SCHEMA + "\",\"profile\":\"tooling-cycle\""),
                "environment descriptor encoding drift");
        require(!descriptor.contains(JAVA_OPTIONS) && !descriptor.contains(temporary.toString()),
                "environment descriptor disclosed a value");
        require(manifest.environment().set().size() == 3, "tooling environment drift");
        require(hostile(temporary).environment().set().size() == 3, "hostile environment drift");
        require(candidate(temporary, temporary.resolve("control")).environment().set().size() == 6,
                "Candidate environment drift");
        require(campaign(temporary, temporary.resolve("plan.json")).environment().set()
                .containsKey("WORLDLINE_CAMPAIGN_EXECUTOR_PLAN"), "campaign environment drift");
        require(guiNative(temporary, temporary.resolve("control"), temporary.resolve("lease"), "7", "11")
                .environment().set().size() == 9, "GUI environment drift");
        require(guiAero(temporary, temporary.resolve("gradle"), temporary.resolve("control"),
                temporary.resolve("lease"), "7", "11").environment().set().size() == 10,
                "Aero environment drift");
        expectFailure(() -> new ExecutionEnvironmentManifest("secret", List.of(
                Entry.set("API_TOKEN", Effect.TOOL, "forbidden"))));
        expectFailure(() -> new ExecutionEnvironmentManifest("duplicate", List.of(
                Entry.set("TEMP", Effect.TOOL, "one"), Entry.set("temp", Effect.TOOL, "two"))));
    }

    private void validate() {
        require(!entries.isEmpty(), "empty environment manifest"); HashSet<String> names = new HashSet<>();
        for (Entry entry : entries) {
            require(entry.name.matches("[A-Za-z_][A-Za-z0-9_]*"), "invalid environment name");
            String canonical = entry.name.toUpperCase(Locale.ROOT);
            require(names.add(canonical), "duplicate environment name");
            require(!secret(canonical), "secret-bearing environment name is forbidden");
            require((entry.mode == Mode.SET) == (entry.value != null), "environment mode/value drift");
        }
    }

    private static boolean secret(String name) { return name.contains("PASSWORD") || name.contains("TOKEN")
            || name.contains("SECRET") || name.contains("CREDENTIAL") || name.endsWith("_KEY"); }
    private static void copyNumeric(List<Entry> entries, String name) {
        String value = System.getenv(name);
        if (value != null && !value.isBlank()) {
            require(value.matches("[0-9]+"), "campaign environment is not numeric: " + name);
            entries.add(Entry.set(name, Effect.BEHAVIOR, value));
        }
    }
    private static void expectFailure(Runnable action) { try { action.run();
        throw new IllegalStateException("invalid environment manifest was accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }

    private enum Mode { INHERIT("inherit"), SET("set"), REMOVE("remove");
        private final String text; Mode(String text) { this.text = text; } }
    private enum Effect { BEHAVIOR("behavior-affecting"), TOOL("tool-affecting"), OBSERVATIONAL("observational");
        private final String text; Effect(String text) { this.text = text; } }
    private record Entry(String name, Mode mode, Effect effect, String value) {
        private static Entry inherit(String name, Effect effect) {
            return new Entry(name, Mode.INHERIT, effect, null);
        }
        private static Entry set(String name, Effect effect, String value) {
            require(value != null && value.indexOf(0) < 0, "invalid environment value");
            return new Entry(name, Mode.SET, effect, value);
        }
    }
}
