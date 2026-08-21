package worldline.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioDsl;
import worldline.minimization.ScenarioRunner;
import worldline.trace.CanonicalStateDocument;

/** Scenario authoring, validation, and controlled execution commands. */
final class ScenarioCommands {
    private ScenarioCommands() {}

    static int run(String[] arguments, PrintStream output, PrintStream error) throws IOException {
        if (arguments.length >= 3 && "create".equals(arguments[1]))
            return create(arguments[2], Arrays.asList(arguments).subList(3, arguments.length), output);
        if (arguments.length == 3 && "inspect".equals(arguments[1]))
            return inspect(arguments[2], output);
        if (arguments.length == 3 && "validate".equals(arguments[1]))
            return validate(arguments[2], output);
        if (arguments.length == 5 && "run".equals(arguments[1]))
            return run(arguments[2], arguments[3], arguments[4], output);
        return WorldlineCli.usage(error);
    }

    static int create(String path, java.util.List<String> steps, PrintStream output)
            throws IOException {
        Scenario scenario = Scenario.of(steps);
        Files.write(Paths.get(path), scenario.bytes(), StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        output.println("WORLDLINE_SCENARIO_CREATE=PASS");
        output.println("steps=" + scenario.size());
        output.println("scenario.sha256=" + scenario.sha256()); return 0;
    }

    static int inspect(String path, PrintStream output) throws IOException {
        Scenario scenario = read(path);
        output.println("WORLDLINE_SCENARIO_INSPECT=PASS");
        output.println("steps=" + scenario.size());
        output.println("scenario.sha256=" + scenario.sha256());
        for (int index = 0; index < scenario.size(); index++)
            output.println(index + "=" + scenario.step(index));
        return 0;
    }

    static int validate(String path, PrintStream output) throws IOException {
        Scenario scenario = read(path);
        java.util.List<worldline.minimization.ScenarioStep> steps = ScenarioDsl.parseAll(scenario);
        output.println("WORLDLINE_SCENARIO_VALIDATE=PASS");
        output.println("dsl=" + ScenarioDsl.VERSION);
        output.println("steps=" + steps.size());
        for (int index = 0; index < steps.size(); index++)
            output.println(index + "=" + steps.get(index).kind() + ":"
                    + ScenarioDsl.render(steps.get(index)));
        return 0;
    }

    static int run(String path, String seedText, String tracePath, PrintStream output)
            throws IOException {
        Scenario scenario = read(path);
        ScenarioDsl.validate(scenario);
        long seed = Checks.seed(seedText);
        ScenarioRunner runner = Checks.provider("worldline.scenario.provider",
                "worldline.b173.B173ScenarioRunner", ScenarioRunner.class);
        CanonicalStateDocument document = runner.run(scenario, seed);
        Files.write(Paths.get(tracePath), document.canonical().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        output.println("WORLDLINE_SCENARIO_RUN=PASS");
        output.println("steps=" + scenario.size());
        output.println("seed=" + seed);
        output.println("trace.sha256=" + document.signature()); return 0;
    }

    static Scenario read(String path) throws IOException {
        long size = Files.size(Paths.get(path));
        Checks.require(size > 0 && size <= Scenario.MAX_BYTES, "invalid scenario size");
        return Scenario.parse(Files.readAllBytes(Paths.get(path)));
    }
}
