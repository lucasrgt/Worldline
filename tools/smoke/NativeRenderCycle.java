import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Qualifies mapped and official Minecraft rendering through a real offscreen context. */
public final class NativeRenderCycle {
    private static final String ID = "m10-native-render";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Properties config = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/NativeRenderCycle.java " + ID);
            System.exit(2);
        }
        try { new NativeRenderCycle().execute(); }
        catch (Exception error) {
            System.err.println("native render cycle failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Path smoke = root.resolve("smokes").resolve(ID);
        load(smoke.resolve("smoke.properties"));
        require(ID.equals(value("id")), "smoke descriptor id mismatch");
        require(System.getProperty("os.name").startsWith("Windows"), "Windows is required");
        require(System.getProperty("os.arch").contains("64"), "64-bit JVM is required");
        Path workspace = local(value("workspace"));
        Path mapped = workspace.resolve("minecraft/bin");
        Path official = workspace.resolve("jars/minecraft.jar");
        Path lwjgl = workspace.resolve("libraries/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209"
                + "/lwjgl-2.9.4-nightly-20150209.jar");
        Path natives = workspace.resolve("libraries/natives");
        verifyHash(official, value("client.jar.sha256"));
        verifyHash(workspace.resolve("conf/version.json"), value("version.json.sha256"));
        verifyHash(workspace.resolve("conf/mappings.tiny"), value("mappings.tiny.sha256"));
        verifyHash(lwjgl, value("lwjgl.jar.sha256"));
        verifyHash(natives.resolve("lwjgl64.dll"), value("lwjgl64.dll.sha256"));
        require(Files.isRegularFile(mapped.resolve("net/minecraft/src/Tessellator.class")),
                "mapped Tessellator.class is absent; run the client smoke first");
        verifyAeroBoundary();

        Path build = root.resolve(".worldline/smokes").resolve(ID).normalize();
        Files.createDirectories(build);
        compile(smoke.resolve("src"), build.resolve("classes"), lwjgl);
        String mappedFirst = run(build, mapped, lwjgl, natives, "mapped");
        String mappedSecond = run(build, mapped, lwjgl, natives, "mapped");
        String officialFirst = run(build, official, lwjgl, natives, "official");
        String officialSecond = run(build, official, lwjgl, natives, "official");
        require(mappedFirst.equals(mappedSecond), "mapped processes diverged");
        require(officialFirst.equals(officialSecond), "official processes diverged");
        require(mappedFirst.equals(officialFirst), "mapped and official renderers diverged");
        require(value("expected.frame.sha256").equals(mappedFirst),
                "frame diverged from frozen signature: " + mappedFirst);
        Path evidence = writeEvidence(build, mappedFirst);
        System.out.println("native render cycle passed");
        System.out.println("  processes: 4 (2 mapped, 2 official)");
        System.out.println("  Minecraft Tessellator -> LWJGL -> Pbuffer -> RGBA: MATCH");
        System.out.println("  frame signature: " + mappedFirst);
        System.out.println("  Aero compatibility: NOT RUN (artifact absent)");
        System.out.println("  evidence: " + root.relativize(evidence));
    }

    private String run(Path build, Path renderer, Path lwjgl, Path natives, String role)
            throws Exception {
        String separator = System.getProperty("path.separator");
        List<String> command = new ArrayList<>(Arrays.asList("java", "-Djava.awt.headless=true",
                "-Dorg.lwjgl.librarypath=" + natives.toAbsolutePath(), "-classpath",
                build.resolve("classes") + separator + renderer + separator + lwjgl,
                "worldline.smoke.m10.NativeRenderSmoke", role, value(role + ".class"),
                value(role + ".instance"), value(role + ".start"), value(role + ".color"),
                value(role + ".vertex"), value(role + ".draw")));
        String output = capture(command);
        require(output.contains("WORLDLINE_RENDER_CONTEXT=Pbuffer"), "Pbuffer proof is absent");
        require(output.contains("WORLDLINE_RENDER_DISPLAY_CREATED=false"), "offscreen proof is absent");
        require(output.contains("WORLDLINE_RENDER_GEOMETRY_PIXELS=1280"), "coverage proof is absent");
        String provenance = line(output, "WORLDLINE_RENDER_PROVENANCE=").replace('\\', '/');
        require(provenance.contains(role.equals("mapped") ? "minecraft/bin/" : "jars/minecraft.jar"),
                "wrong " + role + " renderer provenance: " + provenance);
        return line(output, "WORLDLINE_RENDER_SHA256=");
    }

    private void verifyAeroBoundary() {
        Path candidate = root.resolve(value("aero.candidate")).normalize();
        require(candidate.startsWith(root.resolve("local").normalize()),
                "Aero candidate must remain under local/");
        require("artifact-absent".equals(value("aero.status")), "unknown Aero status");
        require(!Files.exists(candidate), "Aero artifact appeared; explicit qualification is required");
    }

    private void compile(Path source, Path output, Path lwjgl) throws Exception {
        Files.createDirectories(output);
        List<String> command = new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8",
                "--release", "8", "-Xlint:all,-options", "-Werror", "-classpath",
                lwjgl.toString(), "-d", output.toString()));
        command.add(source.resolve("worldline/smoke/m10/NativeRenderSmoke.java").toString());
        capture(command);
        System.out.println("  native render scenario compilation: passed");
    }

    private Path writeEvidence(Path build, String hash) throws IOException {
        Path evidence = build.resolve("evidence.txt");
        String text = "id=" + ID + "\nprocesses=4\ncontext=Pbuffer\ndisplay.created=false"
                + "\nrenderer.path=Minecraft-Tessellator->LWJGL->OpenGL->RGBA"
                + "\nofficial.oracle=MATCH\nframe.sha256=" + hash
                + "\naero.artifact=ABSENT\naero.runtime.compatibility=NOT_RUN\n";
        Files.write(evidence, text.getBytes(StandardCharsets.UTF_8));
        return evidence;
    }

    private void verifyHash(Path path, String expected) throws Exception {
        require(Files.isRegularFile(path), "missing native input: " + path);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(Files.readAllBytes(path));
        StringBuilder actual = new StringBuilder();
        for (byte item : digest.digest()) actual.append(String.format("%02x", item & 255));
        require(actual.toString().equals(expected), "native input drift: " + path);
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

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
