import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/** Public class-loader boundary for the shared data-driven smoke runner. */
public final class DataDrivenSupport {
    private DataDrivenSupport() { }

    public static String capture(Path directory, List<String> command) throws Exception {
        return SmokeSupport.capture(directory, command);
    }
    public static void recreate(Path root, Path target) throws IOException {
        SmokeSupport.recreate(root, target);
    }
    public static void verifyArtifact(Path path, Properties artifact) throws Exception {
        SmokeSupport.verifyArtifact(path, artifact);
    }
    public static List<String> javaFiles(Path source) throws IOException {
        return SmokeSupport.javaFiles(source);
    }
    public static void load(Path path, Properties target) throws IOException {
        SmokeSupport.load(path, target);
    }
    public static String value(Properties properties, String key) {
        return SmokeSupport.value(properties, key);
    }
    public static int freePort() throws IOException { return SmokeSupport.freePort(); }
    public static Path product(Path root, String module) {
        return SmokeSupport.product(root, module);
    }
    public static String line(String output, String prefix) {
        return SmokeSupport.line(output, prefix);
    }
}
