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

/** Qualifies one-chunk movement, Packet50 cache turnover, and native topology frames. */
public final class ChunkTraversalCycle {
    private static final String ID = "m33-chunk-traversal";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties(), serverArtifact = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/ChunkTraversalCycle.java " + ID); System.exit(2);
        }
        try { new ChunkTraversalCycle().execute(); }
        catch (Exception error) { System.err.println("chunk traversal cycle failed: "
                + error.getMessage()); System.exit(1); }
    }

    private void execute() throws Exception {
        load(smoke.resolve("smoke.properties"), config);
        load(root.resolve("artifacts/minecraft-b1.7.3-server.properties"), serverArtifact);
        require(ID.equals(value(config, "id")), "smoke descriptor id mismatch");
        require(System.getProperty("os.name").startsWith("Windows")
                && System.getProperty("os.arch").contains("64"), "64-bit Windows is required");
        Path workspace = local(value(config, "workspace")), mapped = workspace.resolve("minecraft/bin");
        Path lwjgl = workspace.resolve("libraries/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209"
                + "/lwjgl-2.9.4-nightly-20150209.jar"), natives = workspace.resolve("libraries/natives");
        Path client = workspace.resolve("jars/minecraft.jar");
        Path server = root.resolve(value(serverArtifact, "local.path")).normalize();
        require(value(config, "server.jar.sha256").equals(value(serverArtifact, "expected.sha256")),
                "server descriptor drift");
        verify(client, value(config, "client.jar.sha256"), -1L);
        verify(server, value(config, "server.jar.sha256"),
                Long.parseLong(value(serverArtifact, "expected.bytes")));
        verify(lwjgl, value(config, "lwjgl.jar.sha256"), -1L);
        verify(natives.resolve("lwjgl64.dll"), value(config, "lwjgl64.dll.sha256"), -1L);
        require(Files.isRegularFile(mapped.resolve("net/minecraft/src/Tessellator.class")),
                "mapped Tessellator.class is absent; run the client smoke first");
        recreate(build); Path classes = compile(mapped, lwjgl);
        String oracle = capture(Arrays.asList("java", "-classpath", join(classes, product("api")),
                "worldline.b173server.B173ImplicitChunkFixture"));
        require(oracle.contains("WORLDLINE_M33_IMPLICIT_CHUNK_ORACLE=PASS"),
                "implicit chunk oracle absent");
        Outcome first = run(classes, mapped, lwjgl, natives, server, build.resolve("first"));
        Outcome second = run(classes, mapped, lwjgl, natives, server, build.resolve("second"));
        require(first.trace.equals(second.trace) && first.signature.equals(second.signature),
                "fresh chunk traversal contracts diverged");
        require(first.signature.equals(value(config, "expected.signature")), "M33 signature drift");
        String evidence = "id=" + ID + "\nprocesses=5\nservers=2\nimplicit.oracle=PASS\ncontext=Pbuffer"
                + "\ndisplay.created=false\nclient.sha256=" + value(config, "client.jar.sha256")
                + "\nserver.sha256=" + value(config, "server.jar.sha256") + "\ntrace=" + first.trace
                + "\nfirst=" + first.observation + "\nsecond=" + second.observation
                + "\nsignature=" + first.signature + "\n";
        Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
        System.out.println("M33 chunk traversal lifecycle passed");
        System.out.println("  processes: 1 edge-load oracle + 2 clients + 2 official servers");
        System.out.println("  path: quarter-block movement -> Packet50 turnover -> native topology frames");
        System.out.println("  observations: " + first.observation + " | " + second.observation);
        System.out.println("  signature: " + first.signature);
        System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
    }

    private Path compile(Path mapped, Path lwjgl) throws Exception {
        Path output = build.resolve("classes"); Files.createDirectories(output);
        List<String> command = new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8",
                "--release", "8", "-Xlint:all,-options", "-Werror", "-classpath",
                join(product("api"), mapped, lwjgl), "-d", output.toString()));
        command.addAll(javaFiles(root.resolve("modules/smoketest/src/main/java")));command.addAll(javaFiles(root.resolve("adapters/b173-server/src/main/java")));
        command.addAll(javaFiles(smoke.resolve("src"))); capture(command); return output;
    }

    private Outcome run(Path classes, Path mapped, Path lwjgl, Path natives,
            Path server, Path workspace) throws Exception {
        String output = capture(Arrays.asList("java", "-Djava.awt.headless=true",
                "-Dorg.lwjgl.librarypath=" + natives.toAbsolutePath(), "-classpath",
                join(classes, product("api"), mapped, lwjgl),
                "worldline.smoke.chunktraversal.ChunkTraversalSmoke", server.toString(),
                workspace.toString(), Integer.toString(freePort()), value(config, "seed"),
                value(config, "username"), value(config, "ticks")));
        require(output.contains("WORLDLINE_M33_API=server,session,movement,heartbeat,cache-lifecycle,native-render"),
                "M33 API marker absent");
        require(output.contains("WORLDLINE_M33_DISPLAY_CREATED=false"), "offscreen display proof absent");
        require(line(output, "WORLDLINE_M33_RENDERER=").replace('\\', '/').contains("minecraft/bin/"),
                "wrong renderer provenance");
        String observation = line(output, "WORLDLINE_M33_TRAVERSAL=") + ";"
                + line(output, "WORLDLINE_M33_LIFECYCLE=") + ";" + line(output, "WORLDLINE_M33_FRAMES=");
        return new Outcome(line(output, "WORLDLINE_M33_TRACE="),
                line(output, "WORLDLINE_M33_SIGNATURE="), observation);
    }

    private int freePort() throws IOException { try (ServerSocket socket = new ServerSocket(0)) {
        return socket.getLocalPort(); } }
    private void verify(Path path, String expected, long bytes) throws Exception {
        require(Files.isRegularFile(path), "native input is absent: " + path);
        if (bytes >= 0L) require(Files.size(path) == bytes, "native input size drift: " + path);
        require(digest(path).equals(expected), "native input hash drift: " + path);
    }
    private String digest(Path path) throws Exception { MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) { byte[] buffer = new byte[8192]; int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count); }
        return HexFormat.of().formatHex(digest.digest()); }
    private List<String> javaFiles(Path source) throws IOException { try (Stream<Path> paths = Files.walk(source)) {
        return paths.filter(path -> path.toString().endsWith(".java")).sorted().map(Path::toString)
                .collect(Collectors.toList()); } }
    private String capture(List<String> command) throws Exception { Process process = new ProcessBuilder(command)
            .directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(0) + " failed\n" + output);
        return output; }
    private void recreate(Path target) throws IOException { if (Files.exists(target)) {
        require(target.startsWith(root.resolve(".worldline")) && !target.equals(root), "unsafe build path");
        try (Stream<Path> paths = Files.walk(target)) { for (Path path : paths.sorted(Comparator.reverseOrder())
                .collect(Collectors.toList())) Files.delete(path); } } Files.createDirectories(target); }
    private void load(Path path, Properties target) throws IOException { try (java.io.Reader reader = Files
            .newBufferedReader(path, StandardCharsets.UTF_8)) { target.load(reader); } }
    private String value(Properties source, String key) { String result = source.getProperty(key);
        require(result != null && !result.trim().isEmpty(), "missing property " + key); return result.trim(); }
    private Path local(String value) { Path base = root.resolve("local").normalize(), result = root.resolve(value).normalize();
        require(result.startsWith(base), "workspace is not local"); return result; }
    private Path product(String name) { return root.resolve(".worldline/build/classes").resolve(name); }
    private String join(Path... paths) { return Arrays.stream(paths).map(Path::toString)
            .collect(Collectors.joining(System.getProperty("path.separator"))); }
    private String line(String output, String prefix) { return output.lines().filter(row -> row.startsWith(prefix))
            .findFirst().orElseThrow(() -> new IllegalStateException("missing " + prefix)).substring(prefix.length()); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static final class Outcome { final String trace, signature, observation;
        Outcome(String trace, String signature, String observation) {
            this.trace = trace; this.signature = signature; this.observation = observation; } }
}
