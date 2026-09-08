package worldline.atlas;

/** Native dynamics publishes causal controls while retaining explicit unproven gameplay cells. */
final class NativeEntityDynamicsCensusTest {
    private NativeEntityDynamicsCensusTest() { }
    static void verify(AtlasStore store) {
        int published = 0, unknown = 0;
        for (AtlasRecord claim : store.kind(AtlasKind.CLAIM)) {
            if (!claim.id().startsWith("atlas.claim.entity-")) continue;
            if (AtlasStatus.UNKNOWN.equals(claim.status())) unknown++;
            if (!claim.refs().contains("atlas.experiment.b173-native-entity-dynamics-cycle")) continue;
            require(AtlasStatus.VERIFIED.equals(claim.status())
                    && claim.control().contains("automation=PUBLIC_TESTKIT"), "native dynamics public binding");
            published++;
        }
        require(published == 59 && unknown == 53, "bounded native dynamics census drift");
        for (String id : new String[] {"009.movement-policy", "009.tick-lifecycle",
                "011.environment-response", "093.environment-response", "094.tick-lifecycle"}) {
            require(store.get("atlas.claim.entity-" + id).refs().contains(
                    "atlas.experiment.b173-native-entity-dynamics-cycle"), "native special case omitted: " + id);
        }
        require(!store.get("atlas.claim.entity-020.tick-lifecycle").refs().contains(
                "atlas.experiment.b173-native-entity-dynamics-cycle"), "prior TNT lifecycle proof was replaced");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
