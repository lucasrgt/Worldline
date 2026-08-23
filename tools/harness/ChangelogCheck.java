import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Validates the partitioned release history and resolves a version to its series. */
final class ChangelogCheck {
    private static final Pattern HEADING = Pattern.compile("## ([0-9]+)[.]([0-9]+)[.]([0-9]+).*?");
    private final Path root;

    ChangelogCheck(Path root) { this.root = root; }

    void execute(String currentVersion) throws Exception {
        Path index = root.resolve("CHANGELOG.md"), directory = root.resolve("changelog");
        require(Files.isRegularFile(index) && Files.size(index) <= 4_096L,
                "CHANGELOG.md must remain a small series index");
        require(Files.isDirectory(directory), "missing changelog series directory");
        Set<String> versions = new HashSet<>(); int sections = 0;
        try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(item -> item.toString().endsWith(".md")).sorted().toList()) {
                String name = path.getFileName().toString();
                String text = Files.readString(path, StandardCharsets.UTF_8);
                require(Files.readString(index, StandardCharsets.UTF_8)
                        .contains("changelog/" + name), "unindexed changelog partition: " + name);
                for (String line : text.lines().filter(row -> row.startsWith("## ")).toList()) {
                    if (line.startsWith("## Unreleased")) {
                        require(name.equals("unreleased.md"), "Unreleased section outside unreleased.md");
                    } else {
                        Matcher match = HEADING.matcher(line); require(match.matches(),
                                "invalid release heading: " + line);
                        String version = match.group(1) + "." + match.group(2) + "." + match.group(3);
                        require(name.equals(series(version).getFileName().toString()),
                                version + " is in the wrong changelog series");
                        require(versions.add(version), "duplicate changelog version " + version);
                    }
                    sections++;
                }
            }
        }
        require(sections > 0 && versions.contains(currentVersion),
                "current release is absent from partitioned changelog: " + currentVersion);
        System.out.println("  changelog: " + sections + " sections across partitioned series");
    }

    Path series(String version) {
        Matcher match = Pattern.compile("([0-9]+)[.]([0-9]+)[.]([0-9]+)").matcher(version);
        require(match.matches(), "invalid release version: " + version);
        int major = Integer.parseInt(match.group(1)), minor = Integer.parseInt(match.group(2));
        String group = major == 0 ? "0.x" : major + "." + (minor / 100) + "xx";
        return root.resolve("changelog/" + group + ".md");
    }

    static void selfTest() throws Exception {
        Path root = Files.createTempDirectory("worldline-changelog-check-");
        try {
            Files.createDirectories(root.resolve("changelog"));
            Files.writeString(root.resolve("CHANGELOG.md"), "# Changelog\n\n- [1.4xx](changelog/1.4xx.md)\n"
                    + "- [Unreleased](changelog/unreleased.md)\n");
            Files.writeString(root.resolve("changelog/1.4xx.md"),
                    "# Worldline changelog — 1.4xx\n\n## 1.462.0 - Current\n\nA.\n");
            Files.writeString(root.resolve("changelog/unreleased.md"),
                    "# Worldline changelog — Unreleased\n\n## Unreleased - Work\n\nB.\n");
            new ChangelogCheck(root).execute("1.462.0");
            System.out.println("changelog check self-test passed");
        } finally { SafeTreeDelete.delete(root); }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
