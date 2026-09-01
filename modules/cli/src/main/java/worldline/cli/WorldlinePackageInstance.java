package worldline.cli;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Strict recognition boundary for the pre-WPM fixture instance. */
final class WorldlinePackageInstance {
    private static final String PACKAGE = "worldline-testkit-fixture";
    private static final String VERSION = "0.0.0-fixture";
    private static final List<String> KEYS = List.of("schema", "runtime", "mode",
            "official.artifacts", "package.count", "package.0.id", "package.0.version",
            "package.0.path", "package.0.sha256");

    private WorldlinePackageInstance() { }

    static boolean verify(Path project, PrintStream output) throws Exception {
        Path instance = project.getParent() == null ? null : project.getParent().getParent();
        if (instance == null || !project.equals(instance.resolve("tests/worldline"))) return false;
        Path lock = instance.resolve("wpm.lock");
        if (!Files.exists(lock, LinkOption.NOFOLLOW_LINKS)) return false;
        require(Files.isRegularFile(lock, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(lock) && Files.size(lock) <= 4_096L,
                "WPM fixture lock is not a bounded regular file");
        String text = Files.readString(lock, StandardCharsets.UTF_8);
        Map<String, String> values = parse(text);
        require(new ArrayList<>(values.keySet()).equals(KEYS), "WPM fixture lock fields drifted");
        require("worldline.wpm-fixture-lock.v1".equals(values.get("schema"))
                && "b1.7.3".equals(values.get("runtime"))
                && "host-only".equals(values.get("mode"))
                && "external-only".equals(values.get("official.artifacts"))
                && "1".equals(values.get("package.count"))
                && PACKAGE.equals(values.get("package.0.id"))
                && VERSION.equals(values.get("package.0.version")),
                "WPM fixture identity or official-artifact policy drifted");
        String declared = values.get("package.0.path"); Path relative = Path.of(declared).normalize();
        require(!relative.isAbsolute() && !relative.startsWith("..")
                        && declared.equals(relative.toString().replace('\\', '/'))
                        && relative.startsWith("packages"), "WPM fixture package path escaped");
        Path payload = instance.resolve(relative).normalize();
        require(payload.startsWith(instance.resolve("packages"))
                        && Files.isRegularFile(payload, LinkOption.NOFOLLOW_LINKS)
                        && !Files.isSymbolicLink(payload) && Files.size(payload) <= 1_048_576L,
                "WPM fixture package is not a bounded regular file");
        require(values.get("package.0.sha256").matches("[0-9a-f]{64}")
                        && values.get("package.0.sha256").equals(PinnedDownload.digest(payload)),
                "WPM fixture package digest mismatch");
        require(Files.readString(payload, StandardCharsets.UTF_8).equals(packageBytes()),
                "WPM fixture package content drifted");
        Path oracle = project.resolve(".local/oracles/b1.7.3");
        require(!Files.exists(oracle.resolve("minecraft.jar"), LinkOption.NOFOLLOW_LINKS)
                        && !Files.exists(oracle.resolve("minecraft_server.jar"), LinkOption.NOFOLLOW_LINKS),
                "WPM fixture embedded an official runtime");
        require(text.equals(canonical(values)), "WPM fixture lock is not canonical");
        output.println("+ WPM fixture instance verified: " + values.get("package.0.sha256"));
        return true;
    }

    static String packageBytes() {
        return "schema=worldline.wpm-fixture-package.v1\nid=" + PACKAGE
                + "\nversion=" + VERSION + "\n";
    }

    private static Map<String, String> parse(String text) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : text.split("\n", -1)) {
            if (line.isEmpty()) continue;
            int split = line.indexOf('=');
            require(split > 0 && line.indexOf('=', split + 1) < 0, "invalid WPM fixture lock line");
            String key = line.substring(0, split), value = line.substring(split + 1);
            require(key.matches("[a-z0-9.]+") && !value.isBlank()
                    && values.putIfAbsent(key, value) == null, "invalid or duplicate WPM fixture field");
        }
        return values;
    }

    private static String canonical(Map<String, String> values) {
        StringBuilder text = new StringBuilder();
        for (String key : KEYS) text.append(key).append('=').append(values.get(key)).append('\n');
        return text.toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
