import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Executes one declarative, deterministic official-server smoke cycle twice. */
public final class DataDrivenCycle {
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final String id;
    private final Path smoke, build;
    private final Properties config = new Properties(), artifact = new Properties();
    private DataDrivenCyclePlan plan;

    private DataDrivenCycle(String id) {
        this.id = id; smoke = root.resolve("smokes").resolve(id);
        build = root.resolve(".worldline/smokes").resolve(id);
    }

    public static void main(String[] arguments) {
        if (arguments.length != 1) {
            System.err.println("usage: DataDrivenCycle ID"); System.exit(2);
        }
        try { new DataDrivenCycle(arguments[0]).execute(); }
        catch (Exception error) {
            System.err.println(arguments[0] + " data-driven cycle failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private void execute() throws Exception {
        plan = DataDrivenCyclePlan.load(root, id);
        DataDrivenSupport.load(smoke.resolve("smoke.properties"), config);
        DataDrivenSupport.load(root.resolve(plan.artifact), artifact);
        require(value("server.jar.sha256").equals(DataDrivenSupport.value(artifact, "expected.sha256")),
                "official artifact descriptor drift");
        Path official = root.resolve(DataDrivenSupport.value(artifact, "local.path")).normalize();
        DataDrivenSupport.verifyArtifact(official, artifact); DataDrivenSupport.recreate(root, build);
        Path classes = compile();
        Outcome first = run(classes, official, build.resolve("first"));
        Outcome second = run(classes, official, build.resolve("second"));
        require(first.signal.equals(second.signal) && first.trace.equals(second.trace)
                        && first.signature.equals(second.signature), "fresh cycle results diverged");
        require(first.signal.equals(value("expected.signal")), "frozen signal drift");
        require(first.signature.equals(value("expected.signature")), "frozen signature drift");
        assertContains(first.signal, plan.signalContains, "signal");
        assertExcludes(first.signal, plan.signalExcludes, "signal");
        assertContains(first.trace, plan.traceContains, "trace");
        assertExcludes(first.trace, plan.traceExcludes, "trace");
        String evidence = "id=" + id + "\nruns=2\nfirst=" + first.signal + "\nsecond="
                + second.signal + "\ntrace=" + first.trace + "\nsignature=" + first.signature + "\n";
        Files.writeString(build.resolve("evidence.txt"), evidence, StandardCharsets.UTF_8);
        System.out.println(id + " data-driven cycle passed");
        System.out.println("  " + first.signal); System.out.println("  trace: " + first.trace);
        System.out.println("  signature: " + first.signature); System.out.println("FROZEN");
    }

    private Path compile() throws Exception {
        Path output = build.resolve("adapter-classes"); Files.createDirectories(output);
        List<String> command = new ArrayList<>(Arrays.asList("javac", "-encoding", "UTF-8",
                "--release", "8", "-Xlint:all,-options", "-Werror"));
        String classpath = classpath(plan.compileProducts, null);
        if (!classpath.isEmpty()) command.addAll(List.of("-classpath", classpath));
        command.addAll(List.of("-d", output.toString()));
        for (String input : plan.inputs) command.addAll(DataDrivenSupport.javaFiles(root.resolve(input)));
        command.addAll(DataDrivenSupport.javaFiles(smoke.resolve("src")));
        DataDrivenSupport.capture(root, command); return output;
    }

    private Outcome run(Path classes, Path official, Path workspace) throws Exception {
        return SmokeRetry.onceOnEof(id, attempt -> {
            DataDrivenSupport.recreate(root, workspace);
            List<String> command = new ArrayList<>(List.of("java", "-classpath",
                    classpath(plan.runtimeProducts, classes), plan.mainClass, official.toString(),
                    workspace.toString(), Integer.toString(DataDrivenSupport.freePort())));
            for (String key : plan.arguments) command.add(value(key));
            String output = DataDrivenSupport.capture(root, command);
            assertContains(output, plan.outputContains, "process output");
            return new Outcome(DataDrivenSupport.line(output, plan.tracePrefix),
                    DataDrivenSupport.line(output, plan.signaturePrefix),
                    DataDrivenSupport.line(output, plan.signalPrefix));
        });
    }

    private String classpath(List<String> products, Path classes) {
        List<String> values = new ArrayList<>(); if (classes != null) values.add(classes.toString());
        for (String product : products) values.add(DataDrivenSupport.product(root, product).toString());
        return String.join(System.getProperty("path.separator"), values);
    }

    private String value(String key) { return DataDrivenSupport.value(config, key); }
    private static void assertContains(String value, List<String> fragments, String label) {
        for (String fragment : fragments) require(value.contains(fragment),
                label + " lacks " + fragment);
    }
    private static void assertExcludes(String value, List<String> fragments, String label) {
        for (String fragment : fragments) require(!value.contains(fragment),
                label + " unexpectedly contains " + fragment);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    private record Outcome(String trace, String signature, String signal) { }
}
