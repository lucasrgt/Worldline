package worldline.api;

import java.util.List;

/** Dedicated server control extended with connected-player observation. */
public interface MultiplayerServerRuntime extends DedicatedServerRuntime {
    List<String> players();
}
