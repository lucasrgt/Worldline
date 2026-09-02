package worldline.api.scenario;

/** Reusable action boundary for one observed lightning-to-powered transformation. */
public interface PoweredCreeperActions {
    PoweredCreeperEvidence.Trial strike();
    PoweredCreeperEvidence.CreeperState current();

    default PoweredCreeperEvidence exercise(Runnable tick) {
        if (tick == null) {
            throw new IllegalArgumentException("missing powered-creeper tick");
        }
        PoweredCreeperEvidence.Trial trial = strike();
        tick.run();
        return PoweredCreeperEvidence.capture(trial, current());
    }
}
