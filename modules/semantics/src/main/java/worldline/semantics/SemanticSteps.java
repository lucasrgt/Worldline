package worldline.semantics;

/**
 * Closed classification of opaque scenario steps onto catalog categories.
 * Unknown steps are kept; only lab/noise prefixes are disposable.
 */
public final class SemanticSteps {
    private SemanticSteps() {}

    public static boolean disposable(String step) {
        String category = category(step);
        return "lab".equals(category) || (step != null && step.startsWith("noise:"));
    }

    public static boolean boundary(String step) {
        String category = category(step);
        return "input".equals(category) || "gui".equals(category) || "tick".equals(category)
                || "world".equals(category) || "rng".equals(category);
    }

    public static String category(String step) {
        if (step == null || step.isEmpty()) throw new IllegalArgumentException("step");
        if (step.equals("tick") || step.startsWith("tick:")) return "tick";
        if (step.startsWith("tap:") || step.startsWith("key:")) return "input";
        if (step.startsWith("click:") || step.startsWith("gui:")) return "gui";
        if (step.startsWith("setBlock") || step.startsWith("block:")) return "world";
        if (step.startsWith("reseed:")) return "rng";
        if (step.startsWith("observe:")) return "lab";
        return "";
    }
}
