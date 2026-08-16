package worldline.reproduction;

/** Runtime-specific implementation used by the neutral replay CLI. */
public interface ReplayProvider {
    String runtimeId();

    ReplayReport replay(ReproductionBundle bundle);
}
