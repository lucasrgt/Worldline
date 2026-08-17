package worldline.b173server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Bounded strict Packet20 entity-to-username correlation. */
final class B173EntityIdentityTracker {
    private static final int MAX_IDENTITIES = 64;
    private final Map<Integer, String> names = new HashMap<>();

    void bind(int entityId, String username) throws IOException {
        if (entityId < 0 || username == null || !username.matches("[A-Za-z0-9_]{1,16}"))
            throw new IOException("invalid named-player identity");
        String previous = names.get(entityId);
        if (previous != null && !previous.equals(username)) throw new IOException("conflicting named-player identity");
        if (previous == null && names.size() >= MAX_IDENTITIES) throw new IOException("named-player identity bound exceeded");
        names.put(entityId, username);
    }

    String username(int entityId) { return names.get(entityId); }
}
