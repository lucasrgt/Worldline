import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;

/** Runs two balanced complete in-memory frame-census pairs. */
public final class AeroFrameCensusCycle {
    private static final String ID = "m74-complete-aero-census";
    private final Path root = Paths.get("").toAbsolutePath().normalize(), smoke = root.resolve("smokes").resolve(ID), build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties();
    public static void main(String[] args) { if (!Arrays.equals(args, new String[]{ID})) { System.err.println("usage: java tools/smoke/AeroFrameCensusCycle.java " + ID); System.exit(2); }
        try { new AeroFrameCensusCycle().execute(); } catch (Exception error) { System.err.println("Aero frame census failed: " + error.getMessage()); System.exit(1); } }
    private void execute() throws Exception {
        load(smoke.resolve("smoke.properties"), config); require(ID.equals(value("id")) && value("pairs").equals("2") && value("warmup.frames").equals("300")
                && value("warmup.millis").equals("5000") && value("minimum.frames").equals("720") && value("minimum.millis").equals("12000"), "M74 design drift");
        Path checkout = root.resolve(value("aero.path")).normalize(); verifyCheckout(checkout); verifyBoundary(); recreate(build); buildAero(checkout);
        String[] order = {"present", "absent", "absent", "present"}; List<Arm> arms = new ArrayList<>(); Plan[] plans = new Plan[2];
        int start = Integer.getInteger("worldline.m74.armStart", 0), limit = Integer.getInteger("worldline.m74.armLimit", order.length - start);
        require(start >= 0 && limit > 0 && start + limit <= order.length, "arm range drift"); boolean diagnostic = start != 0 || limit != order.length;
        require(!diagnostic || Boolean.getBoolean("worldline.m74.diagnostic"), "partial M74 requires diagnostic opt-in");
        for (int index = start; index < start + limit; index++) { int pair = index / 2, nonce = 7407401 + pair; verifyCheckout(checkout);
            Arm arm = run(checkout, build.resolve("pair-" + (pair + 1) + "-" + order[index]), order[index], pair, nonce, plans[pair]); arms.add(arm);
            if (plans[pair] == null) plans[pair] = new Plan(arm.x, arm.y, arm.z); verifyCheckout(checkout); }
        if (diagnostic) { System.out.println("M74 diagnostic arm passed; qualification not attempted arms=" + limit); arms.forEach(arm -> System.out.println("  " + arm.summary())); return; }
        Pair first = pair(arms.get(0), arms.get(1)), second = pair(arms.get(2), arms.get(3));
        String trace = "v1|design=2-balanced-pairs-16/0+0/16|fixture=exact-plan+tracked-camera+explicit16"
                + "|census=every-complete-head-to-head-interval-after-fixture-ready|capture=fixed65536-primitive-memory+prior-tail-counters"
                + "|window=min720intervals+12s|flush=single-binary-after-seal|fields=intervalNs+atRest+listCalls+visibleChunks+contentCalls+received+applied+rendered"
                + "|aero-log=disabled+explicit-mesh-counter-reset|stats=descriptive-whole-census+paired-dynamic-deltas|regression-causality-density-historical-lag=not-claimed|shutdown=clean";
        String signature = sha256(trace); require(signature.equals(value("expected.signature")), "M74 signature drift: " + signature);
        String evidence = "id=" + ID + "\npairs=2\narms=4\nserver.jvm=4\nclient.jvm=4\nfirst=" + first.summary() + "\nsecond=" + second.summary()
                + "\ntrace=" + trace + "\nsignature=" + signature + "\n"; Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println("M74 complete Aero census passed"); System.out.println("  pair 1: " + first.summary()); System.out.println("  pair 2: " + second.summary());
        System.out.println("  signature: " + signature);
    }
    private Arm run(Path checkout, Path workspace, String mode, int pair, int nonce, Plan plan) throws Exception {
        Files.createDirectories(workspace); int port = freePort(), timeout = Integer.parseInt(value("timeout.seconds"));
        Path base = root.resolve(".worldline/worktrees/m74-" + ProcessHandle.current().pid() + "-" + pair + "-" + mode + "-" + System.nanoTime());
        Path serverTree = base.resolve("server"), clientTree = base.resolve("client"); Captured server = null;
        try { addWorktree(checkout, serverTree); addWorktree(checkout, clientTree); Path init = root.resolve(value("runner"));
            Path serverGame = workspace.resolve("server"), clientGame = workspace.resolve("client"), artifact = clientGame.resolve("census.bin");
            server = startServer(serverTree.resolve("stationapi/test-bare"), command(serverTree, init, "server", serverGame, port, nonce, mode, null, plan), timeout);
            String client = runGradle(clientTree.resolve("stationapi/test-bare"), command(clientTree, init, "client", clientGame, port, nonce, mode, artifact, null), timeout);
            Files.writeString(workspace.resolve("client-output.txt"), client);
            require(client.contains("Loading 46 mods:") && client.contains("- aero-model-lib 3.0.0") && client.contains("- worldline-m74-content 1.0.0")
                    && client.contains("BUILD SUCCESSFUL") && client.contains("[WorldlineCensus] packet1") && client.contains("[WorldlineCensus] packet13"), "client boundary drift\n" + diagnostic(client));
            require(count(client, "[WorldlineCensus] trigger ") == 1 && unique(client, "[WorldlineCensus] trigger ").contains("logger=false") && count(client, "[WorldlineCensus] plan-ready") == 1
                    && count(client, "[WorldlineCensus] census-start ") == 1 && count(client, "[WorldlineCensus] complete ") == 1 && !client.contains("[Aero_"), "client lifecycle drift");
            Census census = parseArtifact(artifact, mode, nonce); String complete = unique(client, "[WorldlineCensus] complete ");
            require(marker(complete, "samples") == census.count && markerLong(complete, "elapsedNs") == census.elapsed && marker(complete, "mask") == census.mask, "completion/artifact drift");
            server.write("save-all\nstop\n"); server.finish(45); String serverText = server.output(); Files.writeString(workspace.resolve("server-output.txt"), serverText); server = null;
            require(serverText.contains("Loading 39 mods:") || serverText.contains("Loading 40 mods:"), "server loader marker absent");
            require(serverText.contains("- worldline-m74-content 1.0.0") && !serverText.contains("- aero-model-lib ") && serverText.contains("BUILD SUCCESSFUL"), "server boundary drift\n" + diagnostic(serverText));
            String scene = unique(serverText, "[WorldlineCensus] scene "); int expected = mode.equals("present") ? 16 : 0;
            require(token(scene, "mode").equals(mode) && marker(scene, "planned") == 16 && marker(scene, "placed") == expected
                    && marker(scene, "yaw") == -90 && marker(scene, "pitch") == 0 && marker(scene, "nonce") == nonce, "scene drift: " + scene);
            int x = marker(scene, "x"), y = marker(scene, "baseY"), z = marker(scene, "baseZ"), raw = marker(scene, "raw");
            require((plan == null || plan.x == x && plan.y == y && plan.z == z) && census.x == x && census.y == y && census.z == z, "paired plan drift");
            require(serverText.indexOf("[WorldlineCensus] activation ") < serverText.indexOf("[WorldlineCensus] tracking-ready ")
                    && serverText.indexOf("[WorldlineCensus] tracking-ready ") < serverText.indexOf("[WorldlineCensus] scene ")
                    && serverText.contains(value("username") + " lost connection") && serverText.contains("Stopping server"), "server lifecycle drift");
            verifyWorktree(serverTree); verifyWorktree(clientTree); return new Arm(mode, nonce, x, y, z, raw, census, sha256(Files.readAllBytes(artifact)));
        } finally { if (server != null) { try { server.write("stop\n"); server.finish(20); } catch (Exception error) { server.kill(); }
                finally { Files.writeString(workspace.resolve("server-output.txt"), server.output()); } }
            removeWorktree(checkout, clientTree); removeWorktree(checkout, serverTree); }
    }
    private Census parseArtifact(Path file, String mode, int nonce) throws Exception {
        byte[] bytes = Files.readAllBytes(file); try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            require(in.readInt() == 0x574c3734 && in.readInt() == 1 && in.readInt() == 28, "artifact schema drift"); int density = in.readInt();
            require(density == (mode.equals("present") ? 16 : 0) && in.readInt() == nonce, "artifact arm drift"); int x = in.readInt(), y = in.readInt(), z = in.readInt();
            require(in.readInt() == Integer.parseInt(value("minimum.frames")) && in.readLong() == Long.parseLong(value("minimum.millis")) * 1_000_000L, "artifact window drift");
            int count = in.readInt(); long elapsed = in.readLong(); require(count >= 720 && count <= 65536 && bytes.length == 56L + count * 28L, "artifact length drift");
            long sum = 0, renderSum = 0, listSum = 0, callSum = 0; int visible = 0, mask = density == 16 ? 0xffff : 0; long[] frames = new long[count];
            for (int i = 0; i < count; i++) { long delta = in.readLong(); int renders = in.readInt(), lists = in.readInt(), chunks = in.readInt(), calls = in.readInt();
                int state = in.readUnsignedShort(), seen = in.readUnsignedShort(); require(delta > 0 && renders >= 0 && lists >= 0 && chunks >= 0 && calls >= 0, "invalid census record");
                require(state == (density == 16 ? 0x1010 : 0) && seen == mask, "fixture state drift at " + i);
                if (density == 0) require(renders == 0 && lists == 0 && calls == 0, "absent work drift"); frames[i] = delta; sum = Math.addExact(sum, delta);
                renderSum += renders; listSum += lists; callSum += calls; visible = Math.max(visible, chunks); }
            require(in.read() == -1 && sum == elapsed && elapsed >= 12_000_000_000L && visible > 0, "artifact aggregate drift");
            if (density == 16) require(renderSum > 0 && listSum > 0 && callSum > 0, "present work absent"); return new Census(x, y, z, count, elapsed, mask, frames, renderSum, callSum);
        }
    }
    private List<String> command(Path tree, Path init, String role, Path game, int port, int nonce, String mode, Path artifact, Plan plan) {
        String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        List<String> result = new ArrayList<>(Arrays.asList(tree.resolve("stationapi/test-bare").resolve(wrapper).toString(), "--no-daemon", "--init-script", init.toString(),
                role.equals("server") ? "runServer" : "runClient", "-PworldlineRole=" + role, "-PworldlineRunDir=" + game, "-PworldlinePort=" + port,
                "-PworldlineMode=" + mode, "-PworldlineNonce=" + nonce)); if (plan != null) result.addAll(Arrays.asList("-PworldlinePlanX=" + plan.x, "-PworldlinePlanY=" + plan.y, "-PworldlinePlanZ=" + plan.z));
        if (artifact != null) result.addAll(Arrays.asList("-PworldlineArtifact=" + artifact, "-PworldlineUsername=" + value("username"), "-PworldlineWarmupFrames=" + value("warmup.frames"),
                "-PworldlineWarmupMillis=" + value("warmup.millis"), "-PworldlineMinimumFrames=" + value("minimum.frames"), "-PworldlineMinimumMillis=" + value("minimum.millis"))); return result;
    }
    private void buildAero(Path checkout) throws Exception { Path tree = root.resolve(".worldline/worktrees/m74-build-" + System.nanoTime()); try { addWorktree(checkout, tree);
        Path stationapi = tree.resolve("stationapi"); String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        String output = runGradle(stationapi, Arrays.asList(stationapi.resolve(wrapper).toString(), "--no-daemon", "remapJar"), Integer.parseInt(value("timeout.seconds")));
        Path jar = stationapi.resolve("build/libs/aero-model-lib-3.0.0.jar"); require(output.contains("BUILD SUCCESSFUL") && Files.isRegularFile(jar), "Aero build failed");
        Files.copy(jar, build.resolve("aero-model-lib-3.0.0.jar"), StandardCopyOption.REPLACE_EXISTING); verifyWorktree(tree); } finally { removeWorktree(checkout, tree); } }
    private Captured startServer(Path dir, List<String> command, int timeout) throws Exception { Captured server = Captured.start(dir, command); server.awaitText("Done (", timeout); return server; }
    private String runGradle(Path dir, List<String> command, int timeout) throws Exception { return Captured.run(dir, command, timeout); }
    private void verifyBoundary() throws IOException { try (Stream<Path> paths = Files.walk(smoke.resolve("runtime-src/worldline/m74"))) { for (Path path : paths.filter(p -> p.toString().endsWith(".java"))
        .filter(p -> !p.toString().contains(File.separator + "client" + File.separator)).filter(p -> !p.toString().contains(File.separator + "mixin" + File.separator)).collect(Collectors.toList())) {
        String text = Files.readString(path); require(!text.contains("aero.modellib") && !text.contains("net.minecraft.client") && !text.contains("org.lwjgl"), "server closure imports client code"); } } }
    private void verifyCheckout(Path checkout) throws Exception { require(git(checkout, "remote", "get-url", "origin").trim().equals(value("aero.repository"))
        && git(checkout, "rev-parse", "HEAD").trim().equals(value("aero.revision")) && git(checkout, "status", "--porcelain", "--untracked-files=all").trim().isEmpty(), "Aero checkout drift"); }
    private void addWorktree(Path checkout, Path target) throws Exception { Files.createDirectories(target.getParent()); git(checkout, "worktree", "add", "--detach", target.toString(), value("aero.revision")); verifyWorktree(target); }
    private void verifyWorktree(Path target) throws Exception { require(git(target, "rev-parse", "HEAD").trim().equals(value("aero.revision"))
            && git(target, "status", "--porcelain", "--untracked-files=all").trim().isEmpty(), "worktree drift"); }
    private void removeWorktree(Path checkout, Path target) { try { if (registered(checkout, target)) try { git(checkout, "worktree", "remove", "--force", target.toString()); }
        catch (Exception error) { if (registered(checkout, target)) throw error; }
        if (Files.exists(target)) { Path allowed = root.resolve(".worldline/worktrees").normalize(), exact = target.toAbsolutePath().normalize(); require(exact.startsWith(allowed) && !exact.equals(allowed), "unsafe remainder");
            try (Stream<Path> paths = Files.walk(exact)) { for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.deleteIfExists(path); } }
        Path parent = target.toAbsolutePath().normalize().getParent(), allowedParent = root.resolve(".worldline/worktrees").normalize();
        if (parent != null && parent.startsWith(allowedParent) && !parent.equals(allowedParent) && Files.isDirectory(parent)) { boolean empty;
            try (Stream<Path> paths = Files.list(parent)) { empty = paths.findAny().isEmpty(); } if (empty) Files.deleteIfExists(parent); } }
        catch (Exception error) { throw new IllegalStateException("M74 cleanup failed " + target, error); } }
    private boolean registered(Path checkout, Path target) throws Exception { return Arrays.stream(git(checkout, "worktree", "list", "--porcelain").split("\\R"))
            .anyMatch(row -> row.startsWith("worktree ") && Paths.get(row.substring(9)).toAbsolutePath().normalize().equals(target.toAbsolutePath().normalize())); }
    private String git(Path dir, String... args) throws Exception { List<String> command = new ArrayList<>(); command.add("git"); command.add("-C"); command.add(dir.toString()); command.addAll(Arrays.asList(args)); return Captured.run(root, command, 60); }
    private Pair pair(Arm one, Arm two) { return one.mode.equals("absent") ? new Pair(one, two) : new Pair(two, one); }
    private String unique(String text, String prefix) { List<String> rows = text.lines().filter(row -> row.startsWith(prefix)).collect(Collectors.toList()); require(rows.size() == 1, "marker drift " + prefix); return rows.get(0).substring(prefix.length()); }
    private int marker(String row, String name) { return Integer.parseInt(token(row, name)); } private long markerLong(String row, String name) { return Long.parseLong(token(row, name)); }
    private String token(String row, String name) { for (String token : row.split(" +")) if (token.startsWith(name + "=")) return token.substring(name.length() + 1); throw new IllegalStateException("missing " + name); }
    private long count(String text, String prefix) { return text.lines().filter(row -> row.startsWith(prefix)).count(); }
    private int freePort() throws IOException { try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); } }
    private void recreate(Path target) throws IOException { if (Files.exists(target)) try (Stream<Path> paths = Files.walk(target)) { for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(path); } Files.createDirectories(target); }
    private void load(Path path, Properties into) throws IOException { try (Reader reader = Files.newBufferedReader(path)) { into.load(reader); } }
    private String value(String key) { String result = config.getProperty(key); require(result != null && !result.trim().isEmpty(), "missing " + key); return result.trim(); }
    private String diagnostic(String text) { return text.lines().filter(row -> row.contains("WorldlineCensus") || row.contains("Loading ") || row.contains("Exception") || row.contains("BUILD ")).collect(Collectors.joining("\n")); }
    private static String sha256(String value) throws Exception { return sha256(value.getBytes(StandardCharsets.UTF_8)); }
    private static String sha256(byte[] value) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value)); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }
    private static long quantile(long[] values, double q) { long[] copy = values.clone(); Arrays.sort(copy); return copy[(int) Math.ceil(copy.length * q) - 1]; }
    private static final class Plan { final int x, y, z; Plan(int x, int y, int z) { this.x = x; this.y = y; this.z = z; } }
    private static final class Census { final int x, y, z, count, mask; final long elapsed, median, p95, p99, max, renderSum, callSum; Census(int x, int y, int z, int count, long elapsed,
        int mask, long[] frames, long renderSum, long callSum) { this.x=x;this.y=y;this.z=z;this.count=count;this.elapsed=elapsed;this.mask=mask;this.renderSum=renderSum;this.callSum=callSum;
        median=quantile(frames,.5);p95=quantile(frames,.95);p99=quantile(frames,.99);max=Arrays.stream(frames).max().orElseThrow(); } }
    private static final class Arm { final String mode, hash; final int nonce,x,y,z,raw; final Census c; Arm(String mode,int nonce,int x,int y,int z,int raw,Census c,String hash){this.mode=mode;this.nonce=nonce;this.x=x;this.y=y;this.z=z;this.raw=raw;this.c=c;this.hash=hash;}
        String summary(){return mode+":nonce="+nonce+",samples="+c.count+",intervalNs="+c.median+"/"+c.p95+"/"+c.p99+"/"+c.max+",renders="+c.renderSum+",artifact="+hash.substring(0,12);} }
    private static final class Pair { final Arm absent,present; Pair(Arm a,Arm p){absent=a;present=p;require(a.x==p.x&&a.y==p.y&&a.z==p.z&&a.raw==p.raw,"pair plan drift");}
        String summary(){return absent.summary()+" | "+present.summary()+" | descriptiveDeltaNs(median/p95/p99/max)="+(present.c.median-absent.c.median)+"/"+(present.c.p95-absent.c.p95)+"/"+(present.c.p99-absent.c.p99)+"/"+(present.c.max-absent.c.max);} }
    private static final class Captured { final Process process; final StringBuilder text=new StringBuilder(); final Thread reader; int exit=-1;
        private Captured(Process p){process=p;reader=new Thread(()->{try(BufferedReader in=new BufferedReader(new InputStreamReader(p.getInputStream(),StandardCharsets.UTF_8))){String line;while((line=in.readLine())!=null)synchronized(text){text.append(line).append('\n');text.notifyAll();}}catch(IOException e){}});reader.setDaemon(true);reader.start();}
        static Captured start(Path d,List<String> c)throws IOException{return new Captured(new ProcessBuilder(c).directory(d.toFile()).redirectErrorStream(true).start());}
        static String run(Path d,List<String> c,int t)throws Exception{Captured v=start(d,c);v.finish(t);require(v.exit==0,"process failed\n"+v.output());return v.output();}
        void write(String v)throws IOException{process.getOutputStream().write(v.getBytes(StandardCharsets.UTF_8));process.getOutputStream().flush();}
        void awaitText(String v,int t)throws Exception{long end=System.currentTimeMillis()+t*1000L;synchronized(text){while(!text.toString().contains(v)&&process.isAlive()&&System.currentTimeMillis()<end)text.wait(100);}require(output().contains(v),"missing "+v+"\n"+output());}
        void finish(int t)throws Exception{if(!process.waitFor(t,TimeUnit.SECONDS)){kill();throw new IllegalStateException("process timeout\n"+output());}exit=process.exitValue();reader.join(5000);require(exit==0,"process exit "+exit+"\n"+output());}
        void kill(){process.descendants().forEach(ProcessHandle::destroyForcibly);process.destroyForcibly();}String output(){synchronized(text){return text.toString();}}
    }
}
