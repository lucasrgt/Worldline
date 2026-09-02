package worldline.b173server;

import java.nio.file.Path;
import java.time.Duration;

/** Canonical official dedicated-server constructor for b1.7.3 oracles. */
public final class OfficialServerBootstrap {
    public static final int VIEW_DISTANCE = 3;
    public static final boolean ALLOW_FLIGHT = true;

    private OfficialServerBootstrap() { }

    public static B173DedicatedServer start(Path jar, Path directory, int port, long seed,
            Duration timeout) {
        return new B173DedicatedServer(jar, directory, port, seed, timeout,
                VIEW_DISTANCE, ALLOW_FLIGHT);
    }
}
