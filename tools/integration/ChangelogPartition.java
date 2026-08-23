import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Mechanically partitions the legacy changelog into release-series files. */
public final class ChangelogPartition {
    private static final Pattern VERSION = Pattern.compile("## ([0-9]+)[.]([0-9]+)[.]([0-9]+).*?");

    public static void main(String[] arguments) {
        try {
            if (List.of(arguments).equals(List.of("--self-test"))) { selfTest(); return; }
            if (arguments.length != 0) throw new IllegalArgumentException("usage: ChangelogPartition.java");
            partition(Path.of("").toAbsolutePath().normalize());
        } catch (Exception error) {
            System.err.println("changelog partition failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static void partition(Path root) throws Exception {
        Path source = root.resolve("CHANGELOG.md"), directory = root.resolve("changelog");
        require(Files.isRegularFile(source), "missing CHANGELOG.md");
        require(!Files.exists(directory), "refusing to overwrite changelog/");
        String original = normalize(Files.readString(source, StandardCharsets.UTF_8));
        int first = original.indexOf("\n## ");
        require(first >= 0, "CHANGELOG.md has no release sections");
        String introduction = original.substring(0, first).stripTrailing();
        List<String> sections = sections(original.substring(first + 1));
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (String section : sections) groups.computeIfAbsent(group(section), ignored -> new ArrayList<>())
                .add(section);
        Files.createDirectories(directory);
        for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
            String title = entry.getKey().equals("unreleased") ? "Unreleased" : entry.getKey();
            Files.writeString(directory.resolve(entry.getKey() + ".md"),
                    "# Worldline changelog — " + title + "\n\n" + String.join("\n", entry.getValue()),
                    StandardCharsets.UTF_8);
        }
        Files.writeString(source, index(introduction, groups.keySet()), StandardCharsets.UTF_8);
        verifyContent(sections, directory);
        System.out.println("changelog partitioned: sections=" + sections.size() + ", files=" + groups.size());
    }

    private static List<String> sections(String body) {
        List<String> result = new ArrayList<>();
        int start = 0;
        while (start < body.length()) {
            int next = body.indexOf("\n## ", start + 1);
            if (next < 0) { result.add(body.substring(start).stripTrailing() + "\n"); break; }
            result.add(body.substring(start, next).stripTrailing() + "\n");
            start = next + 1;
        }
        return result;
    }

    private static String group(String section) {
        String heading = section.lines().findFirst().orElse("");
        if (heading.startsWith("## Unreleased")) return "unreleased";
        Matcher match = VERSION.matcher(heading);
        require(match.matches(), "unsupported changelog heading: " + heading);
        int major = Integer.parseInt(match.group(1)), minor = Integer.parseInt(match.group(2));
        return major == 0 ? "0.x" : major + "." + (minor / 100) + "xx";
    }

    private static String index(String introduction, java.util.Set<String> groups) {
        StringBuilder text = new StringBuilder(introduction).append("\n\nRelease entries are partitioned "
                + "by series so integration trains never edit one monolithic history file.\n\n");
        for (String group : groups) {
            String label = group.equals("unreleased") ? "Unreleased" : group;
            text.append("- [").append(label).append("](changelog/").append(group).append(".md)\n");
        }
        return text.toString();
    }

    private static void verifyContent(List<String> expected, Path directory) throws Exception {
        List<String> actual = new ArrayList<>();
        try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(item -> item.toString().endsWith(".md")).sorted().toList()) {
                String text = normalize(Files.readString(path, StandardCharsets.UTF_8));
                int first = text.indexOf("\n## ");
                require(first >= 0, "partition has no sections: " + path.getFileName());
                actual.addAll(sections(text.substring(first + 1)));
            }
        }
        require(multiset(expected).equals(multiset(actual)), "partition changed changelog section content");
    }

    private static Map<String, Integer> multiset(List<String> values) {
        Map<String, Integer> result = new java.util.HashMap<>();
        for (String value : values) result.merge(value, 1, Integer::sum);
        return result;
    }

    private static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-changelog-");
        try {
            Files.writeString(root.resolve("CHANGELOG.md"), "# Changelog\n\nIntro.\n\n"
                    + "## 1.462.0 - Current\n\nA.\n\n## Unreleased - Work\n\nB.\n\n"
                    + "## 1.399.0 - Old\n\nC.\n\n## 0.1.0 - First\n\nD.\n");
            partition(root);
            require(Files.isRegularFile(root.resolve("changelog/1.4xx.md"))
                    && Files.isRegularFile(root.resolve("changelog/1.3xx.md"))
                    && Files.isRegularFile(root.resolve("changelog/unreleased.md"))
                    && Files.readString(root.resolve("CHANGELOG.md")).contains("changelog/0.x.md"),
                    "changelog partition topology drifted");
            System.out.println("changelog partition self-test passed");
        } finally { delete(root); }
    }

    private static void delete(Path root) throws Exception {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }
    private static String normalize(String value) { return value.replace("\r\n", "\n"); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
