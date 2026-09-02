import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/** Strict common CLI for one exact campaign shard. */
record CampaignOptions(int budgetSeconds, long seed, Integer shardCount,
        int shardIndex, boolean planOnly, Path executorPlan) {
    static CampaignOptions parse(String[] arguments) {
        Integer budget = null, shards = null, index = null; Long seed = null;
        Path executorPlan = null; boolean planOnly = false; Set<String> seen = new HashSet<>();
        for (int cursor = 0; cursor < arguments.length; cursor++) {
            String option = arguments[cursor];
            require(seen.add(option), "duplicate campaign option: " + option);
            switch (option) {
                case "--budget-seconds" -> budget = integer(next(arguments, ++cursor), 10, 7_200, "budget");
                case "--seed" -> seed = signed(next(arguments, ++cursor), "seed");
                case "--shard-count" -> shards = integer(next(arguments, ++cursor), 1, 1_024, "shard count");
                case "--shard-index" -> index = integer(next(arguments, ++cursor), 0, 1_023, "shard index");
                case "--executor-plan" -> executorPlan = path(next(arguments, ++cursor));
                case "--plan-only" -> planOnly = true;
                default -> throw new IllegalArgumentException("unknown campaign option: " + option);
            }
        }
        require(budget != null && seed != null, usage());
        require(shards != null || index == null || index == 0,
                "shard count is required when shard index is nonzero");
        int shardIndex = index == null ? 0 : index;
        if (shards != null) require(shardIndex < shards, "shard index is outside the plan");
        require(executorPlan == null || !planOnly, "executor plan cannot be used with plan-only mode");
        return new CampaignOptions(budget, seed, shards, shardIndex, planOnly, executorPlan);
    }

    static void selfTest() {
        CampaignOptions value = parse(new String[] {"--seed", "7", "--budget-seconds",
                "10", "--shard-count", "3", "--shard-index", "2", "--plan-only"});
        require(value.seed == 7L && value.shardCount == 3 && value.shardIndex == 2
                && value.planOnly && value.executorPlan == null, "campaign option parsing drifted");
        expectFailure(() -> parse(new String[] {"--budget-seconds", "9", "--seed", "7"}));
        expectFailure(() -> parse(new String[] {"--budget-seconds", "10", "--seed", "7",
                "--shard-count", "2", "--shard-index", "2"}));
    }

    static String usage() { return "usage: CAMPAIGN --budget-seconds N --seed N "
            + "[--shard-count N --shard-index N] [--plan-only]"; }
    private static Path path(String value) {
        Path path = Path.of(value).toAbsolutePath().normalize();
        require(path.isAbsolute() && path.toString().equals(value), "executor plan path is not normalized");
        return path;
    }
    private static String next(String[] values, int index) {
        require(index < values.length && !values[index].startsWith("--"), "missing campaign option value");
        return values[index];
    }
    private static int integer(String value, int minimum, int maximum, String label) {
        long parsed = signed(value, label); require(parsed >= minimum && parsed <= maximum,
                label + " must be between " + minimum + " and " + maximum); return (int) parsed;
    }
    private static long signed(String value, String label) {
        try { return Long.parseLong(value); }
        catch (NumberFormatException error) { throw new IllegalArgumentException(label + " must be an integer"); }
    }
    private static void expectFailure(Action action) { try { action.run();
        throw new IllegalStateException("invalid campaign options were accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
    @FunctionalInterface private interface Action { void run(); }
}
