import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;

/** Recognizes token-equivalent smoke edits chained after canonical fixture rewrites. */
final class SmokeSourceRefactor {
    private SmokeSourceRefactor() { }

    static List<Row> formatting(Path root, String id, Properties attestations) throws Exception {
        List<Row> rows = new ArrayList<>();
        for (String relative : capture(root, "diff", "--name-only", "HEAD", "--", "smokes/" + id)
                .lines().filter(value -> !value.isBlank()).toList()) {
            Path currentPath = root.resolve(relative); String current = Files.readString(currentPath);
            if (fixture(attestations, relative, digest(current))) continue;
            if (!relative.endsWith(".java")) return List.of();
            String prior = capture(root, "show", "HEAD:" + relative);
            if (!FormattingPinMigration.tokens(prior).equals(FormattingPinMigration.tokens(current)))
                return List.of();
            rows.add(new Row(relative, digest(prior), digest(current)));
        }
        return List.copyOf(rows);
    }

    private static boolean fixture(Properties values, String relative, String current) {
        int count = Integer.parseInt(values.getProperty("refresh.fixture.count", "0"));
        for (int index = 1; index <= count; index++) {
            String stem = "refresh.fixture." + index + ".";
            if (relative.equals(values.getProperty(stem + "path")))
                return current.equals(values.getProperty(stem + "current_sha256"));
        }
        return false;
    }

    private static String capture(Path root, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git")); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException("git command failed: " + command);
        return output;
    }

    private static String digest(String value) throws Exception { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(value.replace("\r\n", "\n")
                    .getBytes(StandardCharsets.UTF_8))); }

    record Row(String path, String prior, String current) { }
}
