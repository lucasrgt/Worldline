package worldline.testkit;

import worldline.api.RemoteObjectSpawn;

/** Executes one universal Packet23 materialization claim without mapped game classes. */
public final class ObjectMaterializationFixture {
    private ObjectMaterializationFixture() { }

    public static ObjectMaterializationEvidence execute(EntityConformancePlan plan,
            String subject, ObjectSpawnExpectation expectation,
            ObjectMaterializationScenario scenario) {
        if (plan == null || expectation == null || scenario == null) {
            throw new NullPointerException("object materialization");
        }
        EntityConformanceCase claim = plan.caseFor(subject, "spawn-materialization");
        if (claim.layer() != ConformanceLayer.UNIVERSAL) {
            throw new IllegalArgumentException("object materialization must be universal");
        }
        RemoteObjectSpawn spawn = scenario.materialize(expectation.type());
        if (!expectation.matches(spawn)) {
            throw new IllegalStateException("Packet23 materialization identity drifted");
        }
        return new ObjectMaterializationEvidence(claim, expectation, spawn);
    }
}
