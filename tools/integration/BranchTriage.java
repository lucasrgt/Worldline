import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Classifies local branches by patch equivalence against an integration base. */
final class BranchTriage {
    private BranchTriage() { }

    public static void main(String[] arguments) {
        try {
            if (arguments.length == 1 && "--self-test".equals(arguments[0])) {
                selfTest();
                return;
            }
            if (arguments.length != 2 || !"--base".equals(arguments[0]))
                throw new IllegalArgumentException("usage: java BranchTriage.java --base REF");
            write(Path.of("").toAbsolutePath().normalize(), arguments[1]);
        } catch (Exception error) {
            System.err.println("branch triage failed: " + error.getMessage()); System.exit(1);
        }
    }

    static void write(Path root, String reference) throws Exception {
        String base = git(root, "rev-parse", "--verify", reference + "^{commit}").trim();
        Set<String> receipts = receiptHeads(root, base);
        List<Branch> branches = new ArrayList<>();
        String format = "%(refname:short)%00%(objectname)%00%(ahead-behind:" + base + ")";
        for (String line : git(root, "for-each-ref", "--format=" + format, "refs/heads")
                .lines().filter(value -> !value.isBlank()).toList()) {
            Ref ref = ref(line);
            String name = ref.name;
            String head = ref.head;
            int ahead = ref.ahead;
            int behind = ref.behind;
            boolean ancestor = ahead == 0;
            List<String> cherry = ancestor ? List.of()
                    : git(root, "cherry", base, head).lines().toList();
            int unique = (int) cherry.stream().filter(row -> row.startsWith("+")).count();
            int equivalent = (int) cherry.stream().filter(row -> row.startsWith("-")).count();
            boolean receipt = receipts.contains(head);
            String classification = ancestor || unique == 0 || receipt ? "contained"
                    : unique == 1 ? "one-unique" : "divergent";
            branches.add(new Branch(name, head, classification, ahead, behind,
                    unique, equivalent, ancestor, receipt));
        }
        branches.sort(Comparator.comparing(Branch::classification).thenComparing(Branch::name));
        writeReport(root, base, branches);
    }

    private static void writeReport(Path root, String base, List<Branch> branches) throws Exception {
        long contained = branches.stream().filter(value -> value.classification.equals("contained")).count();
        long one = branches.stream().filter(value -> value.classification.equals("one-unique")).count();
        long divergent = branches.size() - contained - one;
        StringBuilder json = new StringBuilder("{\n  \"schema\":1,\n  \"created\":\"")
                .append(Instant.now()).append("\",\n  \"base\":\"").append(base)
                .append("\",\n  \"summary\":{\"total\":").append(branches.size())
                .append(",\"contained\":").append(contained).append(",\"one_unique\":")
                .append(one).append(",\"divergent\":").append(divergent)
                .append("},\n  \"branches\":[\n");
        for (int index = 0; index < branches.size(); index++) {
            Branch value = branches.get(index);
            json.append("    {\"name\":\"").append(escape(value.name)).append("\",\"head\":\"")
                    .append(value.head).append("\",\"classification\":\"")
                    .append(value.classification).append("\",\"ahead\":").append(value.ahead)
                    .append(",\"behind\":").append(value.behind).append(",\"unique_patches\":")
                    .append(value.unique).append(",\"equivalent_patches\":").append(value.equivalent)
                    .append(",\"ancestry_contained\":").append(value.ancestor)
                    .append(",\"receipt_contained\":").append(value.receipt).append('}')
                    .append(index + 1 == branches.size() ? "\n" : ",\n");
        }
        json.append("  ]\n}\n");
        Path report = root.resolve(".worldline/reports/branches.json");
        Files.createDirectories(report.getParent()); Files.writeString(report, json, StandardCharsets.UTF_8);
        System.out.println("branch triage: total=" + branches.size() + ", contained=" + contained
                + ", one-unique=" + one + ", divergent=" + divergent);
        System.out.println("  report: " + root.relativize(report));
    }

    private static Ref ref(String line) {
        String[] fields = line.split(String.valueOf((char) 0), -1);
        require(fields.length == 3, "invalid branch inventory row");
        String[] counts = fields[2].trim().split(" +", -1);
        require(counts.length == 2, "invalid ahead/behind row: " + fields[0]);
        return new Ref(fields[0], fields[1], Integer.parseInt(counts[0]),
                Integer.parseInt(counts[1]));
    }

    private static void selfTest() {
        String separator = String.valueOf((char) 0);
        Ref valid = ref("codex/milestone-m1-valid" + separator + "abc123" + separator + "1 2");
        require(valid.name.equals("codex/milestone-m1-valid") && valid.head.equals("abc123")
                && valid.ahead == 1 && valid.behind == 2, "valid branch row was rejected");
        boolean rejected = false;
        try {
            ref("truncated-row");
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        require(rejected, "malformed branch row was accepted");
        System.out.println("branch triage self-test passed");
    }

    private static Set<String> receiptHeads(Path root, String base) throws Exception {
        String object = base + ":smokes/train-reconciliation.lock";
        if (status(root, "cat-file", "-e", object) != 0) return Set.of();
        Properties values = new Properties(); values.load(new StringReader(git(root, "show", object)));
        Set<String> result = new HashSet<>();
        for (String key : values.stringPropertyNames())
            if (key.startsWith("smoke.") && key.endsWith(".receipt.head")
                    && "milestone".equals(values.getProperty(key.replace(".receipt.head", ".kind"))))
                result.add(values.getProperty(key));
        return Set.copyOf(result);
    }

    private static String git(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Path log = Files.createTempFile("worldline-triage-", ".log");
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                destroy(process); throw new IllegalStateException("git timed out: " + String.join(" ", arguments));
            }
            String output = Files.readString(log, StandardCharsets.UTF_8);
            if (process.exitValue() != 0) throw new IllegalStateException("git failed: " + output);
            return output;
        } finally { Files.deleteIfExists(log); }
    }

    private static int status(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            destroy(process); throw new IllegalStateException("git timed out");
        }
        return process.exitValue();
    }

    private static void destroy(Process process) {
        process.descendants().sorted(Comparator.comparingLong(ProcessHandle::pid).reversed())
                .forEach(ProcessHandle::destroyForcibly); process.destroyForcibly();
    }
    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    private record Ref(String name, String head, int ahead, int behind) { }
    private record Branch(String name, String head, String classification, int ahead, int behind,
            int unique, int equivalent, boolean ancestor, boolean receipt) { }
}
