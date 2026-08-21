import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Qualifies the v2 mod surface: lifecycle hooks, domain handles, scheduling. */
public final class ModApiCycle {
    private static final String ID = "m11-mod-api";
    private final Path root = Paths.get("").toAbsolutePath().normalize();
    private final Path build = root.resolve(".worldline/smokes").resolve(ID);
    private final Path client = root.resolve(".worldline/smokes/controlled-client-tick");

    public static void main(String[] arguments) {
        if (!Arrays.equals(arguments, new String[] {ID})) {
            System.err.println("usage: java tools/smoke/ModApiCycle.java " + ID); System.exit(2);
        }
        try { new ModApiCycle().execute(); }
        catch (Exception error) { System.err.println("M11 mod api cycle failed: " + error.getMessage()); System.exit(1); }
    }

    private void execute() throws Exception {
        recreate(build);
        Path adapter = client.resolve("adapter-classes");
        require(Files.isDirectory(adapter), "run ClientCycle before ModApiCycle");
        Path smoke = root.resolve("smokes").resolve(ID);
        Path classes = compile(smoke.resolve("src"), build.resolve("classes"),
                Arrays.asList(adapter, product("api"), product("trace"), product("mods")));
        Path modClasses = compile(smoke.resolve("mod-src"), build.resolve("mod-classes"),
                Arrays.asList(adapter, product("api"), classes));
        Path lifecycle = jar("lifecycle", modClasses);
        Path scheduleReject = jar("schedule-reject", modClasses);
        Path spawnReject = jar("spawn-reject", modClasses);
        List<Path> game = gamePath(classes, adapter);
        Result first = process(game, "worldline.smoke.m11.M11ModApiSmoke", "run", lifecycle.toString());
        Result second = process(game, "worldline.smoke.m11.M11ModApiSmoke", "run", lifecycle.toString());
        require(first.code == 0 && first.text.equals(second.text), "lifecycle run is not deterministic");
        Result schedule = process(game, "worldline.smoke.m11.M11ModApiSmoke",
                "reject-schedule", scheduleReject.toString());
        Result spawn = process(game, "worldline.smoke.m11.M11ModApiSmoke",
                "reject-spawn", spawnReject.toString());
        require(schedule.code == 0 && schedule.text.contains("WORLDLINE_M11_REJECT=SCHEDULE"),
                "past scheduling was not rejected");
        require(spawn.code == 0 && spawn.text.contains("WORLDLINE_M11_REJECT=SPAWN"),
                "unknown spawn type was not rejected");
        String signature = line(first.text, "WORLDLINE_M11_SIGNATURE=");
        String report = "mod=worldline.lifecycle-probe@1.0.0"
                + "\nsignature=" + signature
                + "\nrejections=SCHEDULE,SPAWN\n";
        String evidence = sha256(report);
        Properties expected = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(smoke.resolve("smoke.properties"))) {
            expected.load(reader);
        }
        require(evidence.equals(expected.getProperty("expected.signature")),
                "M11 evidence diverged: " + evidence);
        Files.write(build.resolve("evidence.txt"), report.getBytes(StandardCharsets.UTF_8));
        System.out.println("M11 mod api cycle passed");
        System.out.println("  onLoad setBlock/give/spawn + scheduled tick 3 action verified");
        System.out.println("  remove, container census, onDispose verified");
        System.out.println("  rejected: past scheduling, unknown spawn type");
        System.out.println("  evidence SHA-256: " + evidence);
    }

    private Path jar(String name, Path classes) throws Exception {
        Path jarPath = build.resolve(name + ".jar");
        run(Arrays.asList("jar", "--create", "--file", jarPath.toString(), "-C", classes.toString(), ".",
                "-C", root.resolve("smokes").resolve(ID).resolve("descriptors").resolve(name).toString(),
                "META-INF/worldline-mod.properties"));
        return jarPath;
    }

    private List<Path> gamePath(Path scenario, Path adapter) throws Exception {
        Path workspace = root.resolve("local/workspaces/b1.7.3");
        List<Path> result = new ArrayList<>(Arrays.asList(scenario, client.resolve("instrumented-client"),
                adapter, client.resolve("headless-classes"), product("api"), product("trace"),
                product("kernel"), product("mods"), workspace.resolve("minecraft/bin"),
                workspace.resolve("jars/minecraft.jar")));
        try (Stream<Path> paths = Files.walk(workspace.resolve("libraries"))) {
            result.addAll(paths.filter(path -> path.toString().endsWith(".jar")).sorted()
                    .collect(Collectors.toList()));
        }
        return result;
    }

    private Path compile(Path source, Path output, List<Path> dependencies) throws Exception {
        Files.createDirectories(output);
        List<String> command = new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8",
                "--release", "8", "-Xlint:all,-options", "-Werror", "-classpath",
                classpath(dependencies), "-d", output.toString()));
        try (Stream<Path> paths = Files.walk(source)) {
            paths.filter(path -> path.toString().endsWith(".java")).sorted()
                    .forEach(path -> command.add(path.toString()));
        }
        run(command); return output;
    }

    private Result process(List<Path> paths, String type, String... arguments) throws Exception {
        List<String> command = new ArrayList<>(Arrays.asList("java", "-Djava.awt.headless=true",
                "-classpath", classpath(paths), type));
        command.addAll(Arrays.asList(arguments));
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new Result(process.waitFor(), output);
    }

    private void run(List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(root.toFile())
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int code = process.waitFor();
        if (code != 0) throw new IllegalStateException(command.get(0) + " failed\n" + output);
    }

    private void recreate(Path target) throws Exception {
        if (Files.exists(target)) {
            try (Stream<Path> paths = Files.walk(target)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder())
                        .collect(Collectors.toList())) Files.delete(path);
            }
        }
        Files.createDirectories(target);
    }

    private String classpath(List<Path> paths) { return paths.stream().map(Path::toString)
            .collect(Collectors.joining(System.getProperty("path.separator"))); }
    private Path product(String name) { return root.resolve(".worldline/build/classes").resolve(name); }
    private String line(String text, String prefix) { return text.lines().filter(row -> row.startsWith(prefix))
            .findFirst().orElseThrow(() -> new IllegalStateException("missing " + prefix)).substring(prefix.length()); }
    private String sha256(String text) throws Exception { byte[] hash = MessageDigest.getInstance("SHA-256")
            .digest(text.getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
            for (byte value : hash) result.append(String.format("%02x", value & 255)); return result.toString(); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }
    private static final class Result { final int code; final String text;
        Result(int code, String text) { this.code = code; this.text = text; } }
}
