import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/** Qualifies real minimal ModLoader and Forge clients against the shared profiler runtime. */
public final class LegacyProfilerQualification {
    private LegacyProfilerQualification() {}

    public static void main(String[] arguments) {
        try {
            Path root = repository();
            if (Arrays.equals(arguments, new String[] {"--self-test"})) {
                LegacyProfilerQualificationSelfTest.execute(root); return;
            }
            require((arguments.length == 4 && "--qualify-all".equals(arguments[0]))
                    || (arguments.length == 5 && "--qualify".equals(arguments[0])), usage());
            Path base = Path.of(arguments[1]), artifacts = Path.of(arguments[2]);
            Path java8 = Path.of(arguments[3]);
            List<String> loaders = "--qualify-all".equals(arguments[0])
                    ? List.of("modloader", "forge") : List.of(loader(arguments[4]));
            LegacyProfilerQualificationConfig config = LegacyProfilerQualificationConfig.load(root);
            for (String loader : loaders) {
                LegacyLoaderWorkspace.Prepared prepared = LegacyLoaderWorkspace.prepare(
                        root, base, artifacts, java8, loader, config);
                LegacyProfilerQualificationProcess.execute(root, loader, prepared, config);
            }
        } catch (Exception error) {
            System.err.println("legacy profiler qualification failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static String loader(String value) {
        require("modloader".equals(value) || "forge".equals(value),
                "loader must be modloader or forge");
        return value;
    }

    private static Path repository() {
        for (Path path = Path.of("").toAbsolutePath().normalize(); path != null;
                path = path.getParent())
            if (Files.isRegularFile(path.resolve(LegacyProfilerQualificationConfig.FILE))) return path;
        throw new IllegalStateException("run qualification from the Worldline repository");
    }

    private static String usage() {
        return "usage: LegacyProfilerQualification --qualify-all BASE_WORKSPACE ARTIFACT_DIR JAVA8_HOME"
                + " or --qualify BASE_WORKSPACE ARTIFACT_DIR JAVA8_HOME modloader|forge";
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
