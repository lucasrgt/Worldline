import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Qualifies neutral command/save/state control over the official b1.7.3 server. */
public final class ServerControlCycle {
    private static final String ID = "m21-server-control";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties();
    private final Properties artifact = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/ServerControlCycle.java " + ID);
            System.exit(2);
        }
        try { new ServerControlCycle().execute(); }
        catch (Exception error) {
            System.err.println("server control cycle failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        load(smoke.resolve("smoke.properties"), config);
        load(root.resolve("artifacts/minecraft-b1.7.3-server.properties"), artifact);
        require(ID.equals(value(config, "id")), "smoke descriptor id mismatch");
        require(value(config, "server.jar.sha256").equals(value(artifact, "expected.sha256")),
                "server descriptor drift");
        Path official = root.resolve(value(artifact, "local.path")).normalize();
        verifyArtifact(official);
        recreate(build);
        Path classes = compile();
        Outcome first = run(classes, official, build.resolve("first"));
        Outcome second = run(classes, official, build.resolve("second"));
        require(first.trace.equals(second.trace) && first.signature.equals(second.signature),
                "fresh server controls diverged");
        require(first.signature.equals(value(config, "expected.signature")),
                "M21 signature drift: " + first.signature);
        String evidence = "id=" + ID + "\nprocesses=2\nserver.sha256="
                + value(artifact, "expected.sha256") + "\ntrace=" + first.trace
                + "\nsignature=" + first.signature + "\n";
        Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
        System.out.println("M21 dedicated server control passed");
        System.out.println("  processes: 2 Worldline controllers + 2 official servers");
        System.out.println("  API: boot -> setTime -> save -> state -> close");
        System.out.println("  persisted times: " + first.worldTime + ", " + second.worldTime);
        System.out.println("  signature: " + first.signature);
        System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
    }

    private Path compile() throws Exception {
        Path output = build.resolve("adapter-classes");
        Files.createDirectories(output);
        List<String> command = new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8",
                "--release", "8", "-Xlint:all,-options", "-Werror", "-classpath",
                product("api").toString(), "-d", output.toString()));
        command.addAll(javaFiles(root.resolve("adapters/b173-server/src/main/java")));
        command.addAll(javaFiles(smoke.resolve("src")));
        capture(root, command);
        return output;
    }

    private Outcome run(Path classes, Path official, Path workspace) throws Exception {
        int port = freePort();
        String output = capture(root, Arrays.asList("java", "-classpath",
                classes + System.getProperty("path.separator") + product("api"),
                "worldline.smoke.serverb173.ServerControlSmoke", official.toString(), workspace.toString(),
                Integer.toString(port), value(config, "seed"), value(config, "target.time")));
        require(output.contains("WORLDLINE_M21_API=boot,set-time,save,state,close"), "M21 API marker absent");
        require(output.replace('\\', '/').contains("adapter-classes/"), "wrong server adapter source");
        return new Outcome(line(output, "WORLDLINE_M21_TRACE="),
                line(output, "WORLDLINE_M21_SIGNATURE="),
                Long.parseLong(line(output, "WORLDLINE_M21_TIME=")));
    }

    private int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); }
    }

    private void verifyArtifact(Path path) throws Exception {
        require(Files.isRegularFile(path), "server artifact absent; run Acquire.java server");
        require(Files.size(path) == Long.parseLong(value(artifact, "expected.bytes")), "server size mismatch");
        require(digest(path, "SHA-1").equals(value(artifact, "expected.sha1")), "server SHA-1 mismatch");
        require(digest(path, "SHA-256").equals(value(artifact, "expected.sha256")), "server SHA-256 mismatch");
    }

    private List<String> javaFiles(Path source) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            return paths.filter(path -> path.toString().endsWith(".java")).sorted()
                    .map(Path::toString).collect(Collectors.toList());
        }
    }

    private String capture(Path directory, List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(0) + " failed\n" + output);
        return output;
    }

    private void recreate(Path target) throws IOException {
        if (Files.exists(target)) {
            require(target.startsWith(root.resolve(".worldline")) && !target.equals(root), "unsafe build path");
            try (Stream<Path> paths = Files.walk(target)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
                    Files.delete(path);
            }
        }
        Files.createDirectories(target);
    }

    private void load(Path path, Properties target) throws IOException {
        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { target.load(reader); }
    }

    private String value(Properties source, String key) {
        String result = source.getProperty(key);
        require(result != null && !result.trim().isEmpty(), "missing property " + key);
        return result.trim();
    }

    private String digest(Path path, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192]; int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private Path product(String name) { return root.resolve(".worldline/build/classes").resolve(name); }

    private String line(String output, String prefix) {
        return output.lines().filter(value -> value.startsWith(prefix)).findFirst().orElseThrow(
                () -> new IllegalStateException("missing " + prefix)).substring(prefix.length());
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static final class Outcome {
        final String trace, signature;
        final long worldTime;
        Outcome(String trace, String signature, long worldTime) {
            this.trace = trace; this.signature = signature; this.worldTime = worldTime;
        }
    }
}
