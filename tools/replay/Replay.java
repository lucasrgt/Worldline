import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Repository launcher for the stable Worldline replay, trace, and mod CLI. */
public final class Replay {
    private final Path root = Paths.get("").toAbsolutePath().normalize();

    public static void main(String[] arguments) {
        try { System.exit(new Replay().execute(arguments)); }
        catch (Exception error) { System.err.println("worldline launcher failed: " + error.getMessage());
            System.exit(1); }
    }

    private int execute(String[] arguments) throws Exception {
        boolean replay = arguments.length == 2 && arguments[0].equals("replay");
        boolean trace = (arguments.length == 3 && arguments[0].equals("trace")
                && arguments[1].equals("show")) || (arguments.length == 4
                && arguments[0].equals("trace") && arguments[1].equals("diff"));
        boolean mod = (arguments.length == 3 && arguments[0].equals("mod")
                && arguments[1].equals("inspect")) || (arguments.length == 6
                && arguments[0].equals("mod") && arguments[1].equals("test")
                && arguments[2].equals("record")) || (arguments.length == 5
                && arguments[0].equals("mod") && arguments[1].equals("test")
                && arguments[2].equals("diff"));
        boolean modRun = arguments.length == 7 && arguments[0].equals("mod")
                && arguments[1].equals("test") && arguments[2].equals("run");
        boolean scenario = (arguments.length >= 3 && arguments[0].equals("scenario")
                && arguments[1].equals("create")) || (arguments.length == 3
                && arguments[0].equals("scenario") && arguments[1].equals("inspect"))
                || (arguments.length == 3 && arguments[0].equals("scenario")
                && arguments[1].equals("validate"));
        boolean scenarioRun = arguments.length == 5 && arguments[0].equals("scenario")
                && arguments[1].equals("run");
        boolean fuzz = arguments.length >= 5 && arguments.length <= 7
                && arguments[0].equals("fuzz");
        boolean debug = arguments.length == 3 && arguments[0].equals("debug");
        boolean profile = (arguments.length == 3 || arguments.length == 4)
                && arguments[0].equals("profile");
        boolean game = replay || modRun || scenarioRun || fuzz || debug || profile;
        if (!replay && !trace && !mod && !scenario && !modRun && !scenarioRun && !fuzz
                && !debug && !profile) {
            System.err.println("usage: java tools/replay/Replay.java replay <bundle.wlrb>");
            System.err.println("   or: java tools/replay/Replay.java trace show <trace.wltrace>");
            System.err.println("   or: java tools/replay/Replay.java trace diff <left.wltrace> <right.wltrace>");
            System.err.println("   or: java tools/replay/Replay.java mod inspect <mod.jar>");
            System.err.println("   or: java tools/replay/Replay.java mod test record <mod.jar> <trace> <result>");
            System.err.println("   or: java tools/replay/Replay.java mod test diff <left> <right>");
            System.err.println("   or: java tools/replay/Replay.java mod test run <mod.jar> <seed> <ticks> <result>");
            System.err.println("   or: java tools/replay/Replay.java scenario create <output> [step ...]");
            System.err.println("   or: java tools/replay/Replay.java scenario inspect <scenario>");
            System.err.println("   or: java tools/replay/Replay.java scenario validate <scenario>");
            System.err.println("   or: java tools/replay/Replay.java scenario run <scenario> <seed> <trace>");
            System.err.println("   or: java tools/replay/Replay.java fuzz <out-dir> <seed> <cases> <steps> [left.jar] [right.jar]");
            System.err.println("   or: java tools/replay/Replay.java debug <scenario> <seed>");
            System.err.println("   or: java tools/replay/Replay.java profile <scenario> <seed> [budget.properties]");
            return 2; }
        if (game) { int inputs = new ProcessBuilder("java", "tools/harness/RuntimeCheck.java", "--required")
                .directory(root.toFile()).inheritIO().start().waitFor(); if (inputs != 0) return inputs; }
        Path classes = root.resolve(".worldline/build/classes");
        Path client = root.resolve(".worldline/smokes/controlled-client-tick");
        Path workspace = root.resolve("local/workspaces/b1.7.3");
        List<Path> paths = new ArrayList<>(Arrays.asList(classes.resolve("cli"),
                classes.resolve("reproduction"), classes.resolve("api"), classes.resolve("invariants"),
                classes.resolve("semantics"),
                classes.resolve("trace"), classes.resolve("mods"), classes.resolve("analysis"),
                classes.resolve("modtest")));
        paths.add(classes.resolve("minimization"));
        paths.add(classes.resolve("fuzz"));
        paths.add(classes.resolve("profiling"));
        if (game) paths.addAll(Arrays.asList(classes.resolve("kernel"), client.resolve("adapter-classes"),
                client.resolve("instrumented-client"), client.resolve("headless-classes"),
                workspace.resolve("minecraft/bin"), workspace.resolve("jars/minecraft.jar")));
        for (Path path : paths) if (!Files.exists(path)) throw new IllegalStateException(
                "prepared runtime is missing " + root.relativize(path)
                        + "; run java tools/harness/Verify.java --smoke");
        if (game) try (Stream<Path> libraries = Files.walk(workspace.resolve("libraries"))) {
            paths.addAll(libraries.filter(path -> path.toString().endsWith(".jar"))
                    .sorted().collect(Collectors.toList()));
        }
        List<String> command = new ArrayList<>(Arrays.asList("java", "-Djava.awt.headless=true",
                "-classpath", paths.stream().map(Path::toString).collect(Collectors.joining(
                        System.getProperty("path.separator"))), "worldline.cli.WorldlineCli"));
        command.addAll(Arrays.asList(arguments));
        return new ProcessBuilder(command).directory(root.toFile()).inheritIO().start().waitFor();
    }
}
