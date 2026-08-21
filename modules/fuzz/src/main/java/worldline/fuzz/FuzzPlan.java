package worldline.fuzz;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioStep;

/**
 * Deterministic generator of bounded public-grammar scenarios. The same
 * (seed, cases, maxSteps) triple always yields the same plan.
 */
public final class FuzzPlan {
    private static final String[] LABELS = {"before", "target", "after", "probe", "edge"};
    private final List<Scenario> scenarios;

    FuzzPlan(List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    public static FuzzPlan generate(long seed, int cases, int maxSteps) {
        if (cases < 1 || cases > 4096) throw new IllegalArgumentException("invalid case count");
        if (maxSteps < 1 || maxSteps > Scenario.MAX_STEPS) {
            throw new IllegalArgumentException("invalid max steps");
        }
        Random random = new Random(seed);
        List<Scenario> generated = new ArrayList<>(cases);
        for (int index = 0; index < cases; index++) {
            generated.add(Scenario.of(steps(random, 1 + random.nextInt(maxSteps))));
        }
        return new FuzzPlan(generated);
    }

    public List<Scenario> scenarios() { return scenarios; }

    public int size() { return scenarios.size(); }

    private static List<String> steps(Random random, int count) {
        List<String> steps = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            steps.add(step(random, steps));
        }
        return steps;
    }

    private static String step(Random random, List<String> steps) {
        switch (random.nextInt(5)) {
            case 0: return "tick:" + (1 + random.nextInt(4));
            case 1: return "reseed:" + random.nextInt();
            case 2: return "tap:" + random.nextInt(ScenarioStep.MAX_KEY + 1);
            case 3: return "observe:" + LABELS[random.nextInt(LABELS.length)];
            default:
                for (String existing : steps) {
                    if (existing.startsWith("observe:")) return block(random);
                }
                return "observe:" + LABELS[0];
        }
    }

    /** Always-registered b1.7.3 block ids; unregistered ids fail closed in adapters. */
    private static final int[] BLOCK_IDS = {1, 3, 17, 20};

    private static String block(Random random) {
        return "block:" + (random.nextInt(17) - 8) + "," + (60 + random.nextInt(9)) + ","
                + (random.nextInt(17) - 8) + ":" + BLOCK_IDS[random.nextInt(BLOCK_IDS.length)]
                + (random.nextInt(4) == 0 ? ":" + random.nextInt(16) : "");
    }
}
