import java.nio.file.Path;

/** Prints a canonical campaign plan from one tracked spec and an explicit root seed. */
public final class CampaignPlanQuery {
    private CampaignPlanQuery() { }

    public static void main(String[] arguments) {
        try {
            if (arguments.length != 1) throw new IllegalArgumentException(
                    "usage: Gate.java --campaign-plan CAMPAIGN_ID");
            String seed = System.getenv("WORLDLINE_CAMPAIGN_SEED");
            require(seed != null && !seed.isBlank(), "WORLDLINE_CAMPAIGN_SEED is required");
            long rootSeed;
            try { rootSeed = Long.parseLong(seed); }
            catch (NumberFormatException error) {
                throw new IllegalArgumentException("WORLDLINE_CAMPAIGN_SEED must be a signed integer", error);
            }
            Path root = Path.of("").toAbsolutePath().normalize();
            System.out.print(CampaignSpec.load(root, arguments[0]).plan(rootSeed).envelope());
        } catch (Exception error) {
            System.err.println("campaign plan query failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
