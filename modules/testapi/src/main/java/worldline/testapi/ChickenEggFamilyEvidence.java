package worldline.testapi;

/** Equatable evidence for chicken materialization and the bounded egg-family scene. */
public final class ChickenEggFamilyEvidence {
    private final EntityConformanceCase claim;
    private final ChickenEggFamilyObservation observation;

    public ChickenEggFamilyEvidence(EntityConformanceCase claim,
            ChickenEggFamilyObservation observation) {
        this.claim = claim;
        this.observation = observation;
    }

    public EntityConformanceCase claim() { return claim; }
    public ChickenEggFamilyObservation observation() { return observation; }

    public String canonical() {
        return "schema=worldline.chicken-egg-family-evidence.v1\n"
                + "claim=" + claim.claimId() + '|' + claim.layer() + "\n"
                + "chicken=packet:24,type:93,positive-distinct-id:true\n"
                + "laying=bounded-optional,item:344-if-observed\n"
                + "thrown=packet:23,type:62,thrower:zero-or-actor,platform-radius:8\n";
    }

    @Override public boolean equals(Object other) {
        return other instanceof ChickenEggFamilyEvidence
                && canonical().equals(((ChickenEggFamilyEvidence) other).canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }
}
