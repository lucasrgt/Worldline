import worldline.testkit.VegetationEcologyObservation;
import worldline.testkit.VegetationEcologyScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped vegetation ecology scenario. */
final class OracleVegetationEcologyScenario implements VegetationEcologyScenario {
    private final dj world;
    private final CanonicalTrace trace;

    OracleVegetationEcologyScenario(dj world, long seed) {
        this.world = world;
        trace = new CanonicalTrace(seed);
    }

    @Override public VegetationEcologyObservation observe() {
        OracleVegetationEcologyProbe probe = OracleVegetationEcologyProbe.execute(world);
        for (int index = 0; index < probe.rows.length; index++)
            trace.record("vegetation-" + probe.rows[index][0], index + 1, 0,
                    probe.rows[index]);
        return new VegetationEcologyObservation(probe.states(), probe.shapes(),
                probe.light(), probe.neighbors());
    }

    void emit() { trace.emitTo(System.out); }
}
