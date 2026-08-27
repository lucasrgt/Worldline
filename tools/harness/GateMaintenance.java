/** Maps explicit Gate maintenance options to bounded harness entry points. */
final class GateMaintenance {
    private GateMaintenance() { }

    public static void main(String[] arguments) {
        try {
            if (arguments.length != 1 || spec(arguments[0]) == null)
                throw new IllegalArgumentException("unknown Gate maintenance option");
            String[] command = spec(arguments[0]);
            Class.forName(command[0]).getMethod("main", String[].class)
                    .invoke(null, (Object) new String[] {command[1]});
        } catch (java.lang.reflect.InvocationTargetException error) {
            Throwable cause = error.getCause();
            System.err.println("Gate maintenance failed: " + cause.getMessage()); System.exit(1);
        } catch (Exception error) {
            System.err.println("Gate maintenance failed: " + error.getMessage()); System.exit(1);
        }
    }

    static String[] spec(String value) { return switch (value) {
        case "--migrate-data-cycles" -> command("DataDrivenCycleMigration", "--apply", 300);
        case "--refresh-data-cycle-pins" -> command("DataDrivenCycleMigration", "--refresh", 300);
        case "--migrate-composite-cycles" -> command("CompositeCycleMigration", "--apply", 300);
        case "--refresh-composite-cycle-pins" -> command("CompositeCycleMigration", "--refresh", 300);
        case "--migrate-telemetry-pins" -> command("TelemetryPinMigration", "--apply", 300);
        case "--migrate-repository-schemas" -> command("RepositorySchemaMigration", "--apply", 600);
        case "--migrate-neighbor-testkit-pins" -> command("NeighborTestKitPinMigration", "--apply", 600);
        case "--migrate-support-face-testkit-pins" -> command("SupportFaceTestKitPinMigration", "--apply", 600);
        case "--migrate-formatting-pins" -> command("FormattingPinMigration", "--apply", 600);
        case "--migrate-shared-helper-pins" -> command("SharedHelperPinMigration", "--apply", 600);
        case "--migrate-unicode-pins" -> command("UnicodePinMigration", "--apply", 600);
        case "--migrate-adapter-split-pins" -> command("AdapterSplitPinMigration", "--apply", 600);
        case "--migrate-provider-discovery-pins" -> command("ProviderDiscoveryPinMigration", "--apply", 600);
        case "--migrate-gui-workbench-pins" -> command("GuiWorkbenchPinMigration", "--apply", 600);
        case "--rebalance-behavior-families" -> command("BehaviorFamilyRebalance", "--apply", 600);
        case "--migrate-behavior-family-pins" -> command("BehaviorFamilyPinMigration", "--apply", 600);
        case "--migrate-train-pins" -> command("TrainPinMigration", "--apply", 600);
        case "--refresh-readme-status" -> command("ReadmeStatus", "update", 600);
        case "--refresh-testkit-artifact-pins" -> command("TestKitArtifactPin", "--write", 600);
        case "--refresh-documentation" -> command("DocumentationCatalog", "--write", 600);
        case "--seal-lane-portability" -> command("LaneDifferential", "--seal", 60);
        case "--seal-client-lane-portability" -> command("LaneEvidence", "--seal", 60);
        case "--module-cache-doctor", "--cache-doctor" -> command("SharedCacheMaintenance", "doctor", 600);
        case "--module-cache-gc", "--cache-gc" -> command("SharedCacheMaintenance", "gc", 600);
        case "--cache-rebuild-drill" -> command("CacheRebuildDrill", "run", 600);
        default -> null;
    }; }

    private static String[] command(String type, String argument, int seconds) {
        return new String[] {type, argument, Integer.toString(seconds)};
    }
}
