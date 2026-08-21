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
        boolean scenario = (arguments.length >= 3 && arguments[0].equals("scenario")
                && arguments[1].equals("create")) || (arguments.length == 3
                && arguments[0].equals("scenario") && arguments[1].equals("inspect"));
        boolean test = arguments.length >= 1 && arguments[0].equals("test");
        boolean testRuntime = test && !Arrays.asList(arguments).contains("--no-runtime")
                && !Arrays.asList(arguments).contains("list")
                && !Arrays.asList(arguments).contains("inspect")
                && !Arrays.asList(arguments).contains("--help");
        if (!replay && !trace && !mod && !scenario && !test) { System.err.println("usage: java tools/replay/Replay.java replay <bundle.wlrb>");
            System.err.println("   or: java tools/replay/Replay.java trace show <trace.wltrace>");
            System.err.println("   or: java tools/replay/Replay.java trace diff <left.wltrace> <right.wltrace>");
            System.err.println("   or: java tools/replay/Replay.java mod inspect <mod.jar>");
            System.err.println("   or: java tools/replay/Replay.java mod test record <mod.jar> <trace> <result>");
            System.err.println("   or: java tools/replay/Replay.java mod test diff <left> <right>");
            System.err.println("   or: java tools/replay/Replay.java scenario create <output> [step ...]");
            System.err.println("   or: java tools/replay/Replay.java scenario inspect <scenario>");
            System.err.println("   or: java tools/replay/Replay.java test [SpecClass]");
            System.err.println("   or: java tools/replay/Replay.java test run <spec.jar|classes> [SpecClass] [options]"); return 2; }
        if (replay || testRuntime) { int inputs = new ProcessBuilder("java", "tools/harness/RuntimeCheck.java", "--required")
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
        if (test) paths.addAll(Arrays.asList(classes.resolve("testmodel"),
                classes.resolve("testapi"), classes.resolve("testkit")));
        if (replay || testRuntime) paths.addAll(Arrays.asList(classes.resolve("kernel"), client.resolve("adapter-classes"),
                client.resolve("instrumented-client"), client.resolve("headless-classes"),
                workspace.resolve("minecraft/bin"), workspace.resolve("jars/minecraft.jar")));
        for (Path path : paths) if (!Files.exists(path)) throw new IllegalStateException(
                "prepared runtime is missing " + root.relativize(path)
                        + "; run java tools/harness/Verify.java" + (replay || testRuntime ? " --smoke" : ""));
        if (replay || testRuntime) try (Stream<Path> libraries = Files.walk(workspace.resolve("libraries"))) {
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
