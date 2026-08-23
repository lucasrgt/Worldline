import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Classifies local branches by patch equivalence against an integration base. */
final class BranchTriage {
    private BranchTriage() { }

    public static void main(String[] arguments) {
        try {
            if (arguments.length != 2 || !"--base".equals(arguments[0]))
                throw new IllegalArgumentException("usage: java BranchTriage.java --base REF");
            write(Path.of("").toAbsolutePath().normalize(), arguments[1]);
        } catch (Exception error) {
            System.err.println("branch triage failed: " + error.getMessage()); System.exit(1);
        }
    }

    static void write(Path root, String reference) throws Exception {
        String base = git(root, "rev-parse", "--verify", reference + "^{commit}").trim();
        List<Branch> branches = new ArrayList<>();
        for (String name : git(root, "for-each-ref", "--format=%(refname:short)", "refs/heads")
                .lines().map(String::trim).filter(value -> !value.isEmpty()).toList()) {
            String head = git(root, "rev-parse", name + "^{commit}").trim();
            int ahead = count(root, base + ".." + head), behind = count(root, head + ".." + base);
            List<String> cherry = git(root, "cherry", base, head).lines().toList();
            int unique = (int) cherry.stream().filter(line -> line.startsWith("+")).count();
            int equivalent = (int) cherry.stream().filter(line -> line.startsWith("-")).count();
            boolean ancestor = status(root, "merge-base", "--is-ancestor", head, base) == 0;
            String classification = ancestor || unique == 0 ? "contained"
                    : unique == 1 ? "one-unique" : "divergent";
            branches.add(new Branch(name, head, classification, ahead, behind, unique, equivalent, ancestor));
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
                    .append(",\"ancestry_contained\":").append(value.ancestor).append('}')
                    .append(index + 1 == branches.size() ? "\n" : ",\n");
        }
        json.append("  ]\n}\n");
        Path report = root.resolve(".worldline/reports/branches.json");
        Files.createDirectories(report.getParent()); Files.writeString(report, json, StandardCharsets.UTF_8);
        System.out.println("branch triage: total=" + branches.size() + ", contained=" + contained
                + ", one-unique=" + one + ", divergent=" + divergent);
        System.out.println("  report: " + root.relativize(report));
    }

    private static int count(Path root, String range) throws Exception {
        return Integer.parseInt(git(root, "rev-list", "--count", range).trim());
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
    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    private record Branch(String name, String head, String classification, int ahead, int behind,
            int unique, int equivalent, boolean ancestor) { }
}
