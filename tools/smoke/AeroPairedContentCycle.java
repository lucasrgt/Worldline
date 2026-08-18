import java.io.*;
import java.math.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;

/** Runs two balanced absent/present pairs around one exact synchronized-content activation. */
public final class AeroPairedContentCycle {
    private static final String ID = "m73-paired-aero-content", TRIGGER = "[WorldlinePairContent] trigger ", COMPLETE = "[WorldlinePairContent] complete ";
    private final Path root = Paths.get("").toAbsolutePath().normalize(), smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID); private final Properties config = new Properties();
    public static void main(String[] arguments) { if (!Arrays.equals(arguments, new String[]{ID})) {
        System.err.println("usage: java tools/smoke/AeroPairedContentCycle.java " + ID); System.exit(2); }
        try { new AeroPairedContentCycle().execute(); } catch (Exception error) {
            System.err.println("paired Aero content cycle failed: " + error.getMessage()); System.exit(1); } }
    private void execute() throws Exception {
        load(smoke.resolve("smoke.properties"), config); require(ID.equals(value("id")), "id drift");
        require(value("pairs").equals("2") && value("warmup.frames").equals("300") && value("warmup.millis").equals("5000")
                && value("window.frames").equals("720") && value("window.millis").equals("12000")
                && value("minimum.rows").equals("45"), "frozen M73 design drift");
        Path checkout = root.resolve(value("aero.path")).normalize(); verifyCheckout(checkout); verifyBoundary(); recreate(build); buildAero(checkout);
        String[] order = {"present", "absent", "absent", "present"}; List<Arm> arms = new ArrayList<>(); Plan[] plans = new Plan[2];
        int start = Integer.getInteger("worldline.m73.armStart", 0), limit = Integer.getInteger("worldline.m73.armLimit", order.length - start);
        require(start >= 0 && limit > 0 && start + limit <= order.length, "arm range drift"); boolean diagnostic = start != 0 || limit != order.length;
        require(!diagnostic || Boolean.getBoolean("worldline.m73.diagnostic"), "partial run requires diagnostic opt-in");
        for (int index = start; index < start + limit; index++) { int pair = index / 2, nonce = 7307301 + pair;
            verifyCheckout(checkout); Arm arm = run(checkout, build.resolve("pair-" + (pair + 1) + "-" + order[index]), order[index], pair, nonce, plans[pair]); arms.add(arm);
            if (plans[pair] == null) plans[pair] = new Plan(arm.x, arm.baseY, arm.baseZ); verifyCheckout(checkout); }
        if (diagnostic) { System.out.println("M73 diagnostic arm passed; qualification not attempted arms=" + limit);
            for (Arm arm : arms) System.out.println("  " + arm.summary()); return; }
        Pair first = pair(arms.get(0), arms.get(1)), second = pair(arms.get(2), arms.get(3));
        require(first.absent.nonce == first.present.nonce && second.absent.nonce == second.present.nonce
                && first.absent.nonce != second.absent.nonce, "pair nonce design drift");
        String trace = "v1|design=2-balanced-pairs-P/A+A/P|hosts=fresh-stationapi-server+real-aero-client"
                + "|equivalence=same-mod-seed-name-heap-logger-pair-nonce|anchor=exact-activate+tracked-plan-ready"
                + "|absent=planned16-placed0-rendered0|present=planned16-placed16-explicitly-synced16-rendered16"
                + "|warmup=min300frames+5s|window=min720frames+12s|rows=min45-threshold-gc-heartbeat-selected"
                + "|logger=threshold25-heartbeat200-sync-false|metrics=descriptive-paired-dynamic-only"
                + "|causality-performance-historical-lag=not-claimed|shutdown=clean";
        String signature = sha256(trace); require(signature.equals(value("expected.signature")), "M73 signature drift: " + signature);
        String evidence = "id=" + ID + "\npairs=2\narms=4\nserver.jvm=4\nclient.jvm=4\nfirst=" + first.summary()
                + "\nsecond=" + second.summary() + "\ntrace=" + trace + "\nsignature=" + signature + "\n";
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M73 paired Aero content passed"); System.out.println("  pair 1: " + first.summary());
        System.out.println("  pair 2: " + second.summary()); System.out.println("  signature: " + signature);
    }
    private Arm run(Path checkout, Path workspace, String mode, int pair, int nonce, Plan plan) throws Exception {
        Files.createDirectories(workspace); int port = freePort(), timeout = Integer.parseInt(value("timeout.seconds"));
        Path base = root.resolve(".worldline/worktrees/m73-" + ProcessHandle.current().pid() + "-" + pair + "-" + mode + "-" + System.nanoTime());
        Path serverTree = base.resolve("server"), clientTree = base.resolve("client"); Captured server = null;
        try {
            addWorktree(checkout, serverTree); addWorktree(checkout, clientTree); Path init = root.resolve(value("runner"));
            Path serverGame = workspace.resolve("server"), clientGame = workspace.resolve("client"), log = clientGame.resolve("aero.log");
            server = startServer(serverTree.resolve("stationapi/test-bare"), command(serverTree, init, "server", serverGame, port, nonce, mode, null, plan), timeout);
            String client = runGradle(clientTree.resolve("stationapi/test-bare"),
                    command(clientTree, init, "client", clientGame, port, nonce, mode, log, null), timeout);
            require(client.contains("Loading 46 mods:") && client.contains("- aero-model-lib 3.0.0")
                    && client.contains("- worldline-m73-content 1.0.0") && client.contains("BUILD SUCCESSFUL"), "client boundary drift\n" + diagnostic(client));
            require(client.contains("[WorldlinePairContent] packet1") && client.contains("[WorldlinePairContent] packet13"), "client readiness absent");
            require(count(client, "[WorldlinePairContent] plan-ready") == 1, "tracked plan readiness absent");
            String trigger = unique(client, TRIGGER), complete = unique(client, COMPLETE); require(trigger.contains("mode=" + mode + " nonce=" + nonce)
                    && trigger.contains("logger=25/200/false"), "trigger/logger drift");
            require(markerNumber(trigger, "warmFrames") >= Integer.parseInt(value("warmup.frames"))
                    && markerNumber(trigger, "warmMs") >= Integer.parseInt(value("warmup.millis")), "short warmup boundary");
            int expected = mode.equals("present") ? 16 : 0; require(complete.contains("mode=" + mode) && complete.contains("rendered=" + expected), "completion drift: " + complete);
            require(markerNumber(complete, "frames") >= Integer.parseInt(value("window.frames"))
                    && markerNumber(complete, "windowMs") >= Integer.parseInt(value("window.millis")), "short renderer window");
            Set<String> applied = cellSet(client, "[WorldlinePairContent] applied "), rendered = cellSet(client, "[WorldlinePairContent] rendered ");
            require(applied.size() == expected && rendered.size() == expected && applied.equals(rendered), "content identity-set drift");
            List<String> selected = rows(client, trigger, complete); require(selected.size() >= Integer.parseInt(value("minimum.rows")), "insufficient rows: " + selected.size());
            List<String> file = Files.readAllLines(log, StandardCharsets.UTF_8); require(subsequence(selected, file), "stdout/file row drift");
            List<Row> parsed = selected.stream().map(this::parse).collect(Collectors.toList());
            server.write("save-all\nstop\n"); server.finish(45); String serverText = server.output(); server = null;
            require(serverText.contains("Loading 40 mods:") && serverText.contains("- worldline-m73-content 1.0.0")
                    && !serverText.contains("- aero-model-lib ") && serverText.contains("BUILD SUCCESSFUL"), "server boundary drift\n" + diagnostic(serverText));
            String scene = unique(serverText, "[WorldlinePairContent] scene "); require(scene.contains("mode=" + mode + " planned=16 placed=" + expected)
                    && scene.contains("yaw=-90 pitch=0 nonce=" + nonce), "scene drift: " + scene);
            int x = markerNumber(scene, "x"), baseY = markerNumber(scene, "baseY"), baseZ = markerNumber(scene, "baseZ"), raw = markerNumber(scene, "raw");
            require(plan == null || plan.x == x && plan.y == baseY && plan.z == baseZ, "supplied pair plan drift");
            require(applied.equals(expectedCells(mode, nonce, x, baseY, baseZ)), "content set/plan drift");
            require(count(serverText, "[WorldlinePairContent] activation ") == 1 && count(serverText, "[WorldlinePairContent] tracking-ready ") == 1
                    && serverText.contains(value("username") + " lost connection")
                    && serverText.contains("Stopping server"), "server lifecycle drift\n" + diagnostic(serverText));
            require(serverText.indexOf("[WorldlinePairContent] activation ") < serverText.indexOf("[WorldlinePairContent] tracking-ready ")
                    && serverText.indexOf("[WorldlinePairContent] tracking-ready ") < serverText.indexOf("[WorldlinePairContent] scene ")
                    && serverText.indexOf("[WorldlinePairContent] scene ") < serverText.indexOf(value("username") + " lost connection"), "server event order drift");
            verifyWorktree(serverTree); verifyWorktree(clientTree); return new Arm(mode, nonce, parsed, expected,
                    x, baseY, baseZ, raw);
        } finally { if (server != null) { try { server.write("stop\n"); server.finish(20); } catch (Exception error) { server.kill(); } }
            removeWorktree(checkout, clientTree); removeWorktree(checkout, serverTree); }
    }
    private List<String> command(Path tree, Path init, String role, Path game, int port, int nonce, String mode, Path log, Plan plan) {
        String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        List<String> result = new ArrayList<>(Arrays.asList(tree.resolve("stationapi/test-bare").resolve(wrapper).toString(), "--no-daemon",
                "--init-script", init.toString(), role.equals("server") ? "runServer" : "runClient", "-PworldlineRole=" + role,
                "-PworldlineRunDir=" + game, "-PworldlinePort=" + port, "-PworldlineMode=" + mode, "-PworldlineNonce=" + nonce));
        if (plan != null) result.addAll(Arrays.asList("-PworldlinePlanX=" + plan.x, "-PworldlinePlanY=" + plan.y, "-PworldlinePlanZ=" + plan.z));
        if (log != null) result.addAll(Arrays.asList("-PworldlineLog=" + log, "-PworldlineUsername=" + value("username"),
                "-PworldlineWarmupFrames=" + value("warmup.frames"), "-PworldlineWarmupMillis=" + value("warmup.millis"),
                "-PworldlineWindowFrames=" + value("window.frames"), "-PworldlineWindowMillis=" + value("window.millis"))); return result;
    }
    private void buildAero(Path checkout) throws Exception { Path tree = root.resolve(".worldline/worktrees/m73-build-" + System.nanoTime());
        try { addWorktree(checkout, tree); Path stationapi = tree.resolve("stationapi"); String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
            String output = runGradle(stationapi, Arrays.asList(stationapi.resolve(wrapper).toString(), "--no-daemon", "remapJar"), Integer.parseInt(value("timeout.seconds")));
            Path jar = stationapi.resolve("build/libs/aero-model-lib-3.0.0.jar"); require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(jar), "Aero build failed");
            Files.copy(jar, build.resolve("aero-model-lib-3.0.0.jar"), StandardCopyOption.REPLACE_EXISTING); verifyWorktree(tree);
        } finally { removeWorktree(checkout, tree); } }
    private List<String> rows(String output, String trigger, String complete) { int start = output.indexOf(TRIGGER + trigger), end = output.indexOf(COMPLETE + complete);
        require(start >= 0 && end > start, "measurement bracket drift"); List<String> result = new ArrayList<>();
        for (String row : output.substring(start, end).lines().collect(Collectors.toList())) if (row.startsWith("[Aero_")) {
            String kind = kind(row); if (kind.equals("WorldFlush")) continue; require(Arrays.asList("FrameSpike", "GC", "Pulse").contains(kind), "unknown Aero row"); result.add(row); }
        return result; }
    private Row parse(String row) { Map<String, String> fields = fields(row); long frame = micros(fields, "frameMs"), atRest = whole(fields, "atRestRenders");
        require(whole(fields, "visibleChunks") > 0, "no visible chunks"); micros(fields, "compileChunksMs"); micros(fields, "gcTimeDeltaMs"); return new Row(frame, atRest); }
    private String kind(String row) { int end = row.indexOf(']'); require(row.startsWith("[Aero_") && end > 6, "bad Aero row"); return row.substring(6, end); }
    private Map<String, String> fields(String row) { Map<String, String> result = new HashMap<>(); for (String token : row.substring(row.indexOf(']') + 1).trim().split(" +")) {
        int equals = token.indexOf('='); if (equals > 0) require(result.put(token.substring(0, equals), token.substring(equals + 1)) == null, "duplicate Aero field"); } return result; }
    private long micros(Map<String, String> fields, String name) { try { BigDecimal value = new BigDecimal(required(fields, name)); require(value.signum() >= 0, "negative " + name);
        return value.movePointRight(3).setScale(0, RoundingMode.HALF_UP).longValueExact(); } catch (RuntimeException error) { throw new IllegalStateException("invalid " + name, error); } }
    private long whole(Map<String, String> fields, String name) { try { long value = Long.parseLong(required(fields, name)); require(value >= 0, "negative " + name); return value; }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid " + name, error); } }
    private Pair pair(Arm one, Arm two) { return one.mode.equals("absent") ? new Pair(one, two) : new Pair(two, one); }
    private Captured startServer(Path directory, List<String> command, int timeout) throws Exception { for (int attempt = 0; attempt < 2; attempt++) {
        Captured server = Captured.start(directory, command); try { server.awaitText("Done (", timeout); return server; }
        catch (Exception error) { server.kill(); server.process.waitFor(5, TimeUnit.SECONDS); if (attempt != 0 || !retryable(error)) throw error;
            System.out.println("M73 retrying one typed Windows Loom race"); Thread.sleep(3000L); } } throw new IllegalStateException("unreachable server retry"); }
    private String runGradle(Path directory, List<String> command, int timeout) throws Exception { for (int attempt = 0; attempt < 2; attempt++) try {
        return Captured.run(directory, command, timeout); } catch (Exception error) { if (attempt != 0 || !retryable(error)) throw error;
        System.out.println("M73 retrying one typed Windows Loom race"); Thread.sleep(3000L); } throw new IllegalStateException("unreachable client retry"); }
    private boolean retryable(Exception error) { String text = error.getMessage(); return text.contains("Could not delete")
            || text.contains("Failed to remap sources for") && text.contains("StationAPI-2.0.0-alpha.5.4-sources.jar"); }
    private void verifyBoundary() throws IOException { try (Stream<Path> paths = Files.walk(smoke.resolve("runtime-src/worldline/m73"))) {
        for (Path path : paths.filter(p -> p.toString().endsWith(".java")).filter(p -> !p.toString().contains(File.separator + "client" + File.separator))
                .filter(p -> !p.toString().contains(File.separator + "mixin" + File.separator)).collect(Collectors.toList())) { String text = Files.readString(path);
            require(!text.contains("aero.modellib") && !text.contains("net.minecraft.client") && !text.contains("org.lwjgl"), "server closure imports client code: " + path); } } }
    private void verifyCheckout(Path checkout) throws Exception { require(Files.isDirectory(checkout.resolve(".git")), "Aero checkout absent");
        require(git(checkout, "remote", "get-url", "origin").trim().equals(value("aero.repository")), "origin drift");
        require(git(checkout, "rev-parse", "HEAD").trim().equals(value("aero.revision")), "revision drift");
        require(git(checkout, "status", "--porcelain", "--untracked-files=all").trim().isEmpty(), "checkout dirty"); }
    private void addWorktree(Path checkout, Path target) throws Exception { Files.createDirectories(target.getParent()); require(!Files.exists(target), "worktree exists");
        git(checkout, "worktree", "add", "--detach", target.toString(), value("aero.revision")); verifyWorktree(target); }
    private void verifyWorktree(Path target) throws Exception { require(git(target, "rev-parse", "HEAD").trim().equals(value("aero.revision")), "worktree revision drift");
        require(git(target, "status", "--porcelain", "--untracked-files=all").trim().isEmpty(), "worktree dirty"); }
    private void removeWorktree(Path checkout, Path target) { try { if (registered(checkout, target)) try { git(checkout, "worktree", "remove", "--force", target.toString()); }
            catch (Exception error) { if (registered(checkout, target)) throw error; } if (Files.exists(target)) deleteRemainder(target);
        } catch (Exception error) { throw new IllegalStateException("M73 worktree cleanup failed " + target, error); } }
    private boolean registered(Path checkout, Path target) throws Exception { for (String row : git(checkout, "worktree", "list", "--porcelain").split("\\R"))
        if (row.startsWith("worktree ") && Paths.get(row.substring(9)).toAbsolutePath().normalize().equals(target.toAbsolutePath().normalize())) return true; return false; }
    private void deleteRemainder(Path target) throws Exception { Path allowed = root.resolve(".worldline/worktrees").normalize(), exact = target.toAbsolutePath().normalize();
        require(exact.startsWith(allowed) && !exact.equals(allowed), "unsafe remainder"); try (Stream<Path> paths = Files.walk(exact)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.deleteIfExists(path); } }
    private String git(Path directory, String... arguments) throws Exception { List<String> command = new ArrayList<>(); command.add("git"); command.add("-C"); command.add(directory.toString());
        command.addAll(Arrays.asList(arguments)); return Captured.run(root, command, 60); }
    private String unique(String text, String prefix) { List<String> found = text.lines().filter(row -> row.startsWith(prefix)).collect(Collectors.toList());
        require(found.size() == 1, "marker count drift " + prefix); return found.get(0).substring(prefix.length()); }
    private int markerNumber(String marker, String name) { for (String token : marker.split(" +")) if (token.startsWith(name + "="))
        return Integer.parseInt(token.substring(name.length() + 1)); throw new IllegalStateException("missing " + name + " in " + marker); }
    private long count(String text, String prefix) { return text.lines().filter(row -> row.startsWith(prefix)).count(); }
    private Set<String> cellSet(String text, String prefix) { Set<String> result = new HashSet<>(); for (String row : text.lines()
            .filter(value -> value.startsWith(prefix)).collect(Collectors.toList())) { String marker = row.substring(prefix.length());
        String key = markerNumber(marker, "x") + ":" + markerNumber(marker, "y") + ":" + markerNumber(marker, "z") + ":" + markerNumber(marker, "nonce");
        require(result.add(key), "duplicate cell marker " + key); } return result; }
    private Set<String> expectedCells(String mode, int nonce, int x, int baseY, int baseZ) { Set<String> result = new HashSet<>();
        if (mode.equals("present")) for (int dz = 0; dz < 4; dz++) for (int dy = 0; dy < 4; dy++)
            result.add(x + ":" + (baseY + dy) + ":" + (baseZ + dz) + ":" + (nonce * 100 + dz * 4 + dy + 1)); return result; }
    private boolean subsequence(List<String> expected, List<String> actual) { int at = 0; for (String row : actual) if (at < expected.size() && expected.get(at).equals(row)) at++; return at == expected.size(); }
    private int freePort() throws IOException { try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); } }
    private void recreate(Path target) throws IOException { if (Files.exists(target)) { require(target.startsWith(root.resolve(".worldline/smokes")), "unsafe build path");
        try (Stream<Path> paths = Files.walk(target)) { for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(path); } } Files.createDirectories(target); }
    private void load(Path path, Properties into) throws IOException { try (Reader reader = Files.newBufferedReader(path)) { into.load(reader); } }
    private String value(String key) { String result = config.getProperty(key); require(result != null && !result.trim().isEmpty(), "missing " + key); return result.trim(); }
    private String required(Map<String, String> fields, String name) { String value = fields.get(name); require(value != null, "missing " + name); return value; }
    private String diagnostic(String text) { return text.lines().filter(row -> row.contains("WorldlinePairContent") || row.contains("Loading ")
            || row.contains("aero-model-lib") || row.contains("Exception") || row.contains("BUILD ")).collect(Collectors.joining("\n")); }
    private static String sha256(String value) throws Exception { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8))); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static long median(List<Long> values) { List<Long> copy = new ArrayList<>(values); Collections.sort(copy); int at = copy.size() / 2;
        return copy.size() % 2 == 1 ? copy.get(at) : (copy.get(at - 1) + copy.get(at)) / 2; }
    private static long p95(List<Long> values) { List<Long> copy = new ArrayList<>(values); Collections.sort(copy); return copy.get((int) Math.ceil(copy.size() * .95) - 1); }
    private static final class Row { final long frame, atRest; Row(long frame, long atRest) { this.frame = frame; this.atRest = atRest; } }
    private static final class Plan { final int x, y, z; Plan(int x, int y, int z) { this.x = x; this.y = y; this.z = z; } }
    private static final class Arm { final String mode; final int nonce, expected, x, baseY, baseZ, raw; final List<Row> rows; final long median, p95, max, atRest;
        Arm(String mode, int nonce, List<Row> rows, int expected, int x, int baseY, int baseZ, int raw) { this.mode = mode; this.nonce = nonce; this.rows = rows; this.expected = expected;
            this.x = x; this.baseY = baseY; this.baseZ = baseZ; this.raw = raw;
            List<Long> frames = rows.stream().map(row -> row.frame).collect(Collectors.toList()); median = median(frames); p95 = p95(frames);
            max = Collections.max(frames); atRest = rows.stream().mapToLong(row -> row.atRest).sum(); }
        String summary() { return mode + ":nonce=" + nonce + ",rows=" + rows.size() + ",frameUs=" + median + "/" + p95 + "/" + max + ",atRest=" + atRest; } }
    private static final class Pair { final Arm absent, present; Pair(Arm absent, Arm present) { this.absent = absent; this.present = present;
            require(absent.x == present.x && absent.baseY == present.baseY && absent.baseZ == present.baseZ
                    && absent.raw == present.raw, "paired concrete plan drift"); }
        String summary() { return absent.summary() + " | " + present.summary() + " | descriptiveDeltaUs(median/p95/max)="
                + (present.median - absent.median) + "/" + (present.p95 - absent.p95) + "/" + (present.max - absent.max); } }
    private static final class Captured { final Process process; final StringBuilder text = new StringBuilder(); final Thread reader; int exitCode = -1;
        private Captured(Process process) { this.process = process; reader = new Thread(() -> { try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line; while ((line = input.readLine()) != null) synchronized (text) { text.append(line).append('\n'); text.notifyAll(); }
        } catch (IOException error) { synchronized (text) { text.append(error).append('\n'); } } }); reader.setDaemon(true); reader.start(); }
        static Captured start(Path directory, List<String> command) throws IOException { return new Captured(new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start()); }
        static String run(Path directory, List<String> command, int timeout) throws Exception { Captured value = start(directory, command); value.finish(timeout);
            require(value.exitCode == 0, command.get(0) + " failed\n" + value.output()); return value.output(); }
        void write(String value) throws IOException { process.getOutputStream().write(value.getBytes(StandardCharsets.UTF_8)); process.getOutputStream().flush(); }
        void awaitText(String value, int timeout) throws Exception { long deadline = System.currentTimeMillis() + timeout * 1000L; synchronized (text) {
            while (!text.toString().contains(value) && process.isAlive() && System.currentTimeMillis() < deadline) text.wait(100L); }
            require(output().contains(value), "missing " + value + "\n" + output()); }
        void finish(int timeout) throws Exception { if (!process.waitFor(timeout, TimeUnit.SECONDS)) { kill(); throw new IllegalStateException("process timeout\n" + output()); }
            exitCode = process.exitValue(); reader.join(5000L); require(exitCode == 0, "process exit " + exitCode + "\n" + output()); }
        void kill() { process.descendants().forEach(ProcessHandle::destroyForcibly); process.destroyForcibly(); }
        String output() { synchronized (text) { return text.toString(); } }
    }
}
