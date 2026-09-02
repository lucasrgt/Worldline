import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Canonical official dedicated-server defaults. The typed factory lives in the adapter. */
public final class OfficialServerBootstrap {
    static final int VIEW_DISTANCE = 3;
    static final boolean ALLOW_FLIGHT = true;
    private static final Path FACTORY = Path.of(
            "adapters/b173-server/src/main/java/worldline/b173server/OfficialServerBootstrap.java");

    private OfficialServerBootstrap() { }

    static boolean matches(int viewDistance, boolean allowFlight) {
        return viewDistance == VIEW_DISTANCE && allowFlight == ALLOW_FLIGHT;
    }

    static void selfTest() throws Exception {
        require(VIEW_DISTANCE == 3 && ALLOW_FLIGHT,
                "official dedicated-server defaults drifted");
        require(matches(3, true) && !matches(4, true) && !matches(3, false),
                "official dedicated-server matcher drifted");
        String factory = Files.readString(FACTORY, StandardCharsets.UTF_8);
        require(factory.contains("start(")
                        && factory.contains("new B173DedicatedServer")
                        && factory.contains("VIEW_DISTANCE")
                        && factory.contains("ALLOW_FLIGHT"),
                "official dedicated-server factory drifted");
        System.out.println("  official server bootstrap self-test: passed");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
