import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Runs bounded exploratory fuzz and mutation campaigns after the canonical Gate. */
public final class NightlyQualityCampaign {
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path reports = root.resolve(".worldline/reports/nightly-quality");

    public static void main(String[] arguments) {
        try {
            if (Arrays.equals(arguments, new String[] {"--self-test"})) {
                selfTest();
                return;
            }
            new NightlyQualityCampaign().execute(arguments);
        } catch (Exception error) {
            System.err.println("nightly quality campaign failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute(String[] arguments) throws Exception {
        require(arguments.length == 4 && arguments[0].equals("--budget-seconds")
                && arguments[2].equals("--seed"), usage());
        int total = integer(arguments[1], 10, 7_200, "budget");
        long seed = Long.parseLong(arguments[3]);
        int fuzzBudget = Math.max(1, total / 2), mutationBudget = total - fuzzBudget;
        require(mutationBudget > 0, "budget must cover both campaigns");
        String classpath = classpath();
        Files.createDirectories(reports);
        int fuzzCases = environment("WORLDLINE_NIGHTLY_FUZZ_CASES", 4_096, 1, 4_096);
        int mutationCases = environment("WORLDLINE_NIGHTLY_MUTATION_CASES", 100_000, 1, 1_000_000);
        Run fuzz = run("fuzz", List.of(javaTool(), "-ea", "-cp", classpath,
                "worldline.fuzz.FuzzTest", "--explore", Long.toString(seed),
                Integer.toString(fuzzCases), "32"), fuzzBudget);
        Run mutation = run("mutation", List.of(javaTool(), "-ea", "-cp", classpath,
                "worldline.testapi.MutationCoverageTest", "--explore",
                Long.toString(seed ^ 0x574c4e494748544cL), Integer.toString(mutationCases)),
                mutationBudget);
        String head = gitIdentity("HEAD"), tree = gitIdentity("HEAD^{tree}");
        String summary = summary(head, tree, seed, total, fuzzCases, mutationCases, fuzz, mutation);
        Files.writeString(reports.resolve("summary.json"), summary, StandardCharsets.UTF_8);
        require(fuzz.exit == 0 && mutation.exit == 0,
                "campaign failure; inspect " + root.relativize(reports.resolve("summary.json")));
        System.out.println("nightly quality campaign passed: fuzz=" + fuzzCases
                + ", mutation=" + mutationCases + ", seed=" + seed);
    }

    private Run run(String name, List<String> command, int budget) throws Exception {
        long started = System.nanoTime();
        ProcessCapture.Result result = ProcessCapture.run(root, command, budget);
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        Files.writeString(reports.resolve(name + ".log"), result.output(), StandardCharsets.UTF_8);
        return new Run(result.exit(), result.timedOut(), budget, elapsed);
    }

    private String classpath() throws Exception {
        Path tests = root.resolve(".worldline/build/test-classes");
        require(Files.isDirectory(tests), "run the canonical Gate before the nightly campaign");
        Properties config = new Properties();
        try (var reader = Files.newBufferedReader(root.resolve("harness.properties"),
                StandardCharsets.UTF_8)) { config.load(reader); }
        List<String> paths = new ArrayList<>();
        paths.add(tests.toString());
        for (String module : config.getProperty("modules", "").split(",")) {
            Path output = root.resolve(".worldline/build/classes").resolve(module.trim());
            require(Files.isDirectory(output), "missing Gate module output: " + module);
            paths.add(output.toString());
        }
        return String.join(File.pathSeparator, paths);
    }

    private String gitIdentity(String reference) throws Exception {
        String value = ProcessCapture.require(root, List.of("git", "rev-parse", reference), 10).trim();
        require(value.matches("[0-9a-f]{40}"), "invalid Git identity for " + reference);
        return value;
    }

    private static String summary(String head, String tree, long seed, int budget,
            int fuzzCases, int mutationCases, Run fuzz, Run mutation) {
        return "{\n  \"schema\":1,\n  \"created\":\"" + Instant.now() + "\",\n"
                + "  \"head\":\"" + head + "\",\n  \"tree\":\"" + tree + "\",\n"
                + "  \"seed\":" + seed + ",\n  \"budget_seconds\":" + budget + ",\n"
                + "  \"fuzz\":" + result(fuzzCases, fuzz) + ",\n"
                + "  \"mutation\":" + result(mutationCases, mutation) + "\n}\n";
    }

    private static String result(int cases, Run run) {
        return "{\"cases\":" + cases + ",\"budget_seconds\":" + run.budget
                + ",\"elapsed_ms\":" + run.elapsed + ",\"exit\":" + run.exit
                + ",\"timed_out\":" + run.timedOut + "}";
    }

    static void selfTest() {
        require(integer("10", 10, 20, "value") == 10, "lower budget bound drifted");
        boolean rejected = false;
        try { integer("9", 10, 20, "value"); } catch (IllegalArgumentException expected) { rejected = true; }
        require(rejected, "invalid budget was accepted");
        String summary = summary("a".repeat(40), "b".repeat(40), 7L, 10, 20, 30,
                new Run(0, false, 5, 2),
                new Run(124, true, 5, 5));
        require(summary.contains("\"seed\":7") && summary.contains("\"timed_out\":true"),
                "nightly summary drifted");
        System.out.println("nightly quality campaign self-test passed");
    }

    private static int environment(String name, int fallback, int minimum, int maximum) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : integer(value, minimum, maximum, name);
    }
    private static int integer(String value, int minimum, int maximum, String name) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed >= minimum && parsed <= maximum) return parsed;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException(name + " must be between " + minimum + " and " + maximum);
    }
    private static String javaTool() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", "java" + (windows ? ".exe" : "")).toString();
    }
    private static String usage() {
        return "usage: NightlyQualityCampaign --budget-seconds N --seed N";
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private record Run(int exit, boolean timedOut, int budget, long elapsed) { }
}
