import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Qualifies the registry census: deterministic canonical dumps per section. */
public final class CensusCycle {
    private static final String ID = "census-cycle";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/CensusCycle.java " + ID); System.exit(2);
        }
        try { new CensusCycle().execute(); }
        catch (Exception error) { System.err.println("census cycle failed: " + error.getMessage()); System.exit(1); }
    }

    private void execute() throws Exception {
        recreate();
        Result first = launcher("census", build.resolve("first").toString());
        require(first.code == 0 && first.text.contains("WORLDLINE_CENSUS=PASS"),
                "first census capture failed");
        Result second = launcher("census", build.resolve("second").toString());
        require(second.code == 0 && second.text.contains("WORLDLINE_CENSUS=PASS"),
                "second census capture failed");
        int blocks = -1, items = -1, recipes = -1, smelts = -1;
        for (String section : Arrays.asList("blocks", "items", "recipes", "smelts")) {
            Path a = build.resolve("first").resolve(section + ".wlcensus");
            Path b = build.resolve("second").resolve(section + ".wlcensus");
            require(Arrays.equals(Files.readAllBytes(a), Files.readAllBytes(b)),
                    section + " census is not deterministic");
            String text = new String(Files.readAllBytes(a), StandardCharsets.UTF_8);
            require(text.startsWith("WORLDLINE-CENSUS/1\nsection=" + section + "\n"),
                    section + " framing drifted");
            int rows = Integer.parseInt(lineOf(text, "rows="));
            if (section.equals("blocks")) blocks = rows;
            else if (section.equals("items")) items = rows;
            else if (section.equals("recipes")) recipes = rows;
            else smelts = rows;
            require(text.endsWith("sha256=" + bodySha(text) + "\n"), section + " digest drifted");
        }
        require(blocks >= 90 && items >= 100 && recipes >= 100 && smelts >= 10,
                "implausible census sizes " + blocks + "/" + items + "/" + recipes + "/" + smelts);
        String glass = new String(Files.readAllBytes(
                build.resolve("first").resolve("blocks.wlcensus")), StandardCharsets.UTF_8);
        require(glass.contains("b020="), "glass block row missing");
        String ironSmelt = new String(Files.readAllBytes(
                build.resolve("first").resolve("smelts.wlcensus")), StandardCharsets.UTF_8);
        boolean ironFound = false;
        for (String row : ironSmelt.split("\n")) {
            if (row.startsWith("s") && row.contains("in=15 ") && row.contains("out=265x")) {
                ironFound = true;
            }
        }
        require(ironFound, "iron smelt row missing");
        String report = "blocks=" + blocks + "\nitems=" + items + "\nrecipes=" + recipes
                + "\nsmelts=" + smelts + "\nsections.deterministic=true\niron.smelt=present"
                + "\nglass.row=present\ndigests.sha256=" + sectionDigests() + "\n";
        String signature = sha256(report);
        Properties expected = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(
                root.resolve("smokes").resolve(ID).resolve("smoke.properties"))) {
            expected.load(reader);
        }
        require(signature.equals(expected.getProperty("expected.signature")),
                "census evidence diverged: " + signature);
        Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
        System.out.println("Census cycle passed");
        System.out.println("  blocks=" + blocks + " items=" + items
                + " recipes=" + recipes + " smelts=" + smelts);
        System.out.println("  evidence SHA-256: " + signature);
    }

    private String sectionDigests() throws Exception {
        StringBuilder digests = new StringBuilder();
        for (String section : List.of("blocks", "items", "recipes", "smelts")) {
            digests.append(section).append(':').append(sha256(Files.readAllBytes(
                    build.resolve("first").resolve(section + ".wlcensus")))).append(';');
        }
        return digests.toString();
    }

    private String bodySha(String text) throws Exception {
        int cut = text.lastIndexOf("sha256=");
        return sha256(text.substring(0, cut).getBytes(StandardCharsets.UTF_8));
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

    private String sha256(byte[] data) throws Exception { MessageDigest d = MessageDigest.getInstance("SHA-256");
        StringBuilder r = new StringBuilder(); for (byte b : d.digest(data)) r.append(String.format("%02x", b)); return r.toString(); }
    private String sha256(String text) throws Exception { return sha256(text.getBytes(StandardCharsets.UTF_8)); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }
    private static final class Result { final int code; final String text;
        Result(int code, String text) { this.code = code; this.text = text; } }
}
