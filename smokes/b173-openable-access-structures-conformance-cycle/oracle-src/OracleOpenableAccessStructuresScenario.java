import worldline.testkit.OpenableAccessStructuresObservation;
import worldline.testkit.OpenableAccessStructuresScenario;
import worldline.trace.CanonicalTrace;

/** Official-name scenario for the openable access structures lifecycle matrix. */
final class OracleOpenableAccessStructuresScenario implements OpenableAccessStructuresScenario {
    private final dj world;
    private final CanonicalTrace trace;

    OracleOpenableAccessStructuresScenario(dj world, long seed) {
        this.world = world;
        trace = new CanonicalTrace(seed);
    }

    @Override public OpenableAccessStructuresObservation observe() {
        OracleOpenableAccessStructuresProbe probe = OracleOpenableAccessStructuresProbe.execute(world);
        for (int index = 0; index < probe.rows.length; index++)
            trace.record("openable-access-" + probe.rows[index][0], index + 1, 0,
                    probe.rows[index]);
        return probe.observation();
    }

    void emit() {
        trace.emitTo(System.out);
    }
}
