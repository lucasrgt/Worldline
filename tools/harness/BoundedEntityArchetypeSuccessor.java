/** Exact reviewed successors for bounded public entity archetype lifecycles. */
final class BoundedEntityArchetypeSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "0eb1c0d9f17e5042c15344b7f545f40d4e6de7709f6774e75dce3dbbb2333dac",
                "12db1dac564454cb6bc2a0aa3fa23a5db6fa2acc1693bd3093e646d20c906446"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "9f3d41ea7c0f936f51cf766d0b0e3017ce4e197e8c8cecc7d6efb4fcfea7dad7",
                "7d06aefbcbd389ee089080cd1fc9f84038745bee087d0aff9eb3bd87e945de5e"},
        {"docs/ATLAS.md",
                "648d48a8782e5e60a61ed3cc8a4947c48bb4ca01f649966ae07c213622fc0676",
                "8ceba4bbd3d883cb05ee67913d41a360ce0c5c84bbb108caa53367ada429f822"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "a121db0990d4b26aafcbdd9e6573896bc0b4afc25e251d153005d75433c999ed",
                "a0bd32a2e404691b8b51e040257883f4dd1be0ed6891098c4dc3dfe0afc062ce"},
        {"docs/TESTKIT.md",
                "68cb03372925c59d006ceaab00eb7a6aa7389bbf4a6f17b9d0f1be1801b350ef",
                "dc12d275003e51ec9560f815da32ef2440381284c377bd1409b27c9639524738"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "dcffe33de952aaf48b82ac2f8969dcbf911e7d5f10a2553ab36354166570cd92",
                "913bfe56b5d391733b4976c74ed58a37472b9d5990561a049abdfdd45f7de7f5"},
        {"modules/testapi/src/main/java/worldline/testkit/EntityLifecycleEvidence.java",
                "1126d64a682b29d92b943a573eaa8e6a968583ca8a3dfbff227484f8b7d11621",
                "c7e5a8f9d8a15d8a27b77c4fb0f23f0e4a11587cb8d4e36d31083348a448f576"},
        {"modules/testapi/src/main/java/worldline/testkit/EntityLifecycleFixture.java",
                "61e47a1e8a301fdb03a45f1220542dcdbca1a56992a8483ac085bca102e56d70",
                "779441b3c9396d82d536fe60be4e2418d3c9686a675968359d86704fb5d1cc08"},
        {"modules/testkit/src/test/java/worldline/testkit/EntityConformancePlanTest.java",
                "dc1bc46853f1e98667ce6f9f6715e325b275959efe92510a78f1d22fd4948b44",
                "b29ec7727c22e6b69a97fb2c064345592377cda2ee3d8a289564e678c1882eec"},
        {"modules/testkit/src/test/java/worldline/testkit/EntityLifecycleFixtureTest.java",
                "6c26920e690f908ea6166e37ac781fe1d0ceaba92d2507186fe8aaced15ef74f",
                "a92305c5f5a022e7e5e4139b99c25717f1fbd4eb293d9ac9edda9e8872eb256e"},
        {"release/testkit-artifacts.lock",
                "f846df93c0a471fb0a8202c7c936a6c37268074e76d9aeadefa6962c83d57e97",
                "60f62800a7ff3200c9c643f946b43e2a984ca3eabb60c789184709750373c942"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "416713838efcf9a3ec3cc2ecb1dd4ddfc3eef6ca17c2eefdae51eb324f40a82e",
                "7a46b2904790881280ff2bb96a568b2a83bc4889ec668c0924e595bb85ad259d"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "6fcdd27f3974a2fc114b811d9c9482530fa63ff2391348b417050a9ca2057803",
                "43063f198a947f93f1598639b04b91fc905244c0d090b01e661fddf51cfd760e"}
    };

    private BoundedEntityArchetypeSuccessor() { }

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
                    "bounded entity successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "bounded entity successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "bounded entity successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
