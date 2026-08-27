import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Compiles the strict JSON/session adoption closure before invoking a legacy retry control. */
public final class LegacyRetryControlLauncher {
    private LegacyRetryControlLauncher() {
    }

    public static void main(String[] arguments) {
        try {
            int status = execute(arguments);
            if (status != 0) {
                System.exit(status);
            }
        } catch (Exception error) {
            System.err.println("legacy retry control launcher failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static int execute(String[] arguments) throws Exception {
        require(arguments.length > 0,
                "missing export, adopt, migrate, rollover, or --self-test command");
        Path root = Path.of("").toAbsolutePath().normalize();
        Path output = root.resolve(".worldline/build/legacy-retry-control");
        Files.createDirectories(output);
        List<Path> sources = List.of(root.resolve("tools/harness/MiniJson.java"),
                root.resolve("tools/integration/OpenCodeSessionExport.java"),
                root.resolve("tools/integration/LegacyRetryAdoption.java"),
                root.resolve("tools/integration/GitAncestry.java"),
                root.resolve("tools/integration/OxAlphaAdoptionReceipt.java"),
                root.resolve("tools/integration/OxAlphaProfile.java"),
                root.resolve("tools/integration/OxAlphaProviderLogMonitor.java"),
                root.resolve("tools/integration/OxAlphaProviderFailure.java"),
                root.resolve("tools/integration/OxAlphaProviderOccurrence.java"),
                root.resolve("tools/integration/OxAlphaRolloverReceipt.java"),
                root.resolve("tools/integration/OxAlphaRolloverLaunch.java"),
                root.resolve("tools/integration/OxAlphaRequest.java"),
                root.resolve("tools/integration/OxAlphaLegacyAdoption.java"),
                root.resolve("tools/integration/OxAlphaControlMigration.java"),
                root.resolve("tools/integration/OxAlphaInfrastructureRollover.java"));
        List<String> compile = new ArrayList<>(List.of(javaTool("javac"), "-encoding", "UTF-8",
                "--release", "21", "-Xlint:all,-options", "-Werror", "-d", output.toString()));
        for (Path source : sources) {
            require(Files.isRegularFile(source), "missing legacy retry source: " + source);
            compile.add(source.toString());
        }
        require(run(root, compile, 120) == 0, "legacy retry source closure did not compile");
        if ("--self-test".equals(arguments[0])) {
            require(target("migrate").equals("OxAlphaControlMigration"),
                    "control migration launcher route drifted");
            return 0;
        }
        String target = target(arguments[0]);
        List<String> command = new ArrayList<>(List.of(javaTool("java"), "-cp", output.toString(),
                target));
        command.addAll(List.of(arguments).subList(1, arguments.length));
        return run(root, command, 300);
    }

    private static String target(String operation) {
        return switch (operation) {
            case "export" -> "OpenCodeSessionExport";
            case "adopt" -> "LegacyRetryAdoption";
            case "migrate" -> "OxAlphaControlMigration";
            case "rollover" -> "OxAlphaInfrastructureRollover";
            default -> throw new IllegalArgumentException("unknown legacy retry command");
        };
    }

    private static int run(Path root, List<String> command, int seconds) throws Exception {
        Process process = new ProcessBuilder(command).directory(root.toFile()).inheritIO().start();
        process.getOutputStream().close();
        if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException(command.get(0) + " timed out");
        }
        return process.exitValue();
    }

    private static String javaTool(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin",
                name + (windows ? ".exe" : "")).toString();
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
