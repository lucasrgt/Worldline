package worldline.api;

/** Optional controlled capability for invariant and scaling work counters. */
public interface RuntimeWorkObservable extends AutomatedMinecraftRuntime {
    RuntimeWorkSnapshot workSnapshot();
}
