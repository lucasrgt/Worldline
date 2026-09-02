package worldline.testapi;

import java.util.Objects;
import worldline.api.BlockPosition;

/** One cell whose control and post-treatment light planes form a causal comparison. */
public final class BlockLightProbe {
    private final String id;
    private final BlockPosition position;
    private final BlockLightExpectation control, treatment;

    public BlockLightProbe(String id, BlockPosition position,
            BlockLightExpectation control, BlockLightExpectation treatment) {
        if (id == null || !id.matches("[a-z0-9][a-z0-9._-]{0,63}")) {
            throw new IllegalArgumentException("invalid light probe id");
        }
        this.id = id; this.position = Objects.requireNonNull(position, "position");
        this.control = Objects.requireNonNull(control, "control");
        this.treatment = Objects.requireNonNull(treatment, "treatment");
    }

    public String id() { return id; }
    public BlockPosition position() { return position; }
    public BlockLightExpectation control() { return control; }
    public BlockLightExpectation treatment() { return treatment; }
}
