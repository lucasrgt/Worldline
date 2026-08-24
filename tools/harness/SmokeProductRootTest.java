import java.nio.file.Path;

/** Proves clean milestone worktrees resolve candidate module products without stale build state. */
final class SmokeProductRootTest {
    private SmokeProductRootTest() { }

    static void execute() {
        Path root = Path.of("").toAbsolutePath().normalize();
        Path fallback = root.resolve(".worldline/build/classes/api");
        require(SmokeSupport.product(root, "api", null).equals(fallback),
                "default smoke product root drifted");
        Path candidate = root.resolve(".worldline/candidates/m900-example/classes");
        require(SmokeSupport.product(root, "api", candidate.toString())
                        .equals(candidate.resolve("api")),
                "candidate smoke product root drifted");
        failure(() -> SmokeSupport.product(root, "api", root.resolve("outside").toString()));
        failure(() -> SmokeSupport.product(root, "../api", candidate.toString()));
        System.out.println("smoke product root self-test passed");
    }

    private static void failure(Runnable action) {
        try { action.run(); throw new AssertionError("expected smoke product root failure"); }
        catch (IllegalStateException expected) { }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
