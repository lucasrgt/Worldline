import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Creates a deterministic, non-qualifiable milestone draft without overwriting repository data. */
final class MilestoneScaffold {
    private MilestoneScaffold() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length == 1, "usage: MilestoneScaffold ID");
            generate(Path.of("").toAbsolutePath().normalize(), arguments[0]);
        } catch (Exception error) {
            System.err.println("milestone scaffold failed: " + error.getMessage());
            System.exit(1);
        }
    }

    static void generate(Path root, String id) throws Exception {
        require(id.matches("m[0-9]+-[a-z0-9]+(?:-[a-z0-9]+)*"),
                "new milestone id must be m<number>-<slug>");
        String number = id.substring(1, id.indexOf('-'));
        String slug = id.substring(id.indexOf('-') + 1);
        String title = title(slug), className = "M" + number + javaName(slug) + "Cycle";
        String signal = "id=" + id + ",state=draft,claim=unassigned";
        String trace = "v1|" + signal + "|qualification=forbidden-until-authored";
        String signature = sha256(trace);
        Path smoke = root.resolve("smokes").resolve(id);
        Map<Path, String> files = new LinkedHashMap<>();
        files.put(smoke.resolve("smoke.properties"), descriptor(id, number, slug, className,
                signal, signature));
        files.put(smoke.resolve("MAP.md"), map(id, title, signal, trace, signature));
        files.put(root.resolve("docs/M" + number + "_" + slug.toUpperCase().replace('-', '_') + ".md"),
                claim(id, title, signal, signature));
        files.put(root.resolve("docs/M" + number + "_CYCLE.md"), cycle(id, className, signal, signature));
        files.put(root.resolve("tools/smoke/" + className + ".java"), runner(id, className));
        for (Path path : files.keySet()) require(!Files.exists(path),
                "refusing to overwrite " + root.relativize(path));
        for (Map.Entry<Path, String> entry : files.entrySet()) {
            Files.createDirectories(entry.getKey().getParent());
            Files.writeString(entry.getKey(), entry.getValue(), StandardCharsets.UTF_8);
        }
        validateDraft(root, id);
        System.out.println("milestone scaffold created: " + id);
        System.out.println("  candidate: java tools/harness/Gate.java --candidate " + id);
        System.out.println("  runtime qualification remains fail-closed until the draft runner is replaced");
    }

    static void selfTest() throws Exception {
        Path first = Files.createTempDirectory("worldline-scaffold-one-");
        Path second = Files.createTempDirectory("worldline-scaffold-two-");
        try {
            prepare(first); prepare(second);
            generate(first, "m900-example-contract"); generate(second, "m900-example-contract");
            List<Path> one = files(first), two = files(second);
            require(one.size() == 5 && relative(first, one).equals(relative(second, two)),
                    "scaffold topology drifted");
            for (int index = 0; index < one.size(); index++) require(
                    Files.mismatch(one.get(index), two.get(index)) == -1L, "scaffold bytes are not deterministic");
            boolean rejected = false;
            try { generate(first, "m900-example-contract"); }
            catch (IllegalStateException expected) { rejected = true; }
            require(rejected, "scaffold overwrite was accepted");
            Path runner = one.stream().filter(path -> path.toString().endsWith(".java")).findFirst().orElseThrow();
            String output = ProcessCapture.require(first, List.of(javaTool("javac"), "-encoding", "UTF-8",
                    "--release", "21", "-Xlint:all,-options", "-Werror", runner.toString()), 30);
            require(output.isBlank(), "draft runner compilation emitted output");
            System.out.println("milestone scaffold self-test passed");
        } finally {
            SafeTreeDelete.delete(first); SafeTreeDelete.delete(second);
        }
    }

    private static void validateDraft(Path root, String id) throws Exception {
        Path descriptor = root.resolve("smokes").resolve(id).resolve("smoke.properties");
        Properties values = new Properties();
        try (var reader = Files.newBufferedReader(descriptor, StandardCharsets.UTF_8)) { values.load(reader); }
        require(id.equals(values.getProperty("id")) && "tooling".equals(values.getProperty("candidate.kind"))
                && "draft".equals(values.getProperty("scaffold.status")), "draft identity drifted");
        require(values.getProperty("expected.signature", "").matches("[0-9a-f]{64}"),
                "draft signature is not frozen");
        require("1".equals(values.getProperty("qualification.schema"))
                && "tooling-cycle".equals(values.getProperty("qualification.proof")),
                "draft qualification contract drifted");
        for (String key : List.of("qualification.docs", "qualification.cycle",
                "qualification.semantic-map", "runner.source")) {
            Path path = root.resolve(values.getProperty(key, "")).normalize();
            require(path.startsWith(root) && Files.isRegularFile(path), "missing draft artifact " + key);
            String text = Files.readString(path, StandardCharsets.UTF_8);
            if (!key.equals("runner.source")) require(text.contains(values.getProperty("expected.signature")),
                    key + " does not bind the draft signature");
        }
    }

    private static String descriptor(String id, String number, String slug, String className,
            String signal, String signature) {
        return "id=" + id + "\ncandidate.kind=tooling\nscaffold.status=draft\nrunner.source=tools/smoke/"
                + className + ".java\nexpected.signal=" + signal + "\nexpected.signature=" + signature
                + "\natlas.subsystems=unassigned\natlas.artifact=worldline\n\nqualification.schema=1"
                + "\nqualification.proof=tooling-cycle\nqualification.docs=docs/M" + number + "_"
                + slug.toUpperCase().replace('-', '_') + ".md\nqualification.cycle=docs/M" + number
                + "_CYCLE.md\nqualification.semantic-map=smokes/" + id + "/MAP.md"
                + "\nqualification.atlas=not-applicable\nqualification.atlas.reason=draft publishes no behavior"
                + "\nqualification.testkit=not-applicable"
                + "\nqualification.testkit.reason=draft publishes no behavior\n";
    }

    private static String claim(String id, String title, String signal, String signature) {
        return "# " + id.toUpperCase() + " " + title + "\n\nThis deterministic scaffold publishes no behavior. "
                + "Replace the draft runner and author the claim before runtime qualification.\n\n"
                + "Draft signal: `" + signal + "`.\n\nDraft SHA-256: `" + signature + "`.\n";
    }
    private static String cycle(String id, String className, String signal, String signature) {
        return "# " + id.toUpperCase() + " qualification cycle\n\n`" + className
                + "` is deliberately fail-closed while this milestone is a scaffold. Candidate validation may"
                + " compile it, but runtime qualification must fail until a real deterministic cycle replaces it.\n\n"
                + "Draft signal: `" + signal + "`.\n\nDraft SHA-256: `" + signature + "`.\n";
    }
    private static String map(String id, String title, String signal, String trace, String signature) {
        return "# " + id.toUpperCase() + " " + title
                + " behavior map\n\nNo semantic boundary is claimed by this draft.\n\n"
                + "Draft signal: `" + signal + "`.\n\nDraft trace: `" + trace + "`.\n\nDraft SHA-256: `"
                + signature + "`.\n";
    }
    private static String runner(String id, String className) {
        return "/** Fail-closed milestone draft; replace with an authored deterministic cycle. */\n"
                + "public final class " + className + " {\n"
                + "    private " + className + "() { }\n"
                + "    public static void main(String[] arguments) {\n"
                + "        if (arguments.length != 1 || !arguments[0].equals(\"" + id + "\")) {\n"
                + "            System.err.println(\"usage: " + className + " " + id + "\");\n"
                + "            System.exit(2);\n        }\n"
                + "        System.err.println(\"draft milestone cannot be runtime-qualified; author the cycle\");\n"
                + "        System.exit(1);\n    }\n}\n";
    }

    private static List<Path> files(Path root) throws Exception {
        try (var paths = Files.walk(root)) { return paths.filter(Files::isRegularFile).sorted().toList(); }
    }
    private static List<String> relative(Path root, List<Path> paths) {
        return paths.stream().map(root::relativize).map(Path::toString).toList();
    }
    private static void prepare(Path root) throws Exception {
        Files.createDirectories(root.resolve("smokes"));
        Files.createDirectories(root.resolve("docs"));
        Files.createDirectories(root.resolve("tools/smoke"));
    }
    private static String title(String slug) {
        return String.join(" ", List.of(slug.split("-")));
    }
    private static String javaName(String slug) {
        StringBuilder value = new StringBuilder();
        for (String part : slug.split("-"))
            value.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        return value.toString();
    }
    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", name + (windows ? ".exe" : "")).toString();
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
