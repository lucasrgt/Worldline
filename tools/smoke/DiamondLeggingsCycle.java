import java.io.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.*;

/** Repeats one official diamond-leggings equip, peer Packet5, and persistence. */
public final class DiamondLeggingsCycle {
    private static final String ID = "m272-diamond-leggings";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties(), artifact = new Properties();
    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/DiamondLeggingsCycle.java " + ID); System.exit(2); }
        try { new DiamondLeggingsCycle().execute(); }
        catch (Exception error) { System.err.println("diamond leggings failed: " + error.getMessage()); System.exit(1); }
    }
    private void execute() throws Exception {
        load(smoke.resolve("smoke.properties"), config);
        load(root.resolve("artifacts/minecraft-b1.7.3-server.properties"), artifact);
        require(ID.equals(value(config, "id")) && value(config, "server.jar.sha256").equals(value(artifact, "expected.sha256")),
                "descriptor drift");
        Path official = root.resolve(value(artifact, "local.path")).normalize(); verifyArtifact(official);
        recreate(build); Path classes = compile();
        Outcome first = run(classes, official, build.resolve("first"));
        Outcome second = run(classes, official, build.resolve("second"));
        require(first.signal.equals(second.signal) && first.trace.equals(second.trace)
                && first.signature.equals(second.signature), "fresh diamond-leggings results diverged");
        String expected = value(config, "expected.signature");
        if (expected.equals("pending") || Boolean.getBoolean("worldline.m272.diagnostic")) {
            System.out.println("FROZEN");
            System.out.println("  " + first.signal);
            System.out.println("  trace: " + first.trace);
            System.out.println("  signature: " + first.signature);
            return;
        }
        require(first.signal.equals(value(config, "expected.signal")) && first.signature.equals(expected),
                "M272 frozen evidence drift");
        Files.writeString(build.resolve("evidence.txt"), "id=" + ID + "\nservers=2\nclients=8\nfirst="
                + first.signal + "\nsecond=" + second.signal + "\ntrace=" + first.trace + "\nsignature="
                + first.signature + "\n", StandardCharsets.UTF_8);
        System.out.println("M272 diamond leggings passed");
        System.out.println("  " + first.signal);
        System.out.println("  signature: " + first.signature);
        System.out.println("FROZEN");
    }
    private Outcome run(Path classes, Path official, Path workspace) throws Exception {
        Exception last = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                if (Files.exists(workspace)) recreate(workspace);
                String output = capture(root, Arrays.asList("java", "-classpath",
                        classes + separator() + product("api"),
                        "worldline.smoke.diamondleggingsb173.DiamondLeggingsSmoke",
                        official.toString(), workspace.toString(), Integer.toString(freePort()),
                        value(config, "seed"), value(config, "username"), value(config, "observer")));
                return new Outcome(line(output, "WORLDLINE_M272_TRACE="),
                        line(output, "WORLDLINE_M272_SIGNATURE="),
                        line(output, "WORLDLINE_M272_LEGGINGS="));
            } catch (Exception error) {
                last = error;
                if (attempt == 0 && eof(error)) { Thread.sleep(5000L); continue; }
                throw error;
            }
        }
        throw last;
    }
    private Path compile() throws Exception {
        Path output = build.resolve("adapter-classes"); Files.createDirectories(output);
        List<String> command = new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8", "--release", "8",
                "-Xlint:all,-options", "-Werror", "-classpath", product("api").toString(), "-d", output.toString()));
        command.addAll(javaFiles(root.resolve("adapters/b173-server/src/main/java")));
        command.addAll(javaFiles(smoke.resolve("src"))); capture(root, command); return output;
    }
    private void verifyArtifact(Path path) throws Exception {
        require(Files.isRegularFile(path), "server artifact absent");
        require(Files.size(path) == Long.parseLong(value(artifact, "expected.bytes"))
                && digest(path, "SHA-1").equals(value(artifact, "expected.sha1"))
                && digest(path, "SHA-256").equals(value(artifact, "expected.sha256")), "server artifact drift");
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
                for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(path);
            }
        }
        Files.createDirectories(target);
    }
    private void load(Path path, Properties target) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { target.load(reader); }
    }
    private String value(Properties source, String key) {
        String result = source.getProperty(key);
        require(result != null && !result.trim().isEmpty(), "missing " + key); return result.trim();
    }
    private String digest(Path path, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192]; int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    private int freePort() throws IOException { try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); } }
    private Path product(String name) { return root.resolve(".worldline/build/classes").resolve(name); }
    private String separator() { return System.getProperty("path.separator"); }
    private String line(String output, String prefix) {
        return output.lines().filter(value -> value.startsWith(prefix)).findFirst()
                .orElseThrow(() -> new IllegalStateException("missing " + prefix + "\n" + output)).substring(prefix.length());
    }
    private static boolean eof(Exception error) {
        String message = String.valueOf(error.getMessage());
        return message.contains("EOFException") || message.contains("EOF");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message); }
    private static final class Outcome {
        final String trace, signature, signal;
        Outcome(String trace, String signature, String signal) {
            this.trace = trace; this.signature = signature; this.signal = signal; }
    }
}
