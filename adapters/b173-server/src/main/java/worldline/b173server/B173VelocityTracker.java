package worldline.b173server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

/** Bounded Packet28 queue keyed by entity identity. */
final class B173VelocityTracker {
    private static final int MAX = 64;
    private final ArrayList<B173EntityVelocity> pending = new ArrayList<>();

    void accept(DataInputStream input) throws IOException {
        B173EntityVelocity value = new B173EntityVelocity(input.readInt(),
                input.readShort(), input.readShort(), input.readShort());
        if (pending.size() == MAX) pending.remove(0);
        pending.add(value);
    }

    B173EntityVelocity take(int entityId) {
        if (entityId < 0) throw new IllegalArgumentException("invalid velocity entity");
        for (int index = 0; index < pending.size(); index++)
            if (pending.get(index).entityId() == entityId) return pending.remove(index);
        return null;
    }
}
