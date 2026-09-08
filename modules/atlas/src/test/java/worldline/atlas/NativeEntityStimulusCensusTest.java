package worldline.atlas;

/** Finite Census closure retains actual public proof and explicit abstract applicability. */
final class NativeEntityStimulusCensusTest {
    private NativeEntityStimulusCensusTest() { }
    static void verify(AtlasStore store) {
        int verified = 0, excluded = 0, stimulus = 0;
        for (AtlasRecord claim : store.kind(AtlasKind.CLAIM)) {
            if (!claim.id().startsWith("atlas.claim.entity-")) continue;
            if (AtlasStatus.NOT_APPLICABLE.equals(claim.status())) {
                require(claim.id().startsWith("atlas.claim.entity-048."), "concrete entity was excluded");
                excluded++;
            } else {
                require(AtlasStatus.VERIFIED.equals(claim.status())
                        && claim.control().contains("automation=PUBLIC_TESTKIT"), "entity lacks public proof");
                verified++;
            }
            if (claim.refs().contains("atlas.experiment.b173-native-entity-stimulus-cycle")) stimulus++;
        }
        require(verified == 254 && excluded == 10 && stimulus == 53, "native stimulus census closure drift");
        for (String id : new String[] {"001.spawn-materialization", "001.damage-death", "010.interaction-state",
                "020.damage-death", "041.drop-matrix", "056.interaction-state", "092.drop-matrix"}) {
            require(store.get("atlas.claim.entity-" + id).refs().contains(
                    "atlas.experiment.b173-native-entity-stimulus-cycle"), "native stimulus proof omitted: " + id);
        }
        require(store.get("atlas.claim.entity-051.interaction-state").refs().contains(
                "atlas.experiment.m445-skeleton-ranged-ai-set"), "independent skeleton ranged proof replaced");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
