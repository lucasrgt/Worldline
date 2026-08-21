import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Qualifies self-contained HTML evidence through the public launcher: a
 * single-trace viewer and a two-trace diff rendered from real controlled
 * runs, byte-deterministic across processes, frozen by document SHA-256.
 */
public final class HtmlCycle {
    private static final String ID = "m19-html";
    private static final String SEED = "4242";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/HtmlCycle.java " + ID); System.exit(2);
        }
        try { new HtmlCycle().execute(); }
        catch (Exception error) { System.err.println("M19 html cycle failed: " + error.getMessage()); System.exit(1); }
    }

    private void execute() throws Exception {
        recreate();
        Path glass = build.resolve("glass.wlscenario"), gold = build.resolve("gold.wlscenario");
        require(launcher("scenario", "create", glass.toString(), "block:8,65,8:20",
                "tick:2", "observe:after").code == 0, "glass scenario failed");
        require(launcher("scenario", "create", gold.toString(), "block:8,65,8:41",
                "tick:2", "observe:after").code == 0, "gold scenario failed");
        Path left = build.resolve("left.wltrace"), right = build.resolve("right.wltrace");
        require(launcher("scenario", "run", glass.toString(), SEED, left.toString()).code == 0,
                "glass execution failed");
        require(launcher("scenario", "run", gold.toString(), SEED, right.toString()).code == 0,
                "gold execution failed");
        Result single = launcher("trace", "html", left.toString(),
                build.resolve("single.html").toString());
        require(single.code == 0 && single.text.contains("WORLDLINE_TRACE_HTML=PASS")
                && single.text.contains("mode=trace"), "single html render failed");
        Path diffPage = build.resolve("diff.html");
        Result diff = launcher("trace", "html", left.toString(), right.toString(),
                diffPage.toString());
        require(diff.code == 0 && diff.text.contains("mode=diff")
                && !lineOf(diff.text, "left.sha256=").equals(lineOf(diff.text, "right.sha256=")),
                "diff html render failed");
        String singleHtml = new String(Files.readAllBytes(build.resolve("single.html")),
                StandardCharsets.UTF_8);
        String diffHtml = new String(Files.readAllBytes(diffPage), StandardCharsets.UTF_8);
        for (String marker : new String[] {"TRACE mode", "Worldline Evidence",
                leftSignature(single)}) {
            require(singleHtml.contains(marker), "single page missing " + marker);
        }
        for (String marker : new String[] {"DIFF mode", "RESULT: DIVERGED at record 0",
                "class=\"first div\"", ">20<", ">41<"}) {
            require(diffHtml.contains(marker), "diff page missing " + marker);
        }
        require(!singleHtml.contains("<script") && !diffHtml.contains("<script"),
                "html evidence must not embed scripts");
        require(Arrays.equals(Files.readAllBytes(build.resolve("single.html")),
                renderAgain(left)), "single html is not byte-deterministic");
        String report = "single.sha256=" + sha256File(build.resolve("single.html"))
                + "\ndiff.sha256=" + sha256File(diffPage) + "\nscripts=none\n";
        String signature = sha256(report);
        Properties expected = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(
                root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
            expected.load(reader);
        }
        require(signature.equals(expected.getProperty("expected.signature")),
                "M19 html evidence diverged: " + signature);
        Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
        System.out.println("M19 html cycle passed");
        System.out.println("  viewer + diff pages rendered from real controlled runs");
        System.out.println("  single.sha256=" + sha256File(build.resolve("single.html")));
        System.out.println("  diff.sha256=" + sha256File(diffPage));
        System.out.println("  evidence SHA-256: " + signature);
    }

    private String leftSignature(Result single) { return lineOf(single.text, "left.sha256="); }

    /** Rerenders one page in a fresh process to prove byte determinism. */
    private byte[] renderAgain(Path trace) throws Exception {
        Path copy = build.resolve("again.html");
        Files.deleteIfExists(copy);
        require(launcher("trace", "html", trace.toString(), copy.toString()).code == 0,
                "second render failed");
        return Files.readAllBytes(copy);
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

    private void recreate() throws Exception {
        if (Files.exists(build)) {
            try (java.util.stream.Stream<Path> paths = Files.walk(build)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder())
                        .collect(java.util.stream.Collectors.toList())) Files.delete(path);
            }
        }
        Files.createDirectories(build);
    }

    private String sha256File(Path path) throws Exception {
        return sha256(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
    }

    private String sha256(String text) throws Exception { byte[] hash = MessageDigest.getInstance("SHA-256")
            .digest(text.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
            for (byte value : hash) result.append(String.format("%02x", value & 255)); return result.toString(); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }
    private static final class Result { final int code; final String text;
        Result(int code, String text) { this.code = code; this.text = text; } }
}
