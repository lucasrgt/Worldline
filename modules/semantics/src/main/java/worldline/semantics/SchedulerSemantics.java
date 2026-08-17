package worldline.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import worldline.api.SemanticMapping;

/**
 * Scheduler-category mappings for the b1.7.3 semantic catalog. Roles cover the
 * timer thread control surface, the task scheduler type, and scheduler advance.
 */
final class SchedulerSemantics {
    private SchedulerSemantics() {}

    static List<SemanticMapping> mappings() {
        List<SemanticMapping> mappings = new ArrayList<SemanticMapping>();
        mappings.add(SemanticMapping.of("scheduler", "TIMER_THREAD",
                "worldline/b173/B173ThreadControl", "class", "B173ThreadControl", "-",
                "", "SCHEDULER", "SCHEDULER", "lab-cycle", "", 9920));
        mappings.add(SemanticMapping.of("scheduler", "TASK_SCHEDULER",
                "worldline/b173/B173Scheduler", "class", "B173Scheduler", "-",
                "CLOCK", "SCHEDULER", "CLOCK,SCHEDULER", "lab-cycle", "", 9990));
        mappings.add(SemanticMapping.of("scheduler", "SCHEDULER_ADVANCE",
                "worldline/b173/B173Scheduler", "method", "advance", "()V",
                "CLOCK", "SCHEDULER", "CLOCK,SCHEDULER", "lab-cycle", "", 9990));
        return Collections.unmodifiableList(mappings);
    }
}
