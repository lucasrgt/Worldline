import worldline.testkit.RedstoneSignalConsumersObservation;
import worldline.testkit.RedstoneSignalConsumersScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped signal-consumer scenario. */
final class OracleRedstoneSignalConsumersScenario implements RedstoneSignalConsumersScenario {
    private final dj world;
    private final CanonicalTrace trace;

    OracleRedstoneSignalConsumersScenario(dj world, long seed) {
        this.world = world;
        trace = new CanonicalTrace(seed);
    }

    @Override
    public RedstoneSignalConsumersObservation observe() {
        OracleRedstoneSignalConsumersProbe probe = OracleRedstoneSignalConsumersProbe.execute(world);
        for (int index = 0; index < probe.rows.length; index++)
            trace.record("redstone-consumer-" + probe.rows[index][0], index + 1, 0,
                    probe.rows[index]);
        return new RedstoneSignalConsumersObservation(probe.states(), probe.shapes(),
                probe.light(), probe.ticks(), probe.neighbors());
    }

    void emit() {
        trace.emitTo(System.out);
    }
}
