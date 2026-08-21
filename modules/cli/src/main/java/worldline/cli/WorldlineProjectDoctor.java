package worldline.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** Standalone diagnosis used before Gradle can be configured successfully. */
final class WorldlineProjectDoctor {
    private static final String CLIENT = "af1fa04b8006d3ef78c7e24f8de4aa56f439a74d7f314827529062d5bab6db4c";
    private static final String SERVER = "033a127e4a25a60b038f15369c89305a3d53752242a1cff11ae964954e79ba4d";
    private WorldlineProjectDoctor() {}
    static int run(String[] arguments, PrintStream output) throws Exception {
        if (arguments.length > 1) throw new IllegalArgumentException("usage: worldline doctor [tests/worldline]");
        Path root = Paths.get(arguments.length == 0 ? "tests/worldline" : arguments[0])
                .toAbsolutePath().normalize();
        WorldlineOracleSettings settings = WorldlineOracleSettings.resolve(root);
        boolean hostOnly = settings.hostOnly; Path client = settings.client, server = settings.server;
        boolean failed = false; output.println("WORLDLINE DOCTOR  project=" + root);
        if (Runtime.version().feature() >= 21) pass(output, "JDK " + Runtime.version().feature() + " tooling");
        else { fail(output, "JDK 21 or newer is required"); failed = true; }
        if (Files.isDirectory(root.resolve("src/test/java"))) pass(output, "Java test source");
        else { fail(output, "missing src/test/java"); failed = true; }
        if (!hostOnly) failed |= !oracle(output, "client", client, 1_465_375L, CLIENT, true);
        else pass(output, "host-only mode");
        oracle(output, "server", server, 503_100L, SERVER, false);
        if (tracked(root, client) || tracked(root, server)) { fail(output, "official JAR is tracked by Git"); failed = true; }
        else pass(output, "official JARs are outside Git tracking");
        output.println("WORLDLINE_DOCTOR=" + (failed ? "FAIL" : "PASS")); return failed ? 1 : 0;
    }
    private static boolean oracle(PrintStream output, String name, Path path, long bytes,
            String hash, boolean required) throws Exception {
        if (!Files.isRegularFile(path)) {
            output.println((required ? "x " : "o ") + name + " oracle absent: " + path); return !required;
        }
        boolean valid = Files.size(path) == bytes && PinnedDownload.digest(path).equals(hash);
        if (valid) pass(output, name + " oracle verified: " + path); else fail(output, name + " oracle mismatch: " + path);
        return valid;
    }
    private static boolean tracked(Path project, Path path) throws Exception {
        if (!Files.exists(path)) return false;
        Path repo = project; while (repo != null && !Files.isDirectory(repo.resolve(".git"))) repo = repo.getParent();
        if (repo == null) return false;
        Path absolute = path.toAbsolutePath().normalize(); if (!absolute.startsWith(repo)) return false;
        Process process = new ProcessBuilder("git", "-C", repo.toString(), "ls-files", "--error-unmatch",
                repo.relativize(absolute).toString()).redirectErrorStream(true).start();
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            process.getInputStream().transferTo(bytes); return process.waitFor() == 0 && bytes.size() > 0;
        }
    }
    private static void pass(PrintStream output, String message) { output.println("+ " + message); }
    private static void fail(PrintStream output, String message) { output.println("x " + message); }
}
