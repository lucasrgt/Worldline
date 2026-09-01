/** Exact successors for moving M622 into the hostile behavior catalog. */
final class MobAiBehaviorCatalogPlacementSuccessor {
    private static final String[][] SUCCESSORS = {
        {"modules/api/src/main/java/worldline/api/WorldlineBehavior.java",
                "2a27dd3f6e5857ec7e94dd1d3f47c057ddadd40a675e43e1f6c1bd00f013cf90",
                "12fafb9abd30402c7cc7c9d0c78ef9bdbc5879bc5ff6d9d6aea5e387498551aa"},
        {"modules/api/src/main/java/worldline/api/WorldlineHostileBehaviors.java",
                "74d7a4cea7c9f34deaa9a89953fc4e78035202e954996a62f66c47c8bd29632f",
                "d538a341440a61ef0db86587020a808bb28a1fae0179692ceeec2f0927dc3e9a"},
        {"modules/testkit/src/main/java/worldline/testkit/PathfindingMatrixFixture.java",
                "6b6f6bc306c66ee36782b6417a6ace123a9b5d02b75f044f65c7982ecfd1acfe",
                "c38bd4097a1c81badd7992aa1c534617f2a5e169e3850f4ba0545b99937e6ca8"},
        {"modules/testkit/src/test/java/worldline/testkit/PathfindingMatrixFixtureTest.java",
                "e250c1c39e169bd350f4ea3d68dfc1ccd8e7f826ba05c2757a682bac187e78f8",
                "fb42f3ecb655407fc979bbe1de648148e65912fa2d1e8f5dd3615bb31c4dd774"},
        {"release/testkit-artifacts.lock",
                "a7e3db6a0ca5f4ccb667540bf803ae47cce696c17b28753194c59b6757445936",
                "395fd8bca3bb0023c3c4aa7eb43c24374039463b0e170961e08c2e05981959a7"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "e3fcc1a871bdb18804ea0256e0e490df5d2dd6663b4b5b92e6ccfd01b944706d",
                "42731ad51b4e1786a893bd021dac1952cc40526f930fae80eceab31cecd4c3d3"},
        {"tools/harness/MobAiBehaviorManifestSuccessor.java",
                "aca1256d5d9649ae1b63029109b44a64581a44cfd120f67664efa441273ae7ed",
                "5f835e52a130369c6ff74eea77770d4a646743cf556f8746356339b4ae67a03b"}
    };

    private MobAiBehaviorCatalogPlacementSuccessor() { }

    static boolean carries(String relative, String prior, String current) {
        for (String[] successor : SUCCESSORS) {
            if (relative.equals(successor[0]) && prior.equals(successor[1])
                    && current.equals(successor[2])) return true;
        }
        return false;
    }

    static void selfTest() {
        for (String[] successor : SUCCESSORS) {
            require(carries(successor[0], successor[1], successor[2]),
                    "mob-AI behavior catalog successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "mob-AI behavior catalog successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "mob-AI behavior catalog successor allowlist drifted");
        DimensionAtlasTrainSourceSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
