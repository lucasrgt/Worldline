import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Qualifies one declarative native inventory-render family across mapped and official clients. */
public final class NativeInventoryRenderCycle {
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Properties config = new Properties();
    private final String id;

    private NativeInventoryRenderCycle(String id) { this.id = id; }

    public static void main(String[] arguments) {
        if (arguments.length != 1 || !arguments[0].matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            System.err.println("usage: NativeInventoryRenderCycle ID");
            System.exit(2);
        }
        try { new NativeInventoryRenderCycle(arguments[0]).execute(); }
        catch (Exception error) {
            System.err.println("native 3D inventory render cycle failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Path smoke = root.resolve("smokes").resolve(id);
        load(smoke.resolve("smoke.properties"));
        require(id.equals(value("id")), "smoke descriptor id mismatch");
        require(System.getProperty("os.name").startsWith("Windows"), "Windows is required");
        Path workspace = local(value("workspace"));
        Path mapped = workspace.resolve("minecraft/bin");
        Path official = workspace.resolve("jars/minecraft.jar");
        Path lwjgl = workspace.resolve(value("lwjgl.path"));
        Path natives = workspace.resolve("libraries/natives");
        verifyHash(official, value("client.jar.sha256"));
        verifyHash(workspace.resolve("conf/mappings.tiny"), value("mappings.tiny.sha256"));
        verifyHash(lwjgl, value("lwjgl.jar.sha256"));
        verifyHash(natives.resolve("lwjgl64.dll"), value("lwjgl64.dll.sha256"));
        verifyTerrain(official, value("terrain.png.sha256"));
        require(Files.isRegularFile(mapped.resolve("net/minecraft/src/RenderBlocks.class")),
                "mapped RenderBlocks.class is absent; run the client smoke first");

        Path build = root.resolve(".worldline/smokes").resolve(id);
        Files.createDirectories(build);
        compile(smoke.resolve("src"), build.resolve("classes"), lwjgl);
        Result mappedFirst = run(build, mapped, official, lwjgl, natives, smoke, "mapped", "first");
        Result mappedSecond = run(build, mapped, official, lwjgl, natives, smoke, "mapped", "second");
        Result officialFirst = run(build, mapped, official, lwjgl, natives, smoke, "official", "first");
        Result officialSecond = run(build, mapped, official, lwjgl, natives, smoke, "official", "second");
        require(mappedFirst.same(mappedSecond), "mapped native block renders diverged");
        require(officialFirst.same(officialSecond), "official native block renders diverged");
        require(mappedFirst.same(officialFirst), "mapped and official native block renders diverged");
        require(mappedFirst.signature.equals(value("expected.signature")),
                "render family diverged from frozen signature: " + mappedFirst.signature);
        Path evidence = writeEvidence(build, mappedFirst);
        int subjects = subjectCount(smoke); String renderTypes = renderTypes(smoke);
        String family = value("behavior");
        String signal = "family=" + family + ",subjects=" + subjects + ",claims=" + subjects
                + ",render-types=" + renderTypes.split(",").length + ","
                + "processes=4,oracle=mapped-official-native-rgba";
        String trace = "v1|client=official-b1.7.3|family=" + family + "|subjects=" + subjects
                + "|render-types=" + renderTypes + "|evidence=" + mappedFirst.signature
                + "|oracle=mapped-official-native-rgba";
        require(signal.equals(value("expected.signal")), "native render signal drifted");
        require(trace.equals(value("expected.trace")), "native render trace drifted");
        String prefix = value("cycle.output.prefix");
        System.out.println(prefix + "SET=" + signal);
        System.out.println(prefix + "TRACE=" + trace);
        System.out.println(prefix + "SIGNATURE=" + mappedFirst.signature);
        System.out.println("  processes: 4 (2 mapped, 2 official); evidence: "
                + root.relativize(evidence));
    }

    private Result run(Path build, Path mapped, Path official, Path lwjgl, Path natives,
            Path smoke, String role, String attempt) throws Exception {
        String separator = System.getProperty("path.separator");
        Path evidence = build.resolve(role + '-' + attempt + ".properties");
        List<String> command = new ArrayList<>(Arrays.asList("java", "-Djava.awt.headless=true",
                "-Dorg.lwjgl.librarypath=" + natives.toAbsolutePath(), "-classpath",
                build.resolve("classes") + separator + mapped + separator + official + separator + lwjgl,
                value("cycle.smoke.main"), role,
                value(role + ".renderer.class"), value(role + ".block.class"),
                value(role + ".blocks.field"), value(role + ".render.method"),
                value(role + ".render-type.method"), value(role + ".render-3d.method"),
                official.toString(), smoke.resolve("subjects.tsv").toString(), evidence.toString()));
        String output = capture(command);
        String prefix = value("cycle.output.prefix");
        require(output.contains(prefix + "ROLE=" + role), "native render role is absent");
        require(output.contains(prefix + "SUBJECTS=" + subjectCount(smoke)),
                "native render census is absent");
        String provenance = line(output, prefix + "PROVENANCE=").replace('\\', '/');
        require(provenance.contains(role.equals("mapped") ? "minecraft/bin/" : "jars/minecraft.jar"),
                "wrong " + role + " renderer provenance: " + provenance);
        return new Result(line(output, prefix + "SIGNATURE="),
                Files.readString(evidence, StandardCharsets.UTF_8));
    }

    private void compile(Path source, Path output, Path lwjgl) throws Exception {
        Files.createDirectories(output);
        List<String> command = new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8",
                "--release", "8", "-Xlint:all,-options", "-Werror", "-classpath",
                lwjgl.toString(), "-d", output.toString()));
        for (String relative : List.of(
                "modules/testapi/src/main/java/worldline/testkit/NativeBlockRenderSubject.java",
                "modules/testapi/src/main/java/worldline/testkit/NativeBlockRenderObservation.java",
                "modules/testapi/src/main/java/worldline/testkit/NativeBlockRenderPlan.java",
                "modules/testkit/src/main/java/worldline/testkit/NativeBlockRenderEvidence.java",
                "modules/testkit/src/main/java/worldline/testkit/NativeBlockRenderFixture.java",
                "adapters/b173-client/src/main/java/worldline/b173/B173BlockInventoryFrame.java",
                "adapters/b173-client/src/main/java/worldline/b173/B173TerrainTexture.java",
                "adapters/b173-client/src/main/java/worldline/b173/B173BlockInventoryRender.java")) {
            command.add(root.resolve(relative).toString());
        }
        command.add(source.resolve(value("cycle.smoke.source")).toString());
        capture(command);
    }

