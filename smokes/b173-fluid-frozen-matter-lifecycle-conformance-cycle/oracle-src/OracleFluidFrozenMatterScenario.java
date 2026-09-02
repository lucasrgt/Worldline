import worldline.testapi.FluidFrozenMatterObservation;
import worldline.testapi.FluidFrozenMatterScenario;
import worldline.trace.CanonicalTrace;

/** Official-name scenario for the fluid and frozen-matter lifecycle matrix. */
final class OracleFluidFrozenMatterScenario implements FluidFrozenMatterScenario {
    private final dj world;
    private final CanonicalTrace trace;

    OracleFluidFrozenMatterScenario(dj world, long seed) {
        this.world = world;
        trace = new CanonicalTrace(seed);
    }

    @Override public FluidFrozenMatterObservation observe() {
        OracleFluidFrozenMatterProbe probe = OracleFluidFrozenMatterProbe.execute(world);
        for (int index = 0; index < probe.rows.length; index++)
            trace.record("fluid-frozen-" + probe.rows[index][0], index + 1, 0,
                    probe.rows[index]);
        return probe.observation();
    }

    void emit() {
        trace.emitTo(System.out);
    }
}
