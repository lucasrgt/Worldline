import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Reads exact reviewed non-ancestral heads that may be bundled as integrated content. */
final class WorktreeArchiveDisposition {
    private WorktreeArchiveDisposition() { }
    static Set<String> heads(Path root, String base) throws Exception {
        String object = base + ":coordination/archive-dispositions.properties";
        String text = git(root, "show", object); if (text == null) return Set.of();
        Properties values = new Properties(); values.load(new StringReader(text));
        require("1".equals(values.getProperty("schema")), "invalid archive disposition schema");
        int count = Integer.parseInt(values.getProperty("entry.count", "-1"));
        Set<String> heads = new HashSet<>();
        for (int index = 0; index < count; index++) {
            String prefix = "entry." + index + ".";
            String head = required(values, prefix + "head");
            String evidence = required(values, prefix + "evidence");
            require(head.matches("[0-9a-f]{40,64}") && heads.add(head)
                            && "content-integrated".equals(values.getProperty(prefix + "disposition"))
                            && git(root, "cat-file", "-e", base + ":" + evidence) != null,
                    "invalid archive disposition entry " + index);
        }
        return Set.copyOf(heads);
    }
    private static String git(Path root, String... arguments) throws Exception {
        java.util.List<String> command = new java.util.ArrayList<>(java.util.List.of("git"));
        command.addAll(java.util.List.of(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(60, TimeUnit.SECONDS)) { process.destroyForcibly();
            throw new IllegalStateException("archive disposition git probe timed out"); }
        return process.exitValue() == 0 ? output : null;
    }
    private static String required(Properties values, String key) {
        String value = values.getProperty(key); require(value != null && !value.isBlank(), "missing " + key);
        return value.trim();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
