import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Supervises applicable-scar recall and the objective readiness interlock before Candidate Gate. */
final class SwarmPreCandidate {
    private static final String REQUIRED = "NYA-01M0VSCA8F3WSMVW32R9XME7DQ";
    private static final String SOURCE = "NYA-01M0X81N6TG6TQ4RM02X6PH7R7";
    private static final String LANES = "NYA-01M0XRE7GSKH7ARKM73DVCGQ7K";
    private static final String CLOSURE = "NYA-01M0XWB16KZB3JRYDGAAYF5SVB";
    private static final String TOKENS = "NYA-01M0XFV9TPVDKFE6RARDHC84T2";
    private static final String SYMBOLS = "NYA-01M0XM730NWRQDKFZ1VMP3732W";
    private static final String TRAVERSAL = "NYA-01M0XYP7T1RKYFD3SJHC4DMHZ3";
    private SwarmPreCandidate() { }

    static void run(String id, String base, String goal) throws Exception {
        require(id.matches("m[0-9]+-[a-z0-9-]+") && base.matches("[0-9a-f]{40}"),
                "invalid pre-Candidate identity");
        Path root = Path.of("").toAbsolutePath().normalize();
        Properties descriptor = new Properties();
        try (var reader = Files.newBufferedReader(root.resolve("smokes").resolve(id)
                .resolve("smoke.properties"), StandardCharsets.UTF_8)) { descriptor.load(reader); }
        List<String> scars = new ArrayList<>(List.of(REQUIRED, SOURCE));
        if (SwarmProcess.status(root, List.of("git", "cat-file", "-e",
                base + ":smokes/" + id + "/smoke.properties"), 60) != 0) scars.add(LANES);
        String runner = descriptor.getProperty("runner.source", "");
        if (runner.endsWith("DataDrivenCycle.java") || runner.endsWith("CompositeCycle.java"))
            scars.add(CLOSURE);
        if (descriptor.containsKey("testkit.fixture")) scars.add(TOKENS);
        if (Files.isRegularFile(root.resolve("smokes").resolve(id).resolve("symbols.map")))
            scars.add(SYMBOLS);
        if (SwarmProcess.output(root, List.of("git", "diff", "--name-only", base), 60).lines()
                .anyMatch(path -> path.startsWith("tools/harness/")
                        || path.startsWith("tools/integration/"))) scars.add(TRAVERSAL);
        StringBuilder recall = new StringBuilder();
        String recallLimit = Integer.toString(recallLimit(root));
        for (String scar : scars) {
            String output = SwarmProcess.output(root, List.of("csm", "nya", "recall", "--task",
                    goal + " Required applicable scar " + scar, "--path", "smokes/" + id,
                    "--path", "tools/smoke", "--path", "modules/testkit",
                    "--limit", recallLimit), 300);
            require(output.contains(scar), "applicable scar was absent from recall: " + scar);
            recall.append("# ").append(scar).append('\n').append(output).append('\n');
        }
        Path proof = root.resolve(".worldline/reports/swarm/readiness-recall-" + id + ".log");
        Files.createDirectories(proof.getParent());
        Files.writeString(proof, recall, StandardCharsets.UTF_8);
        String output = java(root, List.of("tools/harness/Gate.java", "--candidate-ready", id), 600);
        System.out.print(output);
        System.out.println("Ox Alpha pre-Candidate interlock PASS: " + id);
        System.out.println("  applicable scars: " + String.join(",", scars));
    }

    private static String java(Path root, List<String> arguments, int seconds) throws Exception {
        List<String> command = new ArrayList<>(List.of(javaTool())); command.addAll(arguments);
        Path log = Files.createTempFile("worldline-swarm-readiness-", ".log");
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
                process.destroyForcibly(); throw new IllegalStateException("candidate readiness timed out");
            }
            String output = Files.readString(log, StandardCharsets.UTF_8);
            require(process.exitValue() == 0, "objective readiness failed:\n" + output); return output;
        } finally { Files.deleteIfExists(log); }
    }
    private static String javaTool() {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", "java" + (windows ? ".exe" : ""))
                .toString();
    }

    private static int recallLimit(Path root) throws Exception {
        Path scars = root.resolve(".csm/nya/scars");
        require(Files.isDirectory(scars), "NYA scar store is missing");
        int count = 0;
        try (var entries = Files.newDirectoryStream(scars, "*.toml")) {
            for (Path ignored : entries) count++;
        }
        require(count > 0, "NYA scar store is empty");
        return count;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
