/** Exact reviewed successors for the universal Packet23 materialization matrix. */
final class ObjectMaterializationMatrixSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "12db1dac564454cb6bc2a0aa3fa23a5db6fa2acc1693bd3093e646d20c906446",
                "b49cafed3be777d23cb14d3441f09ddb4f95a3db07b5bfa1cc6258a4227f2775"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "7d06aefbcbd389ee089080cd1fc9f84038745bee087d0aff9eb3bd87e945de5e",
                "75a1571ffbb6e0399983203b66aae722ff2e4d3057bd21322974cd58cf00b6f3"},
        {"docs/ATLAS.md",
                "8ceba4bbd3d883cb05ee67913d41a360ce0c5c84bbb108caa53367ada429f822",
                "0342b911b3c4873640cea76f56965f62f0411ff57881830d57c95fa1422248e7"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "a0bd32a2e404691b8b51e040257883f4dd1be0ed6891098c4dc3dfe0afc062ce",
                "0b9f8301a93e1963fc7e03c5a9c6638985729fb220d7b536f1897685fb146296"},
        {"docs/TESTKIT.md",
                "dc12d275003e51ec9560f815da32ef2440381284c377bd1409b27c9639524738",
                "0a3aaf7544f051fbd308202caed6aa5a0522ce287e55a9d4180a540aee35db82"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "913bfe56b5d391733b4976c74ed58a37472b9d5990561a049abdfdd45f7de7f5",
                "8913b4b5f7eac7233e78b41b0604c43459739539af823111ea0be547cc00edce"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "28a1dbb5e14c3d85b12af14a84c7d98046bc7ef01f32703b97e40a404ce4b7ed",
                "b22ca4f6e4a959c8fd0339b8cc74a1c40b3875cdccf185afa2ea2baee59a419b"},
        {"release/testkit-artifacts.lock",
                "60f62800a7ff3200c9c643f946b43e2a984ca3eabb60c789184709750373c942",
                "a4ceb0002b26ac3941f4429280d5511418933dd4e33cd2d8790d2618465d963e"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "7a46b2904790881280ff2bb96a568b2a83bc4889ec668c0924e595bb85ad259d",
                "441d0274926ed8f6d2a0657edb28b98da4a540120bd151aa55ffcad211ba708e"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "43063f198a947f93f1598639b04b91fc905244c0d090b01e661fddf51cfd760e",
                "4eba4e351e2d5b6788762fe22a231120da395cada2f4f6c5be5d0ad7385f3f51"}
    };

    private ObjectMaterializationMatrixSuccessor() { }

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
                    "object materialization successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "object materialization successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "object materialization successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
