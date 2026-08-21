package worldline.testkit;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Prevents derived test output from targeting official evidence roots. */
public final class OutputGuard {
    private OutputGuard() {}
    public static Path safe(Path path, String role) {
        if (path == null) throw new NullPointerException(role);
        Path value = path.toAbsolutePath().normalize();
        Path root = Paths.get("").toAbsolutePath().normalize();
        if (protectedRoot(value, root.resolve("release"))
                || protectedRoot(value, root.resolve("smokes"))
                || protectedRoot(value, root.resolve("artifacts")))
            throw new IllegalArgumentException(role + " cannot target protected evidence path: " + value);
        return value;
    }
    private static boolean protectedRoot(Path value, Path protectedPath) {
        return value.equals(protectedPath) || value.startsWith(protectedPath);
    }
}
