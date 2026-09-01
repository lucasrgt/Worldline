import worldline.testkit.RailNetworkSubsystemObservation;
import worldline.testkit.RailNetworkSubsystemScenario;
import worldline.trace.CanonicalTrace;

/** Official-name counterpart of the mapped rail-network scenario. */
final class OracleRailNetworkSubsystemScenario implements RailNetworkSubsystemScenario {
    private final dj world;
    private final CanonicalTrace trace;

    OracleRailNetworkSubsystemScenario(dj world, long seed) {
        this.world = world;
        trace = new CanonicalTrace(seed);
    }

    @Override public RailNetworkSubsystemObservation observe() {
        OracleRailNetworkSubsystemProbe probe = OracleRailNetworkSubsystemProbe.execute(world);
        trace.record("normal", 1, 0, probe.normalRail);
        trace.record("powered", 2, 0, probe.poweredRail);
        trace.record("detector", 3, 1, probe.detectorRail);
        trace.record("support", 4, 3, probe.supportMask);
        return new RailNetworkSubsystemObservation(probe.normalRail(), probe.poweredRail(),
                probe.detectorRail(), probe.support());
    }

    void emit() { trace.emitTo(System.out); }
}
