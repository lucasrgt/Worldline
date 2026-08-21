package worldline.b173;

import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.minimization.ScenarioStep;
import worldline.trace.CanonicalStateTrace;

/** Shared execution of public-grammar steps over one controlled runtime. */
final class B173ScenarioOps {
    static final String[] SCHEMA = {"tick", "block65"};

    private B173ScenarioOps() {}

    static void apply(ScenarioStep step, B173Runtime runtime, CanonicalStateTrace trace) {
        switch (step.kind()) {
            case TICK:
                for (int index = 0; index < step.count(); index++) runtime.tick();
                return;
            case RESEED:
                runtime.reseed(step.seed()); return;
            case TAP:
                runtime.tap(step.key()); return;
            case OBSERVE:
                B173Observation state = runtime.observe();
                trace.record(step.label(), state.clientTick(), state.blockColumn()[1]);
                return;
            case BLOCK:
                require(step.blockId() == 0
                        || net.minecraft.src.Block.blocksList[step.blockId()] != null,
                        "unregistered block id " + step.blockId());
                runtime.world().setBlock(new BlockPosition(step.x(), step.y(), step.z()),
                        new BlockState(step.blockId(), step.metadata()));
                return;
            default:
                throw new IllegalStateException("unexecutable scenario step");
        }
    }

    static CanonicalStateTrace trace(B173Runtime runtime, long seed) {
        return new CanonicalStateTrace(seed, SCHEMA);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
