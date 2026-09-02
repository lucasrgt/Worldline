import worldline.testapi.RedstoneInputControlsSubsystemObservation;
import worldline.testapi.RedstoneInputControlsSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped redstone input-control scenario. */
final class OracleRedstoneInputControlsScenario
        implements RedstoneInputControlsSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;

    OracleRedstoneInputControlsScenario(dj world, long seed) {
        this.world = world;
        trace = new CanonicalTrace(seed);
    }

    @Override public RedstoneInputControlsSubsystemObservation observe() {
        OracleRedstoneInputControlsProbe probe =
                OracleRedstoneInputControlsProbe.execute(world);
        trace.record("lever", 1, 0, probe.lever);
        trace.record("button", 2, 0, probe.button);
        trace.record("stone-plate", 3, 0, probe.stonePlate);
        trace.record("wooden-plate", 4, 0, probe.woodenPlate);
        trace.record("support", 5, 4, probe.supportMask);
        return new RedstoneInputControlsSubsystemObservation(probe.lever(), probe.button(),
                probe.stonePlate(), probe.woodenPlate(), probe.support());
    }

    void emit() { trace.emitTo(System.out); }
}
