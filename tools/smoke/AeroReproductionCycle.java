import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Captures two real Aero executions recreated from the same seed and camera. */
public final class AeroReproductionCycle {
    private static final String ID = "m12-aero-reproduction";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Properties config = new Properties();

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/AeroReproductionCycle.java " + ID);
            System.exit(2);
        }
        try { new AeroReproductionCycle().execute(); }
        catch (Exception error) {
            System.err.println("Aero reproduction cycle failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        Path smoke = root.resolve("smokes").resolve(ID); load(smoke.resolve("smoke.properties"));
        require(ID.equals(value("id")), "smoke descriptor id mismatch");
        Path checkout = root.resolve(value("aero.path")).normalize(); verifyCheckout(checkout);
        Path test = checkout.resolve("stationapi/test");
        Path run = test.resolve("run").normalize();
        Path activeSave = run.resolve("saves/WorldlineAero").normalize();
        require(activeSave.startsWith(run), "unsafe Aero save path");
        Path build = root.resolve(".worldline/smokes").resolve(ID).normalize(); recreate(build);
        Path frozenSave = build.resolve("saved-world");
        Path[] logs = {build.resolve("capture-1.log"), build.resolve("capture-2.log")};
        for (int index = 0; index < logs.length; index++) {
            Path log = logs[index]; delete(activeSave); capture(test, log, value("capture.ticks"));
            require(Files.isRegularFile(log) && Files.size(log) > 0L, "Aero frame log missing");
            if (index == 0) {
                require(Files.isRegularFile(activeSave.resolve("level.dat")), "captured Aero save missing");
                copyTree(activeSave, frozenSave);
            }
        }
        Path classes = build.resolve("classes"); Files.createDirectories(classes);
        Path analysis = root.resolve(".worldline/build/classes/analysis");
        Path minimization = root.resolve(".worldline/build/classes/minimization");
        require(Files.isDirectory(analysis) && Files.isDirectory(minimization),
                "run repository compilation before M12");
        String cp = analysis + System.getProperty("path.separator") + minimization;
        run(root, command("javac", "-encoding", "UTF-8", "--release", "8", "-Xlint:all,-options",
                "-Werror", "-classpath", cp, "-d", classes.toString(),
                root.resolve("adapters/aero-model-lib/src/main/java/worldline/aero/AeroFrameLog.java").toString(),
                smoke.resolve("src/worldline/smoke/m12/AeroReproductionSmoke.java").toString()));
        String output = output(root, command("java", "-classpath",
                classes + System.getProperty("path.separator") + cp,
                "worldline.smoke.m12.AeroReproductionSmoke", logs[0].toString(), logs[1].toString()));
        require(output.contains("WORLDLINE_M12_REPRODUCTION=PASS"), "M12 proof absent");
        String signature = line(output, "evidence.sha256=");
        require(signature.equals(value("expected.signature")), "M12 signature drift: " + signature);
        String evidence = "id=" + ID + "\naero.revision=" + value("aero.revision")
                + "\nseed=" + value("seed") + "\nsaved.world.sha256=" + treeHash(frozenSave)
                + "\n" + output;
        Files.write(build.resolve("evidence.txt"), evidence.getBytes(StandardCharsets.UTF_8));
        System.out.println("Aero reproduction cycle passed");
        System.out.println("  saved world: captured; test BEs do not survive current reload");
        System.out.println("  replay: same seed and fixed camera recreated twice");
        System.out.println("  recurring spike: chunk compilation logical work");
        System.out.println("  minimized evidence: one frame record per capture");
        System.out.println("  evidence: " + root.relativize(build.resolve("evidence.txt")));
    }

    private void capture(Path test, Path log, String ticks) throws Exception {
        Files.deleteIfExists(log);
        String wrapper = System.getProperty("os.name").startsWith("Windows") ? "gradlew.bat" : "gradlew";
        String text = output(test, command(test.resolve(wrapper).toString(), "--no-daemon", "--init-script",
                root.resolve(value("runner")).toString(), "runClient", "-PworldlineTicks=" + ticks,
                "-PworldlineLog=" + log));
        require(text.contains("[WorldlineCapture] ready") && text.contains("[WorldlineCapture] complete")
                && text.contains("BUILD SUCCESSFUL"), "Aero runtime capture failed\n" + text);
    }

    private void verifyCheckout(Path checkout) throws Exception {
        require(Files.isDirectory(checkout.resolve(".git")), "Aero checkout missing");
        require(output(root, command("git", "-C", checkout.toString(), "remote", "get-url", "origin")).trim()
                .equals(value("aero.repository")), "unexpected Aero origin");
        require(output(root, command("git", "-C", checkout.toString(), "rev-parse", "HEAD")).trim()
                .equals(value("aero.revision")), "unexpected Aero revision");
        require(output(root, command("git", "-C", checkout.toString(), "status", "--porcelain")).trim()
                .isEmpty(), "Aero checkout has tracked changes");
    }

    private void copyTree(Path source, Path target) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : paths.collect(Collectors.toList())) {
                Path destination = target.resolve(source.relativize(path));
                if (Files.isDirectory(path)) Files.createDirectories(destination);
                else Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
    }

    private String treeHash(Path directory) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.filter(Files::isRegularFile).sorted().collect(Collectors.toList())) {
                digest.update(directory.relativize(path).toString().replace('\\', '/').getBytes(StandardCharsets.UTF_8));
                digest.update(Files.readAllBytes(path));
            }
        }
        StringBuilder result = new StringBuilder();
        for (byte item : digest.digest()) result.append(String.format("%02x", item & 255));
        return result.toString();
    }

    private void recreate(Path path) throws IOException { delete(path); Files.createDirectories(path); }
    private void delete(Path path) throws IOException {
        if (!Files.exists(path)) return;
        require(path.startsWith(root) && !path.equals(root), "unsafe delete path " + path);
        try (Stream<Path> paths = Files.walk(path)) {
            for (Path item : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(item);
        }
    }

    private void load(Path path) throws IOException {
        try (java.io.Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { config.load(reader); }
    }
    private String value(String key) { String result = config.getProperty(key);
        require(result != null && !result.trim().isEmpty(), "missing smoke property " + key); return result.trim(); }
    private String line(String text, String prefix) { return Arrays.stream(text.split("\\R"))
        .filter(row -> row.startsWith(prefix)).findFirst().orElseThrow(
            () -> new IllegalStateException("missing " + prefix)).substring(prefix.length()); }
    private List<String> command(String... values) { return new ArrayList<>(Arrays.asList(values)); }
    private void run(Path directory, List<String> command) throws Exception { output(directory, command); }
    private String output(Path directory, List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start();
        String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IllegalStateException(command.get(0) + " failed\n" + text);
        return text;
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
