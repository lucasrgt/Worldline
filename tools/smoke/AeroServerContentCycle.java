import java.io.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Qualifies one server-authored StationAPI block/BE rendered by the pinned Aero client. */
public final class AeroServerContentCycle {
    private static final String ID = "m72-aero-server-content";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties();
    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/AeroServerContentCycle.java " + ID); System.exit(2); }
        try { new AeroServerContentCycle().execute(); }
        catch (Exception error) { System.err.println("Aero server-content cycle failed: "
                + error.getMessage()); System.exit(1); }
    }
    private void execute() throws Exception {
        load(smoke.resolve("smoke.properties"), config);
        require(ID.equals(value("id")), "smoke descriptor id mismatch");
        Path checkout = root.resolve(value("aero.path")).normalize(); verifyCheckout(checkout);
        verifyBoundary(); recreate(build); buildAero(checkout); String aeroJar = digest(aeroJar(), "SHA-256");
        Outcome first = run(checkout, build.resolve("first"), 7207201);
        Outcome second = run(checkout, build.resolve("second"), 7207202); verifyCheckout(checkout);
        require(!first.nonce.equals(second.nonce), "fresh server nonces were not distinct");
        require(first.trace.equals(second.trace) && first.signature.equals(second.signature),
                "fresh server-content traces diverged");
        require(first.signature.equals(value("expected.signature")), "M72 signature drift: " + first.signature);
        String evidence = "id=" + ID + "\nruns=2\nserver.jvm=2\nclient.jvm=2\ncontent="
                + value("content.identifier") + "\naero.revision=" + value("aero.revision")
                + "\naero.jar.sha256=" + aeroJar + "\nfirst=" + first.observation + "\nsecond="
                + second.observation + "\ntrace=" + first.trace + "\nsignature=" + first.signature + "\n";
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M72 Aero server content passed");
        System.out.println("  path: StationAPI server content -> explicit state message -> real Aero renderer");
        System.out.println("  observations: " + first.observation + " | " + second.observation);
        System.out.println("  signature: " + first.signature);
        System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
    }
    private Outcome run(Path checkout, Path workspace, int nonce) throws Exception {
        Files.createDirectories(workspace); int port = freePort(), timeout = Integer.parseInt(value("timeout.seconds"));
        Path worktrees = root.resolve(".worldline/worktrees/m72-" + ProcessHandle.current().pid()
                + "-" + workspace.getFileName() + "-" + System.nanoTime());
        Path serverTree = worktrees.resolve("server"), clientTree = worktrees.resolve("client");
        Captured server = null;
        try {
            addWorktree(checkout, serverTree); addWorktree(checkout, clientTree);
            Path serverGame = workspace.resolve("server"), clientGame = workspace.resolve("client");
            Path init = smoke.resolve("aero-content.init.gradle"), log = clientGame.resolve("aero.log");
            server = Captured.start(serverTree.resolve("stationapi/test-bare"), command(serverTree, init,
                    "server", serverGame, port, nonce, null)); server.awaitText("Done (", timeout);
            String client = Captured.run(clientTree.resolve("stationapi/test-bare"), command(clientTree, init,
                    "client", clientGame, port, nonce, log), timeout);
            require(client.contains("Loading 46 mods:") && client.contains("- aero-model-lib 3.0.0")
                    && client.contains("- worldline-m72-content 1.0.0"), "client mod boundary drifted\n" + diagnostic(client));
            require(client.contains("[WorldlineContent] packet1") && client.contains("[WorldlineContent] packet13")
                    && client.contains("[WorldlineContent] complete frames=" + value("ready.frames"))
                    && client.contains("BUILD SUCCESSFUL"), "client lifecycle proof absent\n" + diagnostic(client));
            String message = line(client, "[WorldlineContent] message ");
            String applied = line(client, "[WorldlineContent] applied ");
            String rendered = line(client, "[WorldlineContent] rendered ");
            require(same(message, applied, "x", "y", "z", "nonce")
                    && same(applied, rendered, "x", "y", "z", "raw", "nonce"), "client content identity drifted");
            require(number(rendered, "nonce") == nonce && rendered.contains("identifier=" + value("content.identifier")),
                    "client nonce/identifier drifted");
            String aero = client.lines().dropWhile(row -> !row.startsWith("[WorldlineContent] rendered "))
                    .filter(row -> row.startsWith("[Aero_")).filter(this::contentAeroRow).findFirst()
                    .orElseThrow(() -> new IllegalStateException("post-render Aero row absent"));
            require(Files.isRegularFile(log) && Files.readString(log).contains(aero), "Aero file did not preserve content row");
            server.write("save-all\nstop\n"); server.finish(45); String serverText = server.output(); server = null;
            require(serverText.contains("Loading 40 mods:") && serverText.contains("- worldline-m72-content 1.0.0")
                    && !serverText.contains("- aero-model-lib "), "server unexpectedly loaded Aero\n" + diagnostic(serverText));
            String placed = line(serverText, "[WorldlineContent] placed ");
            require(same(placed, rendered, "x", "y", "z", "raw", "nonce")
                    && placed.contains("identifier=" + value("content.identifier")), "server/client content drifted");
            require(serverText.contains(value("username") + " [") && serverText.contains("logged in with entity id")
                    && serverText.contains(value("username") + " lost connection")
                    && serverText.contains("Stopping server") && serverText.contains("BUILD SUCCESSFUL"),
                    "server lifecycle proof absent\n" + diagnostic(serverText));
            verifyWorktree(serverTree); verifyWorktree(clientTree);
            String trace = "v1|server=stationapi-modded-without-aero|content=server-authored-custom-block-be"
                    + "|sync=explicit-m72-message-with-server-only-nonce|client=real-stationapi-aero3"
                    + "|identity=exact-identifier-coordinates-raw-nonce|render=aero-return+20-frames"
                    + "|aero=content-row-visible-chunks|runs=2-distinct-nonces|shutdown=clean|performance=not-claimed";
            return new Outcome(trace, sha256(trace), "nonce=" + nonce + ";raw=" + number(rendered, "raw")
                    + ";xyz=" + number(rendered, "x") + "," + number(rendered, "y") + ","
                    + number(rendered, "z") + ";frames=" + value("ready.frames"));
        } finally {
            if (server != null) { try { server.write("stop\n"); server.finish(20); } catch (Exception error) { server.kill(); } }
            removeWorktree(checkout, clientTree); removeWorktree(checkout, serverTree);
        }
    }
    private List<String> command(Path tree, Path init, String role, Path game, int port, int nonce, Path log) {
        String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        List<String> result = new ArrayList<>(Arrays.asList(tree.resolve("stationapi/test-bare").resolve(wrapper).toString(),
                "--no-daemon", "--init-script", init.toString(), role.equals("server") ? "runServer" : "runClient",
                "-PworldlineRole=" + role, "-PworldlineRunDir=" + game, "-PworldlinePort=" + port));
        if (role.equals("server")) result.add("-PworldlineNonce=" + nonce);
        if (log != null) { result.add("-PworldlineLog=" + log); result.add("-PworldlineUsername=" + value("username"));
            result.add("-PworldlineFrames=" + value("ready.frames")); }
        return result;
    }
    private void buildAero(Path checkout) throws Exception {
        String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        Path tree = root.resolve(".worldline/worktrees/m72-build-" + ProcessHandle.current().pid() + "-" + System.nanoTime());
        try {
            addWorktree(checkout, tree); Path stationapi = tree.resolve("stationapi");
            String output = Captured.run(stationapi, Arrays.asList(stationapi.resolve(wrapper).toString(),
                    "--no-daemon", "remapJar"), Integer.parseInt(value("timeout.seconds")));
            Path generated = stationapi.resolve("build/libs/aero-model-lib-3.0.0.jar");
            require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(generated), "pinned Aero remapJar failed");
            Files.copy(generated, aeroJar(), StandardCopyOption.REPLACE_EXISTING); verifyWorktree(tree);
        } finally { removeWorktree(checkout, tree); }
    }
    private Path aeroJar() { return build.resolve("aero-model-lib-3.0.0.jar"); }
    private boolean contentAeroRow(String row) {
        if (!(row.startsWith("[Aero_FrameSpike]") || row.startsWith("[Aero_GC]") || row.startsWith("[Aero_Pulse]"))) return false;
        Map<String, String> fields = fields(row); return whole(fields, "visibleChunks") > 0
                && whole(fields, "atRestRenders") > 0 && whole(fields, "atRestListCalls") > 0;
    }
    private void verifyBoundary() throws IOException {
        Path sources = smoke.resolve("runtime-src/worldline/m72");
        try (Stream<Path> paths = Files.walk(sources)) { for (Path path : paths.filter(p -> p.toString().endsWith(".java"))
                .filter(p -> !p.toString().contains(File.separator + "client" + File.separator))
                .filter(p -> !p.toString().contains(File.separator + "mixin" + File.separator)).toList()) {
            String text = Files.readString(path); require(!text.contains("aero.modellib")
                    && !text.contains("net.minecraft.client") && !text.contains("org.lwjgl"),
                    "server-safe source closure imports client code: " + path); } }
    }
    private void verifyCheckout(Path checkout) throws Exception {
        require(Files.isDirectory(checkout.resolve(".git")), "Aero checkout missing");
        require(git(checkout, "remote", "get-url", "origin").trim().equals(value("aero.repository")), "unexpected Aero origin");
        require(git(checkout, "rev-parse", "HEAD").trim().equals(value("aero.revision")), "unexpected Aero revision");
        require(git(checkout, "status", "--porcelain", "--untracked-files=all").trim().isEmpty(), "Aero checkout dirty");
    }
    private void addWorktree(Path checkout, Path target) throws Exception {
        Files.createDirectories(target.getParent()); require(!Files.exists(target), "worktree path already exists");
        git(checkout, "worktree", "add", "--detach", target.toString(), value("aero.revision")); verifyWorktree(target);
    }
    private void verifyWorktree(Path target) throws Exception {
        require(git(target, "rev-parse", "HEAD").trim().equals(value("aero.revision")), "worktree revision drift");
        require(git(target, "status", "--porcelain", "--untracked-files=all").trim().isEmpty(), "worktree dirty: " + target);
    }
    private void removeWorktree(Path checkout, Path target) {
        try {
            if (registered(checkout, target)) { try { git(checkout, "worktree", "remove", "--force", target.toString()); }
                catch (Exception error) { if (registered(checkout, target)) throw error; } }
            if (Files.exists(target)) deleteWorktreeRemainder(target);
        } catch (Exception error) { throw new IllegalStateException("could not remove M72 worktree " + target, error); }
    }
    private boolean registered(Path checkout, Path target) throws Exception { for (String row : git(checkout,
            "worktree", "list", "--porcelain").split("\\R")) if (row.startsWith("worktree ")
            && Paths.get(row.substring(9)).toAbsolutePath().normalize().equals(target.toAbsolutePath().normalize())) return true;
        return false; }
    private void deleteWorktreeRemainder(Path target) throws Exception { Path allowed = root.resolve(".worldline/worktrees").normalize();
        Path exact = target.toAbsolutePath().normalize(); require(exact.startsWith(allowed) && !exact.equals(allowed), "unsafe worktree remainder");
        try (Stream<Path> paths = Files.walk(exact)) { for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
            Files.deleteIfExists(path); } }
    private String git(Path directory, String... arguments) throws Exception { List<String> command = new ArrayList<>();
        command.add("git"); command.add("-C"); command.add(directory.toString()); command.addAll(Arrays.asList(arguments));
        return Captured.run(root, command, 60); }
    private boolean same(String one, String two, String... names) { for (String name : names)
        if (number(one, name) != number(two, name)) return false; return true; }
    private int number(String marker, String name) { for (String token : marker.split(" +"))
        if (token.startsWith(name + "=")) return Integer.parseInt(token.substring(name.length() + 1));
        throw new IllegalStateException("missing " + name + " in " + marker); }
    private Map<String, String> fields(String row) { Map<String, String> result = new HashMap<>();
        for (String token : row.substring(row.indexOf(']') + 1).trim().split(" +")) { int equals = token.indexOf('=');
            if (equals > 0) require(result.put(token.substring(0, equals), token.substring(equals + 1)) == null,
                    "duplicate Aero field"); } return result; }
    private long whole(Map<String, String> fields, String name) { try { String value = fields.get(name);
        require(value != null, "missing Aero field " + name); return Long.parseLong(value); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid Aero counter " + name, error); } }
    private String line(String text, String prefix) { return text.lines().filter(row -> row.startsWith(prefix))
            .findFirst().orElseThrow(() -> new IllegalStateException("missing " + prefix)); }
    private String diagnostic(String text) { return text.lines().filter(row -> row.contains("WorldlineContent")
            || row.contains("Loading ") && row.contains("mods:") || row.contains("aero-model-lib")
            || row.contains("logged in with entity") || row.contains("lost connection")
            || row.contains("Exception") || row.contains("BUILD ")).collect(Collectors.joining("\n")); }
    private int freePort() throws IOException { try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); } }
    private void recreate(Path target) throws IOException { if (Files.exists(target)) { require(target.startsWith(
            root.resolve(".worldline/smokes")) && !target.equals(root), "unsafe evidence path"); try (Stream<Path> paths = Files.walk(target)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path); } }
        Files.createDirectories(target); }
    private void load(Path path, Properties target) throws IOException { try (Reader reader = Files.newBufferedReader(path)) { target.load(reader); } }
    private String value(String key) { String result = config.getProperty(key); require(result != null
            && !result.trim().isEmpty(), "missing property " + key); return result.trim(); }
    private String digest(Path path, String algorithm) throws Exception { MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream input = Files.newInputStream(path)) { byte[] buffer = new byte[8192]; int count;
            while ((count = input.read(buffer)) >= 0) digest.update(buffer, 0, count); }
        return java.util.HexFormat.of().formatHex(digest.digest()); }
    private static String sha256(String value) throws Exception { MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return java.util.HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8))); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private record Outcome(String trace, String signature, String observation, String nonce) {
        Outcome(String trace, String signature, String observation) { this(trace, signature, observation,
                observation.substring(observation.indexOf('=') + 1, observation.indexOf(';'))); } }
    private static final class Captured {
        final Process process; final StringBuilder text = new StringBuilder(); final Thread reader; int exitCode = -1;
        private Captured(Process process) { this.process = process; reader = new Thread(() -> { try (BufferedReader input =
                new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) { String line;
            while ((line = input.readLine()) != null) synchronized (text) { text.append(line).append('\n'); text.notifyAll(); }
        } catch (IOException error) { synchronized (text) { text.append("[capture-error] ").append(error).append('\n'); } } });
            reader.setDaemon(true); reader.start(); }
        static Captured start(Path directory, List<String> command) throws IOException { return new Captured(new ProcessBuilder(command)
                .directory(directory.toFile()).redirectErrorStream(true).start()); }
        static String run(Path directory, List<String> command, int timeout) throws Exception { Captured value = start(directory, command);
            value.finish(timeout); require(value.exitCode == 0, command.get(0) + " failed\n" + value.output()); return value.output(); }
        void write(String value) throws IOException { process.getOutputStream().write(value.getBytes(StandardCharsets.UTF_8)); process.getOutputStream().flush(); }
        void awaitText(String expected, int timeout) throws Exception { long deadline = System.currentTimeMillis() + timeout * 1000L;
            synchronized (text) { while (!text.toString().contains(expected) && process.isAlive()
                    && System.currentTimeMillis() < deadline) text.wait(100L); }
            if (!output().contains(expected)) { kill(); throw new IllegalStateException("missing " + expected + "\n" + output()); } }
        void finish(int timeout) throws Exception { if (!process.waitFor(timeout, TimeUnit.SECONDS)) { kill();
            throw new IllegalStateException("process timeout\n" + output()); } exitCode = process.exitValue(); reader.join(5000L);
            require(exitCode == 0, "process exit " + exitCode + "\n" + output()); }
        void kill() { process.descendants().forEach(ProcessHandle::destroyForcibly); process.destroyForcibly(); }
        String output() { synchronized (text) { return text.toString(); } }
    }
}
