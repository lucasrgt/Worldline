package worldline.testapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;

/** One causal placement or activation and the exact cells it must produce. */
public final class BlockStateDomainStep {
    public enum Action { PLACE_HELD, ACTIVATE }

    private final String id;
    private final Action action;
    private final BlockPosition position;
    private final BlockFace face;
    private final float yaw, pitch;
    private final List<BlockStateObservation> observations;

    private BlockStateDomainStep(String id, Action action, BlockPosition position,
            BlockFace face, float yaw, float pitch, List<BlockStateObservation> observations) {
        if (id == null || !id.matches("[a-z0-9][a-z0-9._-]{0,63}")) {
            throw new IllegalArgumentException("invalid state-domain step id");
        }
        this.id = id;
        this.action = Objects.requireNonNull(action, "action");
        this.position = Objects.requireNonNull(position, "position");
        this.face = Objects.requireNonNull(face, "face");
        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            throw new IllegalArgumentException("invalid state-domain look");
        }
        if (observations == null || observations.isEmpty()
                || observations.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("state-domain observations are empty");
        }
        this.yaw = yaw;
        this.pitch = pitch;
        this.observations = Collections.unmodifiableList(
                new ArrayList<BlockStateObservation>(observations));
    }

    public static BlockStateDomainStep place(String id, BlockPosition support, BlockFace face,
            float yaw, float pitch, List<BlockStateObservation> observations) {
        return new BlockStateDomainStep(id, Action.PLACE_HELD, support, face,
                yaw, pitch, observations);
    }

    public static BlockStateDomainStep activate(String id, BlockPosition position, BlockFace face,
            List<BlockStateObservation> observations) {
        return new BlockStateDomainStep(id, Action.ACTIVATE, position, face,
                0F, 0F, observations);
    }

    public String id() { return id; }
    public Action action() { return action; }
    public BlockPosition position() { return position; }
    public BlockFace face() { return face; }
    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public List<BlockStateObservation> observations() { return observations; }
}
