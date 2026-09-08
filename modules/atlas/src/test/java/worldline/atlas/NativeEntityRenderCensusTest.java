package worldline.atlas;

/** The complete concrete rendering family must retain its exact public native provenance. */
final class NativeEntityRenderCensusTest {
    private NativeEntityRenderCensusTest() { }

    static void verify(AtlasStore store) {
        int concrete = 0, abstractTypes = 0;
        for (AtlasRecord record : store.kind(AtlasKind.CLAIM)) {
            if (!record.id().startsWith("atlas.claim.entity-")
                    || !record.id().endsWith(".native-render")) continue;
            if (record.id().equals("atlas.claim.entity-048.native-render")) {
                require(AtlasStatus.NOT_APPLICABLE.equals(record.status()), "abstract native render claim");
                abstractTypes++;
            } else {
                require(AtlasStatus.VERIFIED.equals(record.status())
                        && record.control().contains("automation=PUBLIC_TESTKIT"), "unbound native render claim");
                concrete++;
            }
            require(record.refs().contains("atlas.experiment.b173-native-entity-render-cycle"),
                    "native render provenance missing");
        }
        require(concrete == 23 && abstractTypes == 1, "native entity render census drift");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
