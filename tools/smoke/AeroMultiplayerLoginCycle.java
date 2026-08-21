import java.io.*;
import java.net.ServerSocket;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Probes a real StationAPI/Aero client login against the official dedicated server. */
public final class AeroMultiplayerLoginCycle {
    private static final String ID = "m68-aero-multiplayer-login";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties(), artifact = new Properties();
    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/AeroMultiplayerLoginCycle.java " + ID); System.exit(2); }
        try { new AeroMultiplayerLoginCycle().execute(); }
        catch (Exception error) { System.err.println("Aero multiplayer login cycle failed: "
                + error.getMessage()); System.exit(1); }
    }
    private void execute() throws Exception {
        load(smoke.resolve("smoke.properties"), config);
        load(root.resolve("artifacts/minecraft-b1.7.3-server.properties"), artifact);
        require(ID.equals(value(config, "id")), "smoke descriptor id mismatch");
        require(value(config, "server.jar.sha256").equals(value(artifact, "expected.sha256")),
                "server descriptor drift");
        Path official = root.resolve(value(artifact, "local.path")).normalize(); verifyArtifact(official);
        Path checkout = root.resolve(value(config, "aero.path")).normalize(); verifyCheckout(checkout);
        recreate(build); Outcome first = run(checkout, official, build.resolve("first"));
        Outcome second = run(checkout, official, build.resolve("second")); verifyCheckout(checkout);
        require(first.trace.equals(second.trace) && first.signature.equals(second.signature),
                "fresh Aero multiplayer login traces diverged");
        require(first.signature.equals(value(config, "expected.signature")),
                "M68 signature drift: " + first.signature);
        String evidence = "id=" + ID + "\nruns=2\nserver.jvm=2\nclient=real-stationapi-aero"
                + "\naero.revision=" + value(config, "aero.revision") + "\nserver.sha256="
                + value(artifact, "expected.sha256") + "\ntrace=" + first.trace + "\nfirst="
                + first.observation + "\nsecond=" + second.observation + "\nsignature=" + first.signature + "\n";
        Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
        System.out.println("M68 Aero multiplayer login passed");
        System.out.println("  path: real StationAPI/Aero client -> official localhost server -> remote chunk -> frames/log");
        System.out.println("  observations: " + first.observation + " | " + second.observation);
        System.out.println("  signature: " + first.signature);
        System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
    }
    private Outcome run(Path checkout, Path official, Path workspace) throws Exception {
        Files.createDirectories(workspace); int port = freePort(); Server server = null;
        try {
            server = Server.start(official, workspace.resolve("server"), port,
                    value(config, "seed"), Integer.parseInt(value(config, "timeout.seconds")));
            Path test = checkout.resolve("stationapi/test"), log = workspace.resolve("aero.log");
            String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
            List<String> command = Arrays.asList(test.resolve(wrapper).toString(), "--no-daemon", "--init-script",
                    root.resolve(value(config, "runner")).toString(), "runClient", "-PworldlinePort=" + port,
                    "-PworldlineFrames=" + value(config, "ready.frames"),
                    "-PworldlineUsername=" + value(config, "username"), "-PworldlineLog=" + log);
            String output = Captured.run(test, command, Integer.parseInt(value(config, "timeout.seconds")));
            require(output.contains("[WorldlineMultiplayer] connect username=" + value(config, "username")),
                    "real client connect marker absent\n" + output);
            String ready = line(output, "[WorldlineMultiplayer] ready ");
            String complete = line(output, "[WorldlineMultiplayer] complete ");
            require(output.contains("BUILD SUCCESSFUL") && !output.contains("[WorldlineMultiplayer] timeout"),
                    "Aero runClient did not stop normally\n" + output);
            require(number(ready, "chunks") > 0 && number(complete, "frames")
                    >= Integer.parseInt(value(config, "ready.frames"))
                    && number(complete, "aeroLinesAfterReady") > 0, "remote readiness marker drifted");
            require(Files.isRegularFile(log) && Files.size(log) > 0L, "Aero multiplayer log absent");
            List<String> rows = Files.readAllLines(log, StandardCharsets.UTF_8);
            int baseline = number(ready, "aeroBaseline");
            require(baseline >= 0 && baseline <= rows.size(), "Aero readiness baseline drifted");
            long parseable = 0;
            for (String row : rows.subList(baseline, rows.size()))
                if (row.startsWith("[Aero_")) { parseAero(row); parseable++; }
            require(parseable > 0, "post-login Aero frame vocabulary absent");
            server.stop(); String serverLog = server.output(); server = null;
            require(serverLog.contains(value(config, "username") + " [")
                    && serverLog.contains("logged in with entity id")
                    && serverLog.contains(value(config, "username") + " lost connection")
                    && serverLog.contains("Stopping server"), "official server login/shutdown proof absent\n" + serverLog);
            String trace = "v1|client=real-b1.7.3+fabric+stationapi+aero3|server=official-b1.7.3"
                    + "|transport=localhost-protocol14|identity=named-login|remote=packet51-applied"
                    + "|render=post-ready-frames|aero=post-ready-log|shutdown=clean";
            return new Outcome(trace, sha256(trace), "chunks=" + number(ready, "chunks")
                    + ";frames=" + number(complete, "frames") + ";aero-lines="
                    + number(complete, "aeroLinesAfterReady") + ";parseable=" + parseable);
        } finally { if (server != null) server.stopQuietly(); }
    }
    private void verifyCheckout(Path checkout) throws Exception {
        require(Files.isDirectory(checkout.resolve(".git")), "Aero checkout missing");
        require(Captured.run(root, Arrays.asList("git", "-C", checkout.toString(), "remote", "get-url", "origin"), 30)
                .trim().equals(value(config, "aero.repository")), "unexpected Aero origin");
        require(Captured.run(root, Arrays.asList("git", "-C", checkout.toString(), "rev-parse", "HEAD"), 30)
                .trim().equals(value(config, "aero.revision")), "unexpected Aero revision");
        require(Captured.run(root, Arrays.asList("git", "-C", checkout.toString(), "status", "--porcelain"), 30)
                .trim().isEmpty(), "Aero checkout has tracked changes");
    }
    private void verifyArtifact(Path path) throws Exception {
        require(Files.isRegularFile(path), "server artifact absent; run Acquire.java server");
        require(Files.size(path) == Long.parseLong(value(artifact, "expected.bytes")), "server size mismatch");
        require(digest(path, "SHA-1").equals(value(artifact, "expected.sha1")), "server SHA-1 mismatch");
        require(digest(path, "SHA-256").equals(value(artifact, "expected.sha256")), "server SHA-256 mismatch");
    }
    private int number(String marker, String name) { for (String token : marker.split(" +"))
        if (token.startsWith(name + "=")) return Integer.parseInt(token.substring(name.length() + 1));
        throw new IllegalStateException("missing " + name + " in " + marker); }
    private void parseAero(String row) {
        require(row.startsWith("[Aero_") && row.indexOf(']') > 6,
                "invalid Aero row type"); Map<String, String> fields = new HashMap<>();
        for (String token : row.substring(row.indexOf(']') + 1).trim().split(" +")) {
            int equals = token.indexOf('='); if (equals > 0 && equals < token.length() - 1)
                require(fields.put(token.substring(0, equals), token.substring(equals + 1)) == null,
                        "duplicate Aero field"); }
        for (String name : Arrays.asList("frameMs", "compileChunksMs", "compileChunksMaxMs", "gcTimeDeltaMs"))
            require(decimal(fields, name).signum() >= 0, "negative Aero timing " + name);
        for (String name : Arrays.asList("compileChunksCalls", "compileChunksSkipped", "compileBudgetSkipped",
                "batchQueued", "cellQueued", "beViewCulled")) require(whole(fields, name) >= 0, "negative Aero counter " + name);
        require(whole(fields, "visibleChunks") > 0, "post-ready Aero row has no visible chunks");
    }
    private BigDecimal decimal(Map<String, String> fields, String name) { try {
        return new BigDecimal(required(fields, name)); } catch (NumberFormatException error) {
        throw new IllegalStateException("invalid Aero timing " + name, error); } }
    private long whole(Map<String, String> fields, String name) { try {
        return Long.parseLong(required(fields, name)); } catch (NumberFormatException error) {
        throw new IllegalStateException("invalid Aero counter " + name, error); } }
    private String required(Map<String, String> fields, String name) { String value = fields.get(name);
        require(value != null && !value.isEmpty(), "missing Aero field " + name); return value; }
    private String line(String text, String prefix) { return text.lines().filter(row -> row.startsWith(prefix))
            .findFirst().orElseThrow(() -> new IllegalStateException("missing " + prefix)).substring(prefix.length()); }
    private int freePort() throws IOException { try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); } }
    private void recreate(Path target) throws IOException { if (Files.exists(target)) { require(target.startsWith(
            root.resolve(".worldline")) && !target.equals(root), "unsafe build path"); try (Stream<Path> paths = Files.walk(target)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(path); } }
        Files.createDirectories(target); }
    private void load(Path path, Properties target) throws IOException { try (Reader reader = Files.newBufferedReader(
            path, StandardCharsets.UTF_8)) { target.load(reader); } }
    private String value(Properties source, String key) { String result = source.getProperty(key);
        require(result != null && !result.trim().isEmpty(), "missing property " + key); return result.trim(); }
    private String digest(Path path, String algorithm) throws Exception { MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream input = Files.newInputStream(path)) { byte[] buffer = new byte[8192]; int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count); }
        return java.util.HexFormat.of().formatHex(digest.digest()); }
    private static String sha256(String value) throws Exception { MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return java.util.HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8))); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static final class Outcome { final String trace, signature, observation;
        Outcome(String trace, String signature, String observation) { this.trace = trace; this.signature = signature; this.observation = observation; } }
    private static final class Server {
        final Captured process; final Writer input;
        private Server(Captured process) { this.process = process; this.input = new OutputStreamWriter(
                process.process.getOutputStream(), StandardCharsets.UTF_8); }
        static Server start(Path jar, Path workspace, int port, String seed, int timeout) throws Exception {
            Files.createDirectories(workspace); String properties = "server-port=" + port + "\nserver-ip=127.0.0.1"
                    + "\nonline-mode=false\nlevel-seed=" + seed + "\nspawn-monsters=false\nspawn-animals=false"
                    + "\npvp=true\nallow-flight=true\nview-distance=3\n";
            Files.write(workspace.resolve("server.properties"), properties.getBytes(StandardCharsets.ISO_8859_1));
            Captured captured = Captured.start(workspace, Arrays.asList("java", "-jar", jar.toString(), "nogui"));
            captured.awaitText("Done (", timeout); return new Server(captured);
        }
        void stop() throws Exception { input.write("save-all\nstop\n"); input.flush(); process.finish(45);
            require(process.exitCode == 0, "official server exit drift"); }
        void stopQuietly() { try { if (process.process.isAlive()) { input.write("stop\n"); input.flush();
                process.finish(20); } } catch (Exception error) { process.kill(); } }
        String output() { return process.output(); }
    }
    private static final class Captured {
        final Process process; final StringBuilder text = new StringBuilder(); final Thread reader; int exitCode = -1;
        private Captured(Process process) { this.process = process; this.reader = new Thread(() -> {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line; while ((line = input.readLine()) != null) synchronized (text) { text.append(line).append('\n'); text.notifyAll(); }
            } catch (IOException error) { synchronized (text) { text.append("[capture-error] ").append(error).append('\n'); } }
        }); reader.setDaemon(true); reader.start(); }
        static Captured start(Path directory, List<String> command) throws IOException { return new Captured(new ProcessBuilder(command)
                .directory(directory.toFile()).redirectErrorStream(true).start()); }
        static String run(Path directory, List<String> command, int timeout) throws Exception { Captured value = start(directory, command);
            value.finish(timeout); require(value.exitCode == 0, command.get(0) + " failed\n" + value.output()); return value.output(); }
        void awaitText(String expected, int timeout) throws Exception { long deadline = System.currentTimeMillis() + timeout * 1000L;
            synchronized (text) { while (!text.toString().contains(expected) && process.isAlive()
                    && System.currentTimeMillis() < deadline) text.wait(100L); }
            if (!output().contains(expected)) { kill(); throw new IllegalStateException("missing " + expected + "\n" + output()); } }
        void finish(int timeout) throws Exception { if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                kill(); throw new IllegalStateException("process timeout\n" + diagnostic()); }
            exitCode = process.exitValue(); reader.join(5000L); }
        void kill() { process.descendants().forEach(ProcessHandle::destroyForcibly); process.destroyForcibly(); }
        String output() { synchronized (text) { return text.toString(); } }
        String diagnostic() { return output().lines().filter(row -> row.contains("WorldlineMultiplayer")
                || row.contains("Mixin") && (row.contains("ERROR") || row.contains("Exception"))
                || row.contains("logged in with entity") || row.contains("lost connection"))
                .collect(Collectors.joining("\n")); }
    }
}