    private Path writeEvidence(Path build, Result result) throws IOException {
        Path evidence = build.resolve("evidence.txt");
        String header = "id=" + id + "\nprocesses=4\ncontext=Pbuffer\ndisplay.created=false\n"
                + "oracle=mapped-official-native-rgba\nsignature=" + result.signature + "\n";
        Files.writeString(evidence, header + result.evidence, StandardCharsets.UTF_8);
        return evidence;
    }

    private void verifyTerrain(Path jarPath, String expected) throws Exception {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            JarEntry entry = jar.getJarEntry("terrain.png");
            require(entry != null, "official terrain atlas is absent");
            try (InputStream input = jar.getInputStream(entry)) {
                require(hash(input.readAllBytes()).equals(expected), "official terrain atlas drift");
            }
        }
    }

    private void verifyHash(Path path, String expected) throws Exception {
        require(Files.isRegularFile(path), "missing native input: " + path);
        require(hash(Files.readAllBytes(path)).equals(expected), "native input drift: " + path);
    }

    private int subjectCount(Path smoke) throws IOException {
        return Math.toIntExact(Files.readAllLines(smoke.resolve("subjects.tsv"),
                StandardCharsets.UTF_8).stream().skip(1).filter(row -> !row.isBlank()).count());
    }

    private String renderTypes(Path smoke) throws IOException {
        Set<Integer> types = new LinkedHashSet<>();
        for (String row : Files.readAllLines(smoke.resolve("subjects.tsv"), StandardCharsets.UTF_8)
                .stream().skip(1).filter(value -> !value.isBlank()).toList()) {
            String[] cells = row.split("\\t", -1);
            require(cells.length == 5, "invalid native render subject row");
            types.add(Integer.parseInt(cells[4]));
        }
        return String.join(",", types.stream().sorted().map(String::valueOf).toList());
    }

    private String capture(List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(0) + " failed\n" + output);
        return output;
    }

    private String line(String output, String prefix) {
        return Arrays.stream(output.split("\\R")).filter(row -> row.startsWith(prefix)).findFirst()
                .orElseThrow(() -> new IllegalStateException("missing output: " + prefix))
                .substring(prefix.length());
    }

    private void load(Path path) throws IOException {
        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            config.load(reader);
        }
    }

    private String value(String key) {
        String result = config.getProperty(key);
        require(result != null && !result.trim().isEmpty(), "missing smoke property: " + key);
        return result.trim();
    }

    private Path local(String relative) {
        Path base = root.resolve("local").normalize(), result = root.resolve(relative).normalize();
        require(result.startsWith(base) && !result.equals(base), "workspace must be inside local/");
        return result;
    }

    private static String hash(byte[] bytes) throws Exception {
        StringBuilder value = new StringBuilder();
        for (byte item : MessageDigest.getInstance("SHA-256").digest(bytes)) {
            value.append(String.format("%02x", item & 255));
        }
        return value.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static final class Result {
        private final String signature, evidence;
        private Result(String signature, String evidence) {
            this.signature = signature;
            this.evidence = evidence;
        }
        private boolean same(Result other) {
            return signature.equals(other.signature) && evidence.equals(other.evidence);
        }
    }
}
