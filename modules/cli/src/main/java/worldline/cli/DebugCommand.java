package worldline.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import worldline.minimization.Scenario;
import worldline.minimization.ScenarioDsl;
import worldline.minimization.ScenarioTimeTravel;
import worldline.trace.CanonicalStateDocument;

/**
 * Interactive time-travel over one public-grammar scenario: forward stepping,
 * deterministic reverse jumps, and watchpoints on trace fields.
 */
final class DebugCommand {
    private DebugCommand() {}

    static int run(String[] arguments, PrintStream output, PrintStream error)
            throws IOException {
        if (arguments.length != 3 || !"debug".equals(arguments[0])) {
            return WorldlineCli.usage(error);
        }
        Scenario scenario = ScenarioCommands.read(arguments[1]);
        ScenarioDsl.validate(scenario);
        long seed = Checks.seed(arguments[2]);
        ScenarioTimeTravel travel = Checks.provider("worldline.scenario.provider",
                "worldline.b173.B173ScenarioRunner", ScenarioTimeTravel.class);
        new Session(scenario, seed, travel, output).repl(System.in);
        return 0;
    }

    private static final class Session {
        private final Scenario scenario;
        private final long seed;
        private final ScenarioTimeTravel travel;
        private final PrintStream out;
        private int position;
        private CanonicalStateDocument frame;
        private String watched;
        private String watchedValue;

        Session(Scenario scenario, long seed, ScenarioTimeTravel travel, PrintStream out) {
            this.scenario = scenario; this.seed = seed; this.travel = travel; this.out = out;
            move(0);
        }

        void repl(InputStream input) throws IOException {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8));
            status();
            for (String line; (line = reader.readLine()) != null;) {
                if (!command(line.trim())) return;
                status();
            }
        }

        /** Returns false when the session should end. */
        private boolean command(String line) {
            out.println("WORLDLINE_DEBUG_CMD=" + line);
            if (line.isEmpty()) return true;
            String[] parts = line.split("\\s+", -1);
            switch (parts[0]) {
                case "quit": case "exit": return false;
                case "step": move(position + count(parts)); return true;
                case "back": move(position - count(parts)); return true;
                case "goto":
                    Checks.require(parts.length == 2, "goto requires a step index");
                    move(numeric(parts[1])); return true;
                case "watch":
                    Checks.require(parts.length == 2, "watch requires a trace field");
                    requireField(parts[1]);
                    watched = parts[1]; watchedValue = value(watched);
                    out.println("WORLDLINE_DEBUG_WATCH=" + watched + "=" + watchedValue);
                    return true;
                case "unwatch":
                    watched = null; watchedValue = null;
                    out.println("WORLDLINE_DEBUG_WATCH=off"); return true;
                case "observe":
                    dump(); return true;
                case "scenario":
                    out.println("WORLDLINE_DEBUG_STEPS=" + scenario.size());
                    for (int index = 0; index < scenario.size(); index++) {
                        out.println(index + "=" + scenario.step(index));
                    }
                    return true;
                default:
                    out.println("WORLDLINE_DEBUG_ERROR=unknown command " + parts[0]);
                    return true;
            }
        }

        private void move(int target) {
            int clamped = Math.max(0, Math.min(scenario.size(), target));
            frame = travel.prefix(scenario, seed, clamped);
            boolean moved = clamped != position;
            position = clamped;
            reportWatch(moved);
        }

        private void reportWatch(boolean moved) {
            if (watched == null) return;
            String current = value(watched);
            if (!current.equals(watchedValue)) {
                out.println("WORLDLINE_DEBUG_TRIGGER=" + watched + ":"
                        + watchedValue + "->" + current + "@" + lastLabel());
                watchedValue = current;
            } else if (moved) {
                out.println("WORLDLINE_DEBUG_UNCHANGED=" + watched + "=" + current);
            }
        }

        private void dump() {
            if (frame.records().isEmpty()) {
                out.println("WORLDLINE_DEBUG_OBSERVE=empty");
                return;
            }
            CanonicalStateDocument.Record last =
                    frame.records().get(frame.records().size() - 1);
            StringBuilder row = new StringBuilder("WORLDLINE_DEBUG_OBSERVE=" + last.label());
            for (int index = 0; index < frame.fields().size(); index++) {
                row.append(' ').append(frame.fields().get(index))
                        .append('=').append(last.value(index));
            }
            out.println(row);
        }

        private void status() {
            out.println("WORLDLINE_DEBUG_POS=" + position + "/" + scenario.size()
                    + (watched == null ? "" : " watch=" + watched));
        }

        private String value(String field) {
            int index = frame.fields().indexOf(field);
            Checks.require(index >= 0, "unknown trace field: " + field);
            if (frame.records().isEmpty()) return "none";
            return Long.toString(frame.records().get(frame.records().size() - 1).value(index));
        }

        private String lastLabel() {
            return frame.records().isEmpty() ? "-"
                    : frame.records().get(frame.records().size() - 1).label();
        }

        private void requireField(String field) {
            Checks.require(frame.fields().contains(field),
                    "unknown trace field: " + field);
        }

        private static int count(String[] parts) {
            if (parts.length == 1) return 1;
            return numeric(parts[1]);
        }

        private static int numeric(String text) {
            Checks.require(text.matches("[0-9]{1,4}"), "invalid step number: " + text);
            return Integer.parseInt(text);
        }
    }
}
