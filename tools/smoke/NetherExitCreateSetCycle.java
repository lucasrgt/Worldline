import java.io.EOFException;
import java.io.InputStream;
import java.io.Reader;
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
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Repeats official Nether-exit create of a new overworld obsidian 49 + portal 90 frame. */
public final class NetherExitCreateSetCycle {
    private static final String ID = "m563-nether-exit-create-set";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties(), artifact = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/NetherExitCreateSetCycle.java " + ID);
            System.exit(2);
        }
        try { new NetherExitCreateSetCycle().execute(); }
        catch (Exception error) {
            System.err.println("nether exit create set failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        load(smoke.resolve("smoke.properties"), config);
        load(root.resolve("artifacts/minecraft-b1.7.3-server.properties"), artifact);
        require(ID.equals(value(config, "id"))
                && value(config, "server.jar.sha256").equals(value(artifact, "expected.sha256"))
                && value(config, "username").equals("NetherExit563")
                && value(config, "username").length() <= 16, "descriptor drift");
        Path official = root.resolve(value(artifact, "local.path")).normalize();
        verifyArtifact(official);
        recreate(build);
        Path classes = compile();
        Outcome first = run(classes, official, build.resolve("first"));
        Outcome second = run(classes, official, build.resolve("second"));
        require(first.signal.equals(second.signal) && first.trace.equals(second.trace)
                && first.signature.equals(second.signature), "fresh nether-exit-create-set results diverged");
        require(first.signal.contains("0->-1->0") && first.signal.contains("6x90+14x49")
                && first.signal.contains("obsidian=49") && first.signal.contains("portal=90")
                && first.signal.contains("not-source") && first.signal.contains("not-m134-reuse")
                && first.signal.contains("not-m382-activation-only")
                && first.trace.contains("allow-nether-true")
                && first.trace.contains("official-create-overworld-portal-obsidian49-plus-portal90")
                && first.trace.contains("not-m134-reuse-of-source-frame")
                && !first.signal.contains("reused"),
                "set signal must include created obsidian 49 + portal 90 and refuse M134 reuse");
        String expected = value(config, "expected.signature");
        if (expected.equals("pending") || Boolean.getBoolean("worldline.m563.diagnostic")) {
            System.out.println("FROZEN");
            System.out.println("  " + first.signal);
            System.out.println("  trace: " + first.trace);
            System.out.println("  signature: " + first.signature);
            return;
        }
        require(first.signal.equals(value(config, "expected.signal")) && first.signature.equals(expected),
                "M563 frozen evidence drift");
        Files.writeString(build.resolve("evidence.txt"), "id=" + ID + "\nservers=2\nclients=6\nfirst="
                + first.signal + "\nsecond=" + second.signal + "\ntrace=" + first.trace
                + "\nsignature=" + first.signature + "\n", StandardCharsets.UTF_8);
        System.out.println("FROZEN " + first.signature);
        System.out.println("  " + first.signal);
        System.out.println("  signature: " + first.signature);
    }

    private Outcome run(Path classes, Path official, Path workspace) throws Exception {
        Exception last = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                if (Files.exists(workspace)) recreate(workspace);
                String output = capture(Arrays.asList("java", "-classpath",
                        classes + System.getProperty("path.separator") + product("api"),
                        "worldline.smoke.netherexitcreatesetb173.NetherExitCreateSetSmoke", official.toString(),
                        workspace.toString(), Integer.toString(freePort()), value(config, "seed"),
                        value(config, "username"), value(config, "chunk.x"), value(config, "chunk.z"),
                        value(config, "portal.settle.ticks"), value(config, "travel.ticks"),
                        value(config, "cooldown.ticks"), value(config, "nether.shift")));
                return new Outcome(line(output, "WORLDLINE_M563_TRACE="),
                        line(output, "WORLDLINE_M563_SIGNATURE="),
                        line(output, "WORLDLINE_M563_SET="));
            } catch (Exception error) {
                last = error;
                if (attempt == 0 && eof(error)) {
                    Thread.sleep(5000L);
                    continue;
                }
                throw error;
            }
        }
        throw last;
    }

    private Path compile() throws Exception {
        Path output = build.resolve("adapter-classes");
        Files.createDirectories(output);
        List<String> command = new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8", "--release", "8",
                "-Xlint:all,-options", "-Werror", "-classpath", product("api").toString(), "-d", output.toString()));
        command.addAll(javaFiles(root.resolve("adapters/b173-server/src/main/java")));
        command.addAll(javaFiles(smoke.resolve("src")));
        capture(command);
        return output;
    }

    private int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); }
    }

    private void verifyArtifact(Path path) throws Exception {
        require(Files.isRegularFile(path), "server artifact absent; run Acquire.java server");
        require(Files.size(path) == Long.parseLong(value(artifact, "expected.bytes")), "server size mismatch");
        require(digest(path, "SHA-1").equals(value(artifact, "expected.sha1")), "server SHA-1 mismatch");
        require(digest(path, "SHA-256").equals(value(artifact, "expected.sha256")), "server SHA-256 mismatch");
    }

    private List<String> javaFiles(Path source) throws Exception {
        try (Stream<Path> paths = Files.walk(source)) {
            return paths.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.getFileName().toString().equals("B173AtlasRunner.java"))
                    .sorted().map(Path::toString).collect(Collectors.toList());
        }
    }

    private String capture(List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(0) + " failed\n" + output);
        return output;
    }

    private void recreate(Path target) throws Exception {
        if (Files.exists(target)) {
            require(target.startsWith(root.resolve(".worldline")) && !target.equals(root), "unsafe build path");
            try (Stream<Path> paths = Files.walk(target)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))
                    Files.delete(path);
            }
        }
        Files.createDirectories(target);
    }

    private void load(Path path, Properties target) throws Exception {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { target.load(reader); }
    }

    private String value(Properties source, String key) {
        String result = source.getProperty(key);
        require(result != null && !result.trim().isEmpty(), "missing property " + key);
        return result.trim();
    }

    private Path product(String name) { return root.resolve(".worldline/build/classes").resolve(name); }

    private String digest(Path path, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private String line(String output, String prefix) {
        return output.lines().filter(value -> value.startsWith(prefix)).findFirst()
                .orElseThrow(() -> new IllegalStateException("missing " + prefix + "\n" + output))
                .substring(prefix.length());
    }

    private static boolean eof(Exception error) {
        for (Throwable throwable = error; throwable != null; throwable = throwable.getCause()) {
            if (throwable instanceof EOFException) return true;
            String message = throwable.getMessage();
            if (message != null && message.toUpperCase(Locale.ROOT).contains("EOF")) return true;
        }
        return false;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static final class Outcome {
        final String trace, signature, signal;
        Outcome(String trace, String signature, String signal) {
            this.trace = trace; this.signature = signature; this.signal = signal;
        }
    }
}
