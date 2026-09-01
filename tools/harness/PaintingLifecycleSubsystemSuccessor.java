/** Exact reviewed successors for the complete public painting lifecycle subsystem. */
final class PaintingLifecycleSubsystemSuccessor {
    private static final String[][] SUCCESSORS = {
        {"adapters/b173-server/src/main/java/worldline/b173server/B173WireClient.java",
                "5fc866916c7a85532cf6f66555185d559bc22f9772963ed7832ccdc21e3f7680",
                "e3d2758029dfb32b88fa96347067f119e87575ce741d3df0fb46ce286bc52450"},
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "b49cafed3be777d23cb14d3441f09ddb4f95a3db07b5bfa1cc6258a4227f2775",
                "f884b07f718ae7709de3460073affac0a824eac5b566903560cc8a2aae387d0e"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "75a1571ffbb6e0399983203b66aae722ff2e4d3057bd21322974cd58cf00b6f3",
                "60e21dc4fa2cad491ede1f2fb8e823b859879501d6b72036a944dce9b6d02716"},
        {"docs/ATLAS.md",
                "0342b911b3c4873640cea76f56965f62f0411ff57881830d57c95fa1422248e7",
                "917ded6d50dfe742bf5678d07486029a492aa1a163bbe622fae9b2f074354375"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "0b9f8301a93e1963fc7e03c5a9c6638985729fb220d7b536f1897685fb146296",
                "44cadd0af89e127e8893f8950289c483dae64c774ba1ecd96ae921388dad5693"},
        {"docs/TESTKIT.md",
                "0a3aaf7544f051fbd308202caed6aa5a0522ce287e55a9d4180a540aee35db82",
                "f5ce25dc632fd470c8ee0a06432a969f4ce1a8749506479021acac9bff087420"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "8913b4b5f7eac7233e78b41b0604c43459739539af823111ea0be547cc00edce",
                "413852e90c1186a3e3a743265c7bf1a9e0b4d7dc7af48237d605c435465b76c6"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "b22ca4f6e4a959c8fd0339b8cc74a1c40b3875cdccf185afa2ea2baee59a419b",
                "8d3742cbf05d8f9b586078842ebdfcfed37a2d47ce6ccf868398d167061e0f72"},
        {"release/testkit-artifacts.lock",
                "a4ceb0002b26ac3941f4429280d5511418933dd4e33cd2d8790d2618465d963e",
                "739bd226a6d35bdc24bcb905dfb17927016327e3bb088d6b6235b590840db5fa"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "441d0274926ed8f6d2a0657edb28b98da4a540120bd151aa55ffcad211ba708e",
                "f6158bfe5f3d6ec4b75f92d015783e8773890ae57f18886632c3b6cab19a710e"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "4eba4e351e2d5b6788762fe22a231120da395cada2f4f6c5be5d0ad7385f3f51",
                "83031d6551430a6ba4eeebc8aa800fcaa1c4f490313b468b7ec44885f63999d2"}
    };

    private PaintingLifecycleSubsystemSuccessor() { }

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
                    "painting lifecycle successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "painting lifecycle successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "painting lifecycle successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
