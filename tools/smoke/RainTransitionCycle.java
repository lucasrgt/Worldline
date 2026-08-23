import java.io.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.*;

/** Repeats one official dry-to-rain world transition through Packet70Bed reason 1 in two fresh JVMs. */
public final class RainTransitionCycle {
    private static final String ID = "m500-sw-rain-transition";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path smoke = root.resolve("smokes").resolve(ID);
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Properties config = new Properties();
    private final Properties artifact = new Properties();

    public static void main(String[] a) {
        if (!Arrays.equals(a, new String[]{ID})) {
            System.err.println("usage: java tools/smoke/RainTransitionCycle.java " + ID);
            System.exit(2);
        }
        try { new RainTransitionCycle().execute(); }
        catch (Exception e) { System.err.println("rain transition failed: " + e.getMessage()); System.exit(1); }
    }

    private void execute() throws Exception {
        load(smoke.resolve("smoke.properties"), config);
        load(root.resolve("artifacts/minecraft-b1.7.3-server.properties"), artifact);
        require(ID.equals(value(config, "id")) && value(config, "server.jar.sha256").equals(value(artifact, "expected.sha256"))
                && value(config, "username").length() <= 16, "descriptor drift");
        require(value(config, "expected.signal").contains("live=packet70-reason1")
                && value(config, "expected.signal").contains("old-snapshot=raining")
                && value(config, "expected.signal").contains("canonical=dry-original-countdown")
                && value(config, "expected.signal").contains("save-order=overworld-then-secondary"),
                "signal must name the live rain start and the dual-snapshot save-order oracle");
        Path official = root.resolve(value(artifact, "local.path")).normalize();
        verifyArtifact(official);
        recreate(build);
        Path classes = compile();
        Outcome first = run(classes, official, build.resolve("first"));
        Outcome second = run(classes, official, build.resolve("second"));
        require(first.signal.equals(second.signal) && first.trace.equals(second.trace)
                && first.signature.equals(second.signature), "fresh rain transitions diverged");
        require(first.signal.contains("live=packet70-reason1")
                && first.signal.contains("old-snapshot=raining")
                && first.signal.contains("canonical=dry-original-countdown")
                && first.signal.contains("save-order=overworld-then-secondary")
                && first.signal.contains("identity=seed-spawn-preserved")
                && first.trace.contains("packet70-reason1-begin-rain")
                && first.trace.contains("dry-before-raining-after")
                && !first.trace.contains("packet71") && !first.trace.contains("thundering=true"),
                "rain transition collapsed to bootstrap, thunder, or lightning");
        String expected = value(config, "expected.signature");
        if (expected.equals("pending") || Boolean.getBoolean("worldline.m500sw.diagnostic")) {
            System.out.println("FROZEN");
            System.out.println("  " + first.signal);
            System.out.println("  trace: " + first.trace);
            System.out.println("  signature: " + first.signature);
            return;
        }
        require(first.signal.equals(value(config, "expected.signal")) && first.signature.equals(expected),
                "M500-SW frozen evidence drift");
        String evidence = "id=" + ID + "\nsmoke.jvm=2\nserver.jvm=4\nclient.sessions=2\nfirst="
                + first.signal + "\nsecond=" + second.signal + "\ntrace=" + first.trace
                + "\nsignature=" + first.signature + "\n";
        Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
        System.out.println("M500-SW rain transition passed");
        System.out.println("  processes: 2 smoke JVMs + 4 official server JVMs; 2 client sessions");
        System.out.println("  signal: " + first.signal);
        System.out.println("  signature: " + first.signature);
        System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
    }

    private Outcome run(Path classes, Path official, Path workspace) throws Exception {
        Exception last = null;
        for (int i = 0; i < 2; i++) {
            try {
                if (Files.exists(workspace)) recreate(workspace);
                String output = capture(root, Arrays.asList("java", "-classpath", classes + separator() + product("api"),
                        "worldline.smoke.raintransitionb173.RainTransitionSmoke",
                        official.toString(), workspace.toString(), Integer.toString(freePort()),
                        value(config, "seed"), value(config, "username"), value(config, "rain.ticks")));
                return new Outcome(line(output, "WORLDLINE_M500SW_TRACE="),
                        line(output, "WORLDLINE_M500SW_SIGNATURE="),
                        line(output, "WORLDLINE_M500SW_RAIN="));
            } catch (Exception e) { last = e; SmokeRetryBoundary.afterEofFailure(i,1,e); throw e; }
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
        capture(root, command);
        return output;
    }

    private void verifyArtifact(Path p) throws Exception {
        require(Files.isRegularFile(p), "server artifact absent");
        require(Files.size(p) == Long.parseLong(value(artifact, "expected.bytes"))
                && digest(p, "SHA-1").equals(value(artifact, "expected.sha1"))
                && digest(p, "SHA-256").equals(value(artifact, "expected.sha256")), "server artifact drift");
    }

    private List<String> javaFiles(Path s) throws IOException {
        try (Stream<Path> p = Files.walk(s)) {
            return p.filter(x -> x.toString().endsWith(".java")).sorted().map(Path::toString).collect(Collectors.toList());
        }
    }

    private String capture(Path d, List<String> c) throws Exception {
        Process p = new ProcessBuilder(c).directory(d.toFile()).redirectErrorStream(true).start();
        String o = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (p.waitFor() != 0) throw new IllegalStateException(c.get(0) + " failed\n" + o);
        return o;
    }

    private void recreate(Path t) throws IOException {
        if (Files.exists(t)) {
            require(t.startsWith(root.resolve(".worldline")) && !t.equals(root), "unsafe build path");
            try (Stream<Path> p = Files.walk(t)) {
                for (Path f : p.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(f);
            }
        }
        Files.createDirectories(t);
    }

    private void load(Path p, Properties t) throws IOException {
        try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) { t.load(r); }
    }

    private String value(Properties p, String k) {
        String v = p.getProperty(k);
        require(v != null && !v.trim().isEmpty(), "missing " + k);
        return v.trim();
    }

    private String digest(Path p, String a) throws Exception {
        MessageDigest d = MessageDigest.getInstance(a);
        try (InputStream in = Files.newInputStream(p)) {
            byte[] b = new byte[8192];
            int n;
            while ((n = in.read(b)) >= 0) d.update(b, 0, n);
        }
        return HexFormat.of().formatHex(d.digest());
    }

    private int freePort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) { return s.getLocalPort(); }
    }

    private Path product(String n) { return root.resolve(".worldline/build/classes").resolve(n); }
    private String separator() { return System.getProperty("path.separator"); }

    private String line(String o, String p) {
        return o.lines().filter(x -> x.startsWith(p)).findFirst()
                .orElseThrow(() -> new IllegalStateException("missing " + p + "\n" + o)).substring(p.length());
    }



    private static void require(boolean v, String m) { if (!v) throw new IllegalStateException(m); }

    private static final class Outcome {
        final String trace, signature, signal;
        Outcome(String t, String s, String l) { trace = t; signature = s; signal = l; }
    }
}
