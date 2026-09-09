import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/** Shared four-process native capture protocol for declarative client and server families. */
public final class NativeDifferentialCapture {
    private NativeDifferentialCapture() { }

    public static void execute(Path root, String id, Properties config) throws Exception {
        require("1".equals(value(config, "native.capture.schema")), "unsupported native capture schema");
        Path smoke = root.resolve("smokes").resolve(id), build = root.resolve(".worldline/smokes").resolve(id);
        Path workspace = root.resolve(value(config, "workspace")).normalize();
        require(workspace.startsWith(root.resolve("local")), "native workspace escaped local inputs");
        String side = value(config, "side");
        require(side.equals("client") || side.equals("server"), "native side differs");
        boolean client = side.equals("client");
        String game = client ? "minecraft" : "minecraft_server";
        Path official = workspace.resolve("jars/" + game + ".jar");
        Path mappings = workspace.resolve("conf/mappings.tiny");
        verify(official, value(config, side + ".jar.sha256"));
        verify(mappings, value(config, "mappings.tiny.sha256"));
        String separator = System.getProperty("path.separator"), extra = "";
        List<String> options = new ArrayList<>();
        if (client) {
            Path lwjgl = workspace.resolve(value(config, "lwjgl.path"));
            Path natives = workspace.resolve("libraries/natives");
            verify(lwjgl, value(config, "lwjgl.jar.sha256"));
            verify(natives.resolve("lwjgl64.dll"), value(config, "lwjgl64.dll.sha256"));
            options.add("-Djava.awt.headless=true");
            options.add("-Dorg.lwjgl.librarypath=" + natives.toAbsolutePath()); extra = lwjgl.toString();
        }
        Files.createDirectories(build); Path classes = build.resolve("classes"); Files.createDirectories(classes);
        List<String> compile = new ArrayList<>(List.of("javac", "--release", "8", "-encoding", "UTF-8",
                "-Xlint:all,-options", "-Werror", "-classpath", extra.isEmpty() ? "." : extra,
                "-d", classes.toString()));
        int count = Integer.parseInt(value(config, "native.sources.count"));
        require(count > 0 && count <= 64, "native source closure count differs");
        HashSet<String> unique = new HashSet<>();
        for (int index = 1; index <= count; index++) {
            String source = value(config, "native.source." + index);
            require(sourcePath(source) && unique.add(source), "invalid native source closure: " + source);
            Path path = root.resolve(source); require(Files.isRegularFile(path), "native source is absent");
            compile.add(path.toString());
        }
        String main = value(config, "native.main");
        require(main.matches("worldline[.][A-Za-z0-9_.]+"), "invalid native capture main");
        compile.add(smoke.resolve("src/" + main.replace('.', '/') + ".java").toString());
        SmokeSupport.capture(root, compile);
        Path subjects = smoke.resolve("subjects.tsv");
        long subjectsCount = Files.readAllLines(subjects, StandardCharsets.UTF_8).stream()
                .skip(1).filter(line -> !line.isBlank()).count();
        String prefix = value(config, "native.output.prefix"), baseline = null;
        for (String role : List.of("mapped", "official")) for (int attempt = 1; attempt <= 2; attempt++) {
            Path output = build.resolve(role + '-' + attempt + ".properties");
            String nativePath = role.equals("mapped") ? workspace.resolve(game + "/bin") + separator + official
                    : official.toString();
            List<String> command = new ArrayList<>(List.of("java")); command.addAll(options);
            command.addAll(List.of("-classpath", classes + separator + nativePath
                    + (extra.isEmpty() ? "" : separator + extra), main, role, mappings.toString(),
                    subjects.toString(), output.toString()));
            String log = SmokeSupport.capture(root, command);
            Files.writeString(build.resolve(role + '-' + attempt + ".log"), log, StandardCharsets.UTF_8);
            require(SmokeSupport.line(log, prefix + "ROLE=").equals(role)
                    && SmokeSupport.line(log, prefix + "SUBJECTS=").equals(Long.toString(subjectsCount)),
                    "native capture role or family differs");
            String origin = SmokeSupport.line(log, prefix + "PROVENANCE=").replace('\\', '/');
            require(origin.contains(role.equals("mapped") ? game + "/bin/" : "jars/" + game + ".jar"),
                    "native capture provenance differs");
            String canonical = Files.readString(output, StandardCharsets.UTF_8);
            require(SmokeSupport.digest(output, "SHA-256").equals(
                    SmokeSupport.line(log, prefix + "SIGNATURE=")), "native capture output digest differs");
            if (baseline == null) baseline = canonical;
            else equivalent(baseline, canonical);
        }
        Path evidence = build.resolve("evidence.txt"); Files.writeString(evidence, baseline, StandardCharsets.UTF_8);
        String signature = SmokeSupport.digest(evidence, "SHA-256");
        require(signature.equals(value(config, "expected.signature")),
                "native frozen signature differs: " + signature);
        System.out.println(prefix + "SET=" + value(config, "expected.signal"));
        System.out.println(prefix + "TRACE=" + value(config, "expected.trace"));
        System.out.println(prefix + "SIGNATURE=" + signature);
    }

    static void selfTest() {
        require(sourcePath("modules/testapi/src/main/java/worldline/testapi/Subject.java")
                && sourcePath("adapters/b173-server/src/testkit/java/worldline/b173server/Native.java")
                && !sourcePath("modules/api/src/test/java/Probe.java")
                && !sourcePath("adapters/b173-server/src/main/java/../../Foreign.java"),
                "native source confinement drift");
        equivalent("subject=one\n", "subject=one\n");
        try {
            equivalent("subject=one\n", "subject=two\n");
            throw new IllegalStateException("divergent native capture was accepted");
        } catch (IllegalArgumentException expected) { }
    }
    private static void equivalent(String first, String next) {
        if (first.isEmpty() || !first.equals(next))
            throw new IllegalArgumentException("native replica/oracle divergence");
    }
    private static boolean sourcePath(String path) {
        return path.matches("(?:modules|adapters)/[a-z0-9-]+/src/(?:main|testkit)/java/[A-Za-z0-9_/]+[.]java");
    }
    private static void verify(Path path, String expected) throws Exception {
        require(SmokeSupport.digest(path, "SHA-256").equals(expected), "native input drift: " + path);
    }
    private static String value(Properties values, String key) { return SmokeSupport.value(values, key); }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
