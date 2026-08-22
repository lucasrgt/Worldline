import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Builds one pinned Aero artifact for a homogeneous GUI smoke batch. */
public final class AeroPrebuild {
    private static final Path ROOT = Path.of("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try { execute(arguments); }
        catch (Exception error) { System.err.println("Aero prebuild failed: " + error.getMessage()); System.exit(1); }
    }

    private static void execute(String[] arguments) throws Exception {
        require(arguments.length >= 2, "usage: java tools/containers/AeroPrebuild.java OUTPUT SMOKE_ID...");
        Path output = Path.of(arguments[0]).toAbsolutePath().normalize(); Properties first = smoke(arguments[1]);
        String relative = required(first, "aero.path"), revision = required(first, "aero.revision");
        String repository = required(first, "aero.repository");
        for (int index = 2; index < arguments.length; index++) {
            Properties next = smoke(arguments[index]);
            require(relative.equals(required(next, "aero.path")) && revision.equals(required(next, "aero.revision"))
                    && repository.equals(required(next, "aero.repository")), "GUI batch mixes Aero inputs");
        }
        Path checkout = ROOT.resolve(relative).normalize(); require(checkout.startsWith(ROOT), "Aero checkout escapes repository");
        require(git(checkout, "rev-parse", "HEAD").trim().equals(revision), "Aero revision drift");
        require(git(checkout, "remote", "get-url", "origin").trim().equals(repository), "Aero repository drift");
        require(git(checkout, "status", "--porcelain", "--untracked-files=all").trim().isEmpty(), "Aero checkout is dirty");
        Path stationapi = checkout.resolve("stationapi"); String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        Process build = new ProcessBuilder(stationapi.resolve(wrapper).toString(), "--no-daemon", "remapJar")
                .directory(stationapi.toFile()).inheritIO().start();
        require(build.waitFor(10, TimeUnit.MINUTES) && build.exitValue() == 0, "Aero remapJar failed");
        Path artifact = stationapi.resolve("build/libs/aero-model-lib-3.0.0.jar"); require(Files.isRegularFile(artifact), "Aero artifact missing");
        Files.createDirectories(output.getParent()); Files.copy(artifact, output, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Aero prebuild ready: " + output);
    }

    private static Properties smoke(String id) throws IOException {
        require(id.matches("m[0-9]+-[a-z0-9-]+"), "unsafe smoke id: " + id);
        Path file = ROOT.resolve("smokes").resolve(id).resolve("smoke.properties");
        require(Files.isRegularFile(file), "missing smoke properties: " + id); Properties values = new Properties();
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { values.load(reader); } return values;
    }
    private static String git(Path directory, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of("git", "-C", directory.toString())); command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start(); String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        require(process.waitFor(60, TimeUnit.SECONDS) && process.exitValue() == 0, "git failed: " + text); return text;
    }
    private static String required(Properties values, String key) { String value = values.getProperty(key); require(value != null && !value.isBlank(), "missing " + key); return value.trim(); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalArgumentException(message); }
}
