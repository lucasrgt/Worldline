import java.io.StringReader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Resolves formal milestone retractions from the versioned behavior identity lock. */
final class WorktreeRetraction {
    private WorktreeRetraction() { }

    static Set<String> ids(Path root, String base) throws Exception {
        String object = base + ":smokes/behavior-identity.lock";
        if (status(root, "cat-file", "-e", object) != 0) return Set.of();
        Process show = new ProcessBuilder("git", "show", object).directory(root.toFile())
                .redirectErrorStream(true).start();
        String text = new String(show.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        require(show.waitFor(60, TimeUnit.SECONDS) && show.exitValue() == 0,
                "cannot read behavior retraction lock");
        Properties values = new Properties(); values.load(new StringReader(text));
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)\\bM([0-9]+)\\b")
                .matcher(text);
        Set<String> result = new HashSet<>();
        while (matcher.find()) result.add("m" + matcher.group(1));
        require(result.size() >= Integer.parseInt(values.getProperty("retracted.count", "0")),
                "behavior retraction IDs are incomplete");
        return Set.copyOf(result);
    }

    static boolean matches(String branch, Path path, Set<String> ids) {
        String value = branch + "/" + path.getFileName();
        return ids.stream().anyMatch(id -> value.toLowerCase(Locale.ROOT)
                .matches(".*(?:^|[-_/])" + id + "(?:[-_/].*|$)"));
    }

    private static int status(Path root, String... arguments) throws Exception {
        java.util.List<String> command = new java.util.ArrayList<>(java.util.List.of("git"));
        command.addAll(java.util.List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        require(process.waitFor(60, TimeUnit.SECONDS), "git retraction probe timed out");
        return process.exitValue();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
