import worldline.testkit.BuiltEnvironmentMaterialsObservation;
import worldline.testkit.BuiltEnvironmentMaterialsScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped construction-material scenario. */
final class OracleBuiltEnvironmentMaterialsScenario
        implements BuiltEnvironmentMaterialsScenario {
    private final dj world;
    private final CanonicalTrace trace;

    OracleBuiltEnvironmentMaterialsScenario(dj world, long seed) {
        this.world = world;
        trace = new CanonicalTrace(seed);
    }

    @Override public BuiltEnvironmentMaterialsObservation observe() {
        OracleBuiltEnvironmentMaterialsProbe probe =
                OracleBuiltEnvironmentMaterialsProbe.execute(world);
        for (int index = 0; index < probe.rows.length; index++)
            trace.record("material-" + probe.rows[index][0], index + 1, 0, probe.rows[index]);
        return new BuiltEnvironmentMaterialsObservation(probe.states(), probe.shapes(),
                probe.light(), probe.ticks(), probe.neighbors());
    }

    void emit() { trace.emitTo(System.out); }
}
