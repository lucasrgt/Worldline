package worldline.atlas;

/** Native reconstruction contributes all concrete subjects without replacing sheep provenance. */
final class EntityPersistenceCensusTest {
    private EntityPersistenceCensusTest() { }

    static void execute(AtlasStore store) {
        int verified = 0, abstractTypes = 0;
        for (AtlasRecord claim : store.kind(AtlasKind.CLAIM)) {
            if (!claim.id().startsWith("atlas.claim.entity-")
                    || !claim.id().endsWith(".save-reload")) continue;
            if (claim.id().equals("atlas.claim.entity-048.save-reload")) {
                require(AtlasStatus.NOT_APPLICABLE.equals(claim.status()),
                        "abstract EntityLiving must not receive a fabricated instance");
                abstractTypes++;
            } else {
                require(AtlasStatus.VERIFIED.equals(claim.status())
                                && claim.control().contains("automation=PUBLIC_TESTKIT"),
                        "concrete native persistence is not publicly executable: " + claim.id());
                verified++;
            }
            require(claim.refs().contains(
                            "atlas.experiment.b173-entity-persistence-envelope-cycle"),
                    "native persistence provenance was omitted: " + claim.id());
        }
        require(verified == 23 && abstractTypes == 1, "native persistence census drifted");
        require(store.get("atlas.claim.entity-091.save-reload").refs().contains(
                        "atlas.experiment.m506-sw-sheep-sheared-persistence"),
                "native NBT proof must retain the independent sheep restart proof");
        int abstractClaims = 0;
        for (AtlasRecord claim : store.kind(AtlasKind.CLAIM)) {
            if (!claim.id().startsWith("atlas.claim.entity-048.")) continue;
            boolean registry = claim.id().endsWith(".registry-presence");
            require((registry ? AtlasStatus.VERIFIED : AtlasStatus.NOT_APPLICABLE)
                    .equals(claim.status()), "abstract registry/runtime distinction drifted");
            abstractClaims++;
        }
        require(abstractClaims == 11, "abstract entity applicability census drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
