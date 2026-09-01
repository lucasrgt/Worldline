package worldline.testkit;

import worldline.api.RemoteObjectSpawn;

/** Equatable normalized evidence for one universal Packet23 materialization claim. */
public final class ObjectMaterializationEvidence {
    private final EntityConformanceCase claim;
    private final ObjectSpawnExpectation expectation;
    private final RemoteObjectSpawn spawn;

    ObjectMaterializationEvidence(EntityConformanceCase claim,
            ObjectSpawnExpectation expectation, RemoteObjectSpawn spawn) {
        this.claim = claim;
        this.expectation = expectation;
        this.spawn = spawn;
    }

    public EntityConformanceCase claim() { return claim; }
    public RemoteObjectSpawn spawn() { return spawn; }

    public String canonical() {
        return "schema=worldline.object-materialization-evidence.v1\n"
                + "claim=" + claim.claimId() + '|' + claim.layer() + "\n"
                + "spawn=packet:23,type:" + expectation.type() + ",positive-id:true\n"
                + "thrower=" + expectation.canonicalThrower() + "\n"
                + "velocity=" + (expectation.stationary() ? "zero" : "unconstrained") + "\n";
    }

    @Override public boolean equals(Object other) {
        return other instanceof ObjectMaterializationEvidence
                && canonical().equals(((ObjectMaterializationEvidence) other).canonical());
    }

    @Override public int hashCode() { return canonical().hashCode(); }
}
