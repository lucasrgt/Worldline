package worldline.cli;

import java.io.PrintStream;
import java.util.Map;
import worldline.api.WorldlineBehavior;

/** Lists the stable public behavior identities available to TestKit authors. */
final class BehaviorsCommand {
    private BehaviorsCommand() { }

    static int run(String[] arguments, PrintStream output, PrintStream error) {
        if (arguments.length != 2 || !"list".equals(arguments[1])) {
            error.println("usage: worldline behaviors list");
            return 2;
        }
        Map<String, WorldlineBehavior> catalog = WorldlineBehavior.all();
        output.println("WORLDLINE_BEHAVIORS=PASS");
        output.println("count=" + catalog.size());
        output.println("token\tfamily\tatlas\tsubject");
        for (WorldlineBehavior behavior : catalog.values()) {
            output.println(behavior.token() + "\t" + behavior.family() + "\t"
                    + behavior.atlasId() + "\t" + behavior.subject());
        }
        return 0;
    }
}
