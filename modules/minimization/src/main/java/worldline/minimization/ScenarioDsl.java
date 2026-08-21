package worldline.minimization;

import java.util.ArrayList;
import java.util.List;

/**
 * Public grammar for scenario steps. Parsing is strict and fail closed;
 * rendering is canonical so one step has exactly one spelling.
 */
public final class ScenarioDsl {
    /** Canonical spellings accepted for every verb. */
    public static final String VERSION = "worldline-scenario-dsl/1";

    private ScenarioDsl() {}

    public static ScenarioStep parse(String step) {
        if (step == null) throw new NullPointerException("step");
        if (step.equals("tick")) return ScenarioStep.tick(1);
        if (step.startsWith("tick:")) return ScenarioStep.tick(number(step.substring(5)));
        if (step.startsWith("reseed:") && ScenarioStep.number(body(step)))
            return ScenarioStep.reseed(Long.parseLong(body(step)));
        if (step.startsWith("tap:")) return ScenarioStep.tap(number(body(step)));
        if (step.startsWith("observe:")) return ScenarioStep.observe(body(step));
        if (step.startsWith("block:")) return block(step);
        throw new IllegalArgumentException("unknown scenario step: " + step);
    }

    public static String render(ScenarioStep step) {
        switch (step.kind()) {
            case TICK: return step.count() == 1 ? "tick" : "tick:" + step.count();
            case RESEED: return "reseed:" + step.seed();
            case TAP: return "tap:" + step.key();
            case OBSERVE: return "observe:" + step.label();
            case BLOCK: return "block:" + step.x() + "," + step.y() + "," + step.z()
                    + ":" + step.blockId() + ":" + step.metadata();
            default: throw new IllegalStateException("unrenderable scenario step");
        }
    }

    public static List<ScenarioStep> parseAll(Scenario scenario) {
        if (scenario == null) throw new NullPointerException("scenario");
        List<ScenarioStep> steps = new ArrayList<>(scenario.size());
        for (int index = 0; index < scenario.size(); index++) steps.add(parse(scenario.step(index)));
        return steps;
    }

    /** Rejects any scenario containing a step outside the public grammar. */
    public static void validate(Scenario scenario) {
        parseAll(scenario);
    }

    private static ScenarioStep block(String step) {
        String[] fields = step.substring(6).split(":", -1);
        require(fields.length == 2 || fields.length == 3, "invalid block step");
        String[] coordinates = fields[0].split(",", -1);
        require(coordinates.length == 3, "invalid block coordinates");
        int metadata = fields.length == 3 ? number(fields[2]) : 0;
        return ScenarioStep.block(number(coordinates[0]), number(coordinates[1]),
                number(coordinates[2]), number(fields[1]), metadata);
    }

    private static String body(String step) { return step.substring(step.indexOf(':') + 1); }

    private static int number(String text) {
        require(ScenarioStep.number(text), "invalid integer: " + text);
        return Integer.parseInt(text);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
