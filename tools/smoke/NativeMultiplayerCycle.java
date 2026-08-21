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

/** Qualifies protocol-14 state feeding a real native offscreen Minecraft render. */
public final class NativeMultiplayerCycle {
    private static final String ID = "m26-native-multiplayer";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties(), serverArtifact = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/NativeMultiplayerCycle.java " + ID);
            System.exit(2);
        }
        try { new NativeMultiplayerCycle().execute(); }
        catch (Exception error) {
            System.err.println("native multiplayer cycle failed: " + error.getMessage()); System.exit(1);
        }
    }

    private void execute() throws Exception {
        load(smoke.resolve("smoke.properties"), config);
        load(root.resolve("artifacts/minecraft-b1.7.3-server.properties"), serverArtifact);
        require(ID.equals(value(config, "id")), "smoke descriptor id mismatch");
        require(System.getProperty("os.name").startsWith("Windows")
                && System.getProperty("os.arch").contains("64"), "64-bit Windows is required");
        Path workspace = local(value(config, "workspace"));
        Path mapped = workspace.resolve("minecraft/bin");
        Path lwjgl = workspace.resolve("libraries/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209"
                + "/lwjgl-2.9.4-nightly-20150209.jar");
        Path natives = workspace.resolve("libraries/natives");
        Path officialClient = workspace.resolve("jars/minecraft.jar");
        Path officialServer = root.resolve(value(serverArtifact, "local.path")).normalize();
        require(value(config, "server.jar.sha256").equals(value(serverArtifact, "expected.sha256")),
                "server descriptor drift");
        verify(officialClient, value(config, "client.jar.sha256"), -1L);
        verify(officialServer, value(config, "server.jar.sha256"),
                Long.parseLong(value(serverArtifact, "expected.bytes")));
        verify(lwjgl, value(config, "lwjgl.jar.sha256"), -1L);
        verify(natives.resolve("lwjgl64.dll"), value(config, "lwjgl64.dll.sha256"), -1L);
        require(Files.isRegularFile(mapped.resolve("net/minecraft/src/Tessellator.class")),
                "mapped Tessellator.class is absent; run the client smoke first");
        recreate(build); Path classes = compile(mapped, lwjgl);
        Outcome first = run(classes, mapped, lwjgl, natives, officialServer, build.resolve("first"));
        Outcome second = run(classes, mapped, lwjgl, natives, officialServer, build.resolve("second"));
        require(first.trace.equals(second.trace) && first.signature.equals(second.signature)
                && first.frame.equals(second.frame), "fresh native multiplayer scenarios diverged");
        require(first.frame.equals(value(config, "expected.frame.sha256")),
                "M26 frame drift: " + first.frame);
        require(first.signature.equals(value(config, "expected.signature")),
                "M26 signature drift: " + first.signature);
        String evidence = "id=" + ID + "\nprocesses=2\nservers=2\ncontext=Pbuffer"
                + "\ndisplay.created=false\nclient.sha256=" + value(config, "client.jar.sha256")
                + "\nserver.sha256=" + value(config, "server.jar.sha256") + "\ntrace=" + first.trace
                + "\nframe.sha256=" + first.frame + "\nsignature=" + first.signature + "\n";
        Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
        System.out.println("M26 native multiplayer render passed");
        System.out.println("  processes: 2 clients + 2 official servers");
        System.out.println("  path: protocol-14 pose -> Minecraft Tessellator -> LWJGL -> Pbuffer -> RGBA");
        System.out.println("  frame signature: " + first.frame);
        System.out.println("  signature: " + first.signature);
        System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
    }

    private Path compile(Path mapped, Path lwjgl) throws Exception {
        Path output = build.resolve("classes"); Files.createDirectories(output);
        List<String> command = new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8",
                "--release", "8", "-Xlint:all,-options", "-Werror", "-classpath",
                join(product("api"), mapped, lwjgl), "-d", output.toString()));
        command.addAll(javaFiles(root.resolve("adapters/b173-server/src/main/java")));
        command.addAll(javaFiles(smoke.resolve("src"))); capture(root, command); return output;
    }

    private Outcome run(Path classes, Path mapped, Path lwjgl, Path natives,
            Path officialServer, Path workspace) throws Exception {
        String output = capture(root, Arrays.asList("java", "-Djava.awt.headless=true",
                "-Dorg.lwjgl.librarypath=" + natives.toAbsolutePath(), "-classpath",
                join(classes, product("api"), mapped, lwjgl),
                "worldline.smoke.nativemulti.NativeMultiplayerSmoke", officialServer.toString(),
                workspace.toString(), Integer.toString(freePort()), value(config, "seed"),
                value(config, "username")));
        require(output.contains("WORLDLINE_M26_API=server,session,pose,native-render,frame"),
                "M26 API marker absent");
        require(output.contains("WORLDLINE_M26_CONTEXT=Pbuffer")
                && output.contains("WORLDLINE_M26_DISPLAY_CREATED=false")
                && output.contains("WORLDLINE_M26_GEOMETRY_PIXELS=1280"),
                "native offscreen proof is incomplete");
        require(line(output, "WORLDLINE_M26_RENDERER=").replace('\\', '/').contains("minecraft/bin/"),
                "wrong renderer provenance");
        return new Outcome(line(output, "WORLDLINE_M26_TRACE="),
                line(output, "WORLDLINE_M26_SIGNATURE="), line(output, "WORLDLINE_M26_FRAME="));
    }

    private int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); }
    }
    private void verify(Path path, String expected, long bytes) throws Exception {
        require(Files.isRegularFile(path), "native input is absent: " + path);
        if (bytes >= 0L) require(Files.size(path) == bytes, "native input size drift: " + path);
        require(digest(path).equals(expected), "native input hash drift: " + path);
    }
    private String digest(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) { byte[] buffer = new byte[8192]; int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count); }
        return HexFormat.of().formatHex(digest.digest());
    }
    private List<String> javaFiles(Path source) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) { return paths.filter(path -> path.toString().endsWith(".java"))
                .sorted().map(Path::toString).collect(Collectors.toList()); }
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
            try (Stream<Path> paths = Files.walk(target)) { for (Path path : paths.sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList())) Files.delete(path); }
        }
        Files.createDirectories(target);
    }
    private void load(Path path, Properties target) throws IOException {
        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { target.load(reader); }
    }
    private String value(Properties source, String key) { String result = source.getProperty(key);
        require(result != null && !result.trim().isEmpty(), "missing property " + key); return result.trim(); }
    private Path local(String value) { Path base = root.resolve("local").normalize();
        Path result = root.resolve(value).normalize(); require(result.startsWith(base), "workspace is not local");
        return result; }
    private Path product(String name) { return root.resolve(".worldline/build/classes").resolve(name); }
    private String join(Path... paths) { return Arrays.stream(paths).map(Path::toString)
            .collect(Collectors.joining(System.getProperty("path.separator"))); }
    private String line(String output, String prefix) { return output.lines().filter(row -> row.startsWith(prefix))
            .findFirst().orElseThrow(() -> new IllegalStateException("missing " + prefix)).substring(prefix.length()); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private static final class Outcome {
        final String trace, signature, frame;
        Outcome(String trace, String signature, String frame) {
            this.trace = trace; this.signature = signature; this.frame = frame;
        }
    }
}
