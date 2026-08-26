import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Fail-closed entry point for milestone census, resolution, and worker preflight. */
public final class SwarmLoop {
    private SwarmLoop() { }

    public static void main(String[] arguments) {
        try {
            require(arguments.length > 0, usage());
            switch (arguments[0]) {
                case "audit" -> audit(arguments);
                case "resolve" -> resolve(arguments);
                case "report" -> report(arguments);
                case "close-wave" -> closeWave(arguments);
                case "plan-micro-wave" -> planMicroWave(arguments);
                case "preflight" -> preflight(arguments);
                case "pre-candidate" -> preCandidate(arguments);
                case "--self-test" -> selfTest(arguments);
                default -> throw new IllegalArgumentException(usage());
            }
        } catch (Exception error) {
            System.err.println("swarm loop failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void audit(String[] arguments) throws Exception {
        Path output = Path.of(".worldline/reports/swarm-census.json");
        Path archive = null;
        List<SwarmCensus.Wave> waves = new ArrayList<>();
        for (int index = 1; index < arguments.length; index++) {
            require(index + 1 < arguments.length, usage());
            switch (arguments[index]) {
                case "--wave" -> waves.add(SwarmCensus.Wave.parse(arguments[++index]));
                case "--output" -> output = Path.of(arguments[++index]);
                case "--archive" -> archive = Path.of(arguments[++index]);
                default -> throw new IllegalArgumentException(usage());
            }
        }
        require(!waves.isEmpty(), "audit requires at least one --wave PATH=BASE");
        SwarmCensus.audit(waves, output, archive);
    }

    private static void resolve(String[] arguments) throws Exception {
        Path census = null;
        String scar = "", rejected = "";
        int attempts = 2;
        for (int index = 1; index < arguments.length; index++) {
            require(index + 1 < arguments.length, usage());
            switch (arguments[index]) {
                case "--census" -> census = Path.of(arguments[++index]);
                case "--scar" -> scar = arguments[++index];
                case "--reject" -> rejected = arguments[++index];
                case "--max-attempts" -> attempts = Integer.parseInt(arguments[++index]);
                default -> throw new IllegalArgumentException(usage());
            }
        }
        require(census != null && !scar.isBlank(), usage());
        SwarmResolution.write(census, scar, rejected, attempts);
    }

    private static void preflight(String[] arguments) throws Exception {
        String id = "", base = "", goal = "";
        Path census = null, closure = null, microWave = null;
        for (int index = 1; index < arguments.length; index++) {
            require(index + 1 < arguments.length, usage());
            switch (arguments[index]) {
                case "--id" -> id = arguments[++index];
                case "--base" -> base = arguments[++index];
                case "--goal" -> goal = arguments[++index];
                case "--census" -> census = Path.of(arguments[++index]);
                case "--closure" -> closure = Path.of(arguments[++index]);
                case "--micro-wave" -> microWave = Path.of(arguments[++index]);
                default -> throw new IllegalArgumentException(usage());
            }
        }
        require(!id.isBlank() && !base.isBlank() && !goal.isBlank() && census != null
                && closure != null && microWave != null, usage());
        SwarmPreflight.run(id, base, goal, census, closure, microWave);
    }

    private static void preCandidate(String[] arguments) throws Exception {
        String id = "", base = "", goal = "";
        for (int index = 1; index < arguments.length; index++) {
            require(index + 1 < arguments.length, usage());
            switch (arguments[index]) {
                case "--id" -> id = arguments[++index];
                case "--base" -> base = arguments[++index];
                case "--goal" -> goal = arguments[++index];
                default -> throw new IllegalArgumentException(usage());
            }
        }
        require(!id.isBlank() && !base.isBlank() && !goal.isBlank(), usage());
        SwarmPreCandidate.run(id, base, goal);
    }

    private static void report(String[] arguments) throws Exception {
        Path census = null, resolution = null;
        Path output = Path.of(".worldline/reports/swarm-wave.json");
        for (int index = 1; index < arguments.length; index++) {
            require(index + 1 < arguments.length, usage());
            switch (arguments[index]) {
                case "--census" -> census = Path.of(arguments[++index]);
                case "--resolution" -> resolution = Path.of(arguments[++index]);
                case "--output" -> output = Path.of(arguments[++index]);
                default -> throw new IllegalArgumentException(usage());
            }
        }
        require(census != null && resolution != null, usage());
        SwarmWaveReport.write(census, resolution, output);
    }

    private static void closeWave(String[] arguments) throws Exception {
        Path census = null, previous = null, evidence = null;
        Path output = Path.of(".worldline/reports/swarm/wave-self-improvement.json");
        String base = "", correction = "";
        for (int index = 1; index < arguments.length; index++) {
            require(index + 1 < arguments.length, usage());
            switch (arguments[index]) {
                case "--census" -> census = Path.of(arguments[++index]);
                case "--previous-census" -> previous = Path.of(arguments[++index]);
                case "--evidence-root" -> evidence = Path.of(arguments[++index]);
                case "--base" -> base = arguments[++index];
                case "--process-correction" -> correction = arguments[++index];
                case "--output" -> output = Path.of(arguments[++index]);
                default -> throw new IllegalArgumentException(usage());
            }
        }
        require(census != null && evidence != null && !base.isBlank(), usage());
        WaveSelfImprovement.close(census, previous, output, evidence, base, correction);
    }

    private static void planMicroWave(String[] arguments) throws Exception {
        Path census = null, closure = null;
        Path output = null;
        String base = ""; List<String> ids = new ArrayList<>();
        for (int index = 1; index < arguments.length; index++) {
            require(index + 1 < arguments.length, usage());
            switch (arguments[index]) {
                case "--census" -> census = Path.of(arguments[++index]);
                case "--closure" -> closure = Path.of(arguments[++index]);
                case "--base" -> base = arguments[++index];
                case "--id" -> ids.add(arguments[++index]);
                case "--output" -> output = Path.of(arguments[++index]);
                default -> throw new IllegalArgumentException(usage());
            }
        }
        require(census != null && closure != null && !base.isBlank(), usage());
        SwarmMicroWave.plan(closure, census, output, base, ids);
    }

    private static void selfTest(String[] arguments) {
        require(arguments.length == 1, usage());
        require(SwarmCensus.legacyState(true, false, false, false).equals("DIRTY_SUSPENDED"),
                "dirty classification drifted");
        require(SwarmCensus.legacyState(false, true, true, true).equals("QUALIFIED"),
                "qualified classification drifted");
        require(SwarmCensus.legacyState(false, false, false, true).equals("FAILED_GATE"),
                "failed classification drifted");
        require(SwarmCensus.legacyState(false, false, true, false).equals("NOT_STARTED"),
                "not-started classification drifted");
        WaveSelfImprovement.selfTest();
        WaveCensus.selfTest();
        SwarmMicroWave.selfTest();
        System.out.println("swarm loop self-test passed");
    }

    private static String usage() {
        return "usage: SwarmLoop.java audit --wave PATH=BASE... [--output PATH] [--archive DIR] | "
                + "resolve --census PATH --scar ID [--reject ID,...] [--max-attempts N] | "
                + "report --census PATH --resolution PATH [--output PATH] | "
                + "close-wave --census PATH [--previous-census PATH] --evidence-root DIR "
                + "--base SHA [--process-correction SHA] [--output PATH] | "
                + "plan-micro-wave --census PATH --closure PATH --base SHA --id ID... "
                + "[--output CANONICAL_PATH] | preflight --id ID --base SHA --goal TEXT --census PATH "
                + "--closure PATH --micro-wave PATH | "
                + "pre-candidate --id ID --base SHA --goal TEXT | --self-test";
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
