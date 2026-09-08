import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Four-process native entity-render differential with exact mapped and official provenance. */
public final class NativeEntityRenderCycle {
    private static final String ID = "b173-native-entity-render-cycle";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Properties config = new Properties();
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);

    public static void main(String[] arguments) {
        try {
            SmokeSupport.require(arguments.length == 1 && arguments[0].equals(ID), "native entity cycle ID");
            new NativeEntityRenderCycle().execute();
        } catch (Exception error) {
            error.printStackTrace(); System.exit(1);
        }
    }

    private void execute() throws Exception {
        Path smoke = root.resolve("smokes").resolve(ID);
        SmokeSupport.load(smoke.resolve("smoke.properties"), config);
        Path workspace = root.resolve(value("workspace"));
        Path official = workspace.resolve("jars/minecraft.jar");
        Path mappings = workspace.resolve("conf/mappings.tiny");
        Path lwjgl = workspace.resolve(value("lwjgl.path"));
        Path natives = workspace.resolve("libraries/natives");
        verify(official, "client.jar.sha256"); verify(mappings, "mappings.tiny.sha256");
        verify(lwjgl, "lwjgl.jar.sha256"); verify(natives.resolve("lwjgl64.dll"), "lwjgl64.dll.sha256");
        Files.createDirectories(build); compile(smoke, lwjgl);
        String baseline = null;
        for (String role : List.of("mapped", "official")) {
            for (int attempt = 1; attempt <= 2; attempt++) {
                Path output = build.resolve(role + '-' + attempt + ".properties");
                String separator = System.getProperty("path.separator");
                String game = role.equals("mapped") ? workspace.resolve("minecraft/bin")
                        + separator + official : official.toString();
                String log = SmokeSupport.capture(root, List.of("java", "-Djava.awt.headless=true",
                        "-Dorg.lwjgl.librarypath=" + natives.toAbsolutePath(), "-classpath",
                        build.resolve("classes") + separator + game + separator + lwjgl,
                        "worldline.smoke.b173entityrender.B173NativeEntityRenderSmoke", role,
                        mappings.toString(), smoke.resolve("subjects.tsv").toString(), output.toString()));
                Files.writeString(build.resolve(role + '-' + attempt + ".log"), log, StandardCharsets.UTF_8);
                SmokeSupport.require(SmokeSupport.line(log, "WORLDLINE_ENTITY_RENDER_ROLE=").equals(role)
                        && SmokeSupport.line(log, "WORLDLINE_ENTITY_RENDER_SUBJECTS=").equals("23"),
                        "native entity capture identity drift");
                String canonical = Files.readString(output, StandardCharsets.UTF_8);
                SmokeSupport.require(SmokeSupport.digest(output, "SHA-256").equals(
                        SmokeSupport.line(log, "WORLDLINE_ENTITY_RENDER_SIGNATURE=")), "capture digest drift");
                if (baseline == null) baseline = canonical;
                else SmokeSupport.require(baseline.equals(canonical), "native mapped/official render divergence: "
                        + role + '-' + attempt + "; compare captures in " + build);
            }
        }
        Path evidence = build.resolve("evidence.txt");
        Files.writeString(evidence, baseline, StandardCharsets.UTF_8);
        String signature = SmokeSupport.digest(evidence, "SHA-256");
        SmokeSupport.require(signature.equals(value("expected.signature")),
                "native entity frozen signature differs: " + signature);
        String signal = "family=native-entity-render,subjects=23,processes=4,abstract=48:NOT_APPLICABLE,"
                + "oracle=mapped-official-native-rgba";
        String trace = "v1|client=official-b1.7.3|family=native-entity-render|subjects=23|scene=canonical-body|"
                + "oracle=mapped-official-native-rgba";
        SmokeSupport.require(signal.equals(value("expected.signal"))
                && trace.equals(value("expected.trace")), "native entity signal or trace drift");
        System.out.println("WORLDLINE_ENTITY_RENDER_SET=" + signal);
        System.out.println("WORLDLINE_ENTITY_RENDER_TRACE=" + trace);
        System.out.println("WORLDLINE_ENTITY_RENDER_SIGNATURE=" + signature);
    }

    private void compile(Path smoke, Path lwjgl) throws Exception {
        Path classes = build.resolve("classes"); Files.createDirectories(classes);
        List<String> command = new ArrayList<>(List.of("javac", "--release", "8", "-encoding", "UTF-8",
                "-Xlint:all,-options", "-Werror", "-classpath", lwjgl.toString(), "-d", classes.toString()));
        for (String source : List.of("NativeEntityRenderSubject", "NativeEntityRenderObservation")) {
            command.add(root.resolve("modules/testapi/src/main/java/worldline/testapi/" + source + ".java").toString());
        }
        for (String source : List.of("NativeEntityRenderFixture", "NativeEntityRenderEvidence")) {
            command.add(root.resolve("modules/testkit/src/main/java/worldline/testkit/" + source + ".java").toString());
        }
        for (String source : List.of("B173NativeNames", "B173EntityRenderWorld", "B173EntityRenderRuntime",
                "B173EntityRenderFrame", "B173EntityRender", "B173RgbaDigest", "B173ReflectionObjects",
                "B173ChunkProviderAccess")) {
            command.add(root.resolve("adapters/b173-client/src/main/java/worldline/b173/"
                    + source + ".java").toString());
        }
        command.add(smoke.resolve("src/worldline/smoke/b173entityrender/B173NativeEntityRenderSmoke.java").toString());
        SmokeSupport.capture(root, command);
    }

    private void verify(Path path, String key) throws Exception {
        SmokeSupport.require(SmokeSupport.digest(path, "SHA-256").equals(value(key)),
                "native render input drift: " + path);
    }
    private String value(String key) { return SmokeSupport.value(config, key); }
}
