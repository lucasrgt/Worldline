import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Qualifies the seed atlas: deterministic official-server terrain pages. */
public final class AtlasCycle {
    private static final String ID = "seed-atlas";
    private static final String SEED = "12345";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/AtlasCycle.java " + ID); System.exit(2);
        }
        try { new AtlasCycle().execute(); }
        catch (Exception error) { error.printStackTrace(); System.exit(1); }
    }

    private void execute() throws Exception {
        recreate(build);
        compileServerAdapter();
        Result first = launcher("atlas", SEED, "1",
                build.resolve("first.html").toString());
        require(first.code == 0 && first.text.contains("WORLDLINE_ATLAS=PASS")
                && first.text.contains("seed=" + SEED) && first.text.contains("radius=1"),
                "first atlas render failed");
        Result second = launcher("atlas", SEED, "1",
                build.resolve("second.html").toString());
        require(second.code == 0 && Arrays.equals(Files.readAllBytes(
                build.resolve("first.html")), Files.readAllBytes(build.resolve("second.html"))),
                "atlas page is not byte-deterministic");
        Result other = launcher("atlas", "54321", "1",
                build.resolve("other.html").toString());
        require(other.code == 0 && !Arrays.equals(Files.readAllBytes(build.resolve("first.html")),
                Files.readAllBytes(build.resolve("other.html"))), "different seed produced identical page");
        String page = new String(Files.readAllBytes(build.resolve("first.html")),
                StandardCharsets.UTF_8);
        for (String marker : new String[] {"Worldline Seed Atlas", "seed=" + SEED,
                "&#9632;", "<table>", "side=48"}) {
            require(page.contains(marker), "page missing marker: " + marker);
        }
        require(!page.contains("<script"), "atlas page must not embed scripts");
        String report = "seed=" + SEED + "\nradius=1\nservers=3\ndeterministic=true"
                + "\nother-seed.differs=true\nscripts=none\npage.sha256="
                + sha256File(build.resolve("first.html")) + "\n";
        String signature = sha256(report);
        Properties expected = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(
                root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
            expected.load(reader);
        }
        require(signature.equals(expected.getProperty("expected.signature")),
                "seed atlas evidence diverged: " + signature);
        Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
        System.out.println("Seed atlas cycle passed");
        System.out.println("  48x48 columns from official server; byte-deterministic");
        System.out.println("  page.sha256=" + sha256File(build.resolve("first.html")));
        System.out.println("  evidence SHA-256: " + signature);
    }

    /** Compiles the server process adapter used by the atlas command. */
    private void compileServerAdapter() throws Exception {
        Path output = root.resolve(".worldline/build/server-adapter");
        recreate(output);
        List<String> command = new java.util.ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8", "--release", "8",
                "-Xlint:all,-options", "-Werror", "-classpath", product("api") + System.getProperty("path.separator")
                + product("analysis"), "-d", output.toString()));
        List<String> sources = new java.util.ArrayList<>();
        try (java.util.stream.Stream<Path> paths = Files
                .walk(root.resolve("adapters/b173-server/src/main/java"))) {
            paths.filter(path -> path.toString().endsWith(".java")).sorted()
                    .forEach(path -> sources.add(path.toString()));
        }
        try (java.util.stream.Stream<Path> paths = Files
                .walk(root.resolve("adapters/b173-server/src/atlas/java"))) {
            paths.filter(path -> path.toString().endsWith(".java")).sorted()
                    .forEach(path -> sources.add(path.toString()));
        }
        command.addAll(sources);
        capture(command.toArray(new String[0]));
    }

    private Result launcher(String... arguments) throws Exception {
        List<String> command = new java.util.ArrayList<>(
                Arrays.asList("java", "tools/replay/Replay.java"));
        command.addAll(Arrays.asList(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new Result(process.waitFor(), output.replace('\r', '\n'));
    }

    private String lineOf(String text, String prefix) {
        for (String row : text.split("\n", -1)) {
            if (row.startsWith(prefix)) return row.substring(prefix.length());
        }
        throw new IllegalStateException("missing " + prefix);
    }

    private void recreate(Path target) throws IOException {
        if (Files.exists(target)) {
            try (java.util.stream.Stream<Path> paths = Files.walk(target)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder())
                        .collect(java.util.stream.Collectors.toList())) Files.delete(path);
            }
        }
        Files.createDirectories(target);
    }

    private void capture(String... command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException("javac failed\n" + output);
    }

    private Path product(String name) {
        return root.resolve(".worldline/build/classes").resolve(name);
    }

    private String sha256File(Path path) throws Exception {
        return sha256(Files.readAllBytes(path));
    }

    private String sha256(String text) throws Exception { return sha256(text.getBytes(StandardCharsets.UTF_8)); }
    private String sha256(byte[] data) throws Exception { MessageDigest d = MessageDigest.getInstance("SHA-256");
        StringBuilder r = new StringBuilder(); for (byte b : d.digest(data)) r.append(String.format("%02x", b)); return r.toString(); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }
    private static final class Result { final int code; final String text;
        Result(int code, String text) { this.code = code; this.text = text; } }
}
