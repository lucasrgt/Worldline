import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Qualifies one Packet18 local request and named peer observation. */
public final class PeerSwingCycle {
    private static final String ID = "m69-peer-swing";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID), build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties(), artifact = new Properties();
    public static void main(String[] arguments) { if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/PeerSwingCycle.java " + ID); System.exit(2); }
        try { new PeerSwingCycle().execute(); }
        catch (Exception error) { System.err.println("peer swing cycle failed: " + error.getMessage()); System.exit(1); } }
    private void execute() throws Exception {
        load(smoke.resolve("smoke.properties"), config); load(root.resolve(
                "artifacts/minecraft-b1.7.3-server.properties"), artifact);
        require(ID.equals(value(config, "id")), "smoke descriptor id mismatch");
        require(value(config, "server.jar.sha256").equals(value(artifact, "expected.sha256")), "server descriptor drift");
        Path official = root.resolve(value(artifact, "local.path")).normalize(); verifyArtifact(official);
        recreate(build); Path classes = compile(); require(capture(Arrays.asList("java", "-classpath", classpath(classes),
                "worldline.b173server.B173SwingPacketFixture")).contains("PASS bytes=6"), "Packet18 fixture absent"); Outcome first = run(classes, official, build.resolve("first"));
        Outcome second = run(classes, official, build.resolve("second"));
        require(first.trace.equals(second.trace) && first.signature.equals(second.signature)
                && first.observation.equals(second.observation)
                && first.observation.equals(value(config, "expected.signal")), "fresh swing scenarios diverged");
        require(first.signature.equals(value(config, "expected.signature")), "M69 signature drift: " + first.signature);
        String evidence = "id=" + ID + "\nchild.processes=4\nsmoke.jvm=2\nserver.jvm=2\nwire.sessions=4\nserver.sha256="
                + value(artifact, "expected.sha256") + "\ntrace=" + first.trace + "\nfirst=" + first.observation
                + "\nsecond=" + second.observation + "\nsignature=" + first.signature + "\n";
        Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
        System.out.println("M69 peer swing passed"); System.out.println("  runtime: 2 smoke JVMs + 2 official server JVMs + 4 wire sessions");
        System.out.println("  path: Packet20/5 bootstrap -> Packet18 request -> named Packet18 peer observation");
        System.out.println("  observations: " + first.observation + " | " + second.observation);
        System.out.println("  signature: " + first.signature); System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
    }
    private Path compile() throws Exception { Path output = build.resolve("classes"); Files.createDirectories(output);
        List<String> command = new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8", "--release", "8",
                "-Xlint:all,-options", "-Werror", "-classpath", product("api").toString(), "-d", output.toString()));
        command.addAll(javaFiles(root.resolve("adapters/b173-server/src/main/java")));
        command.addAll(javaFiles(smoke.resolve("src"))); capture(command); return output; }
    private Outcome run(Path classes, Path official, Path workspace) throws Exception {
        String output = capture(Arrays.asList("java", "-classpath", classpath(classes),
                "worldline.smoke.peerswing.PeerSwingSmoke", official.toString(), workspace.toString(),
                Integer.toString(freePort()), value(config, "seed"), value(config, "actor"), value(config, "observer")));
        require(output.contains("WORLDLINE_M69_API=peer-swing,packet18,named-observation"), "M69 API marker absent");
        return new Outcome(line(output, "WORLDLINE_M69_TRACE="), line(output, "WORLDLINE_M69_SIGNATURE="),
                line(output, "WORLDLINE_M69_SWING=")); }
    private int freePort() throws Exception { try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); } }
    private void verifyArtifact(Path path) throws Exception { require(Files.isRegularFile(path), "server artifact absent");
        require(Files.size(path) == Long.parseLong(value(artifact, "expected.bytes")), "server size mismatch");
        require(digest(path, "SHA-1").equals(value(artifact, "expected.sha1")), "server SHA-1 mismatch");
        require(digest(path, "SHA-256").equals(value(artifact, "expected.sha256")), "server SHA-256 mismatch"); }
    private List<String> javaFiles(Path source) throws Exception { try (Stream<Path> paths = Files.walk(source)) {
        return paths.filter(path -> path.toString().endsWith(".java")).sorted().map(Path::toString).collect(Collectors.toList()); } }
    private String capture(List<String> command) throws Exception { Process process = new ProcessBuilder(command)
            .directory(root.toFile()).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(0) + " failed\n" + output); return output; }
    private void recreate(Path target) throws Exception { if (Files.exists(target)) { require(target.startsWith(
            root.resolve(".worldline")), "unsafe build path"); try (Stream<Path> paths = Files.walk(target)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(path); } }
        Files.createDirectories(target); }
    private void load(Path path, Properties target) throws Exception { try (java.io.Reader reader = Files
            .newBufferedReader(path, StandardCharsets.UTF_8)) { target.load(reader); } }
    private String value(Properties source, String key) { String result = source.getProperty(key);
        require(result != null && !result.trim().isEmpty(), "missing property " + key); return result.trim(); }
    private String digest(Path path, String algorithm) throws Exception { MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream input = Files.newInputStream(path)) { byte[] buffer = new byte[8192]; int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count); }
        return java.util.HexFormat.of().formatHex(digest.digest()); }
    private String classpath(Path classes) { return classes + System.getProperty("path.separator") + product("api"); }
    private Path product(String name) { return root.resolve(".worldline/build/classes").resolve(name); }
    private String line(String output, String prefix) { return output.lines().filter(value -> value.startsWith(prefix))
            .findFirst().orElseThrow(() -> new IllegalStateException("missing " + prefix)).substring(prefix.length()); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static final class Outcome { final String trace, signature, observation;
        Outcome(String trace, String signature, String observation) { this.trace = trace; this.signature = signature; this.observation = observation; } }
}
