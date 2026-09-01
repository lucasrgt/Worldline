/** Exact train source successors introduced by the public entity lifecycle TestKit. */
final class EntityLifecycleTestKitSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/TESTKIT.md",
                "a48e1490c19a0d78170154eb7d3383f0c11de84893a06338a4e1d3bee83bc207",
                "68cb03372925c59d006ceaab00eb7a6aa7389bbf4a6f17b9d0f1be1801b350ef"},
        {"harness.properties",
                "1f1bf1d75113e77f052811e6e909f936afb19f553388ddc33828243539689476",
                "6f34a3d2a82547e61fb05934864b8c1fa616e2988c893123c24011768a8478f6"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "2a52826ca6be8d830a87757a9b47c7f6e5da302d8f098a87d93f1136a0b4e6b8",
                "28a1dbb5e14c3d85b12af14a84c7d98046bc7ef01f32703b97e40a404ce4b7ed"},
        {"behavior/functional-census/schema.properties",
                "3111bed8b8c3c7baf03835911c9a687c85f2a26e9f4fa7c58f3f3d44080dc853",
                "5307af31545989a71e33dd0d247c5a1d666ac50e8b85efc9a70daaf4dad9f29d"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "4493f711f34de9998d64d6a20f2eee4ed98988931852dcaa7d7e755b3bb9a9e4",
                "a121db0990d4b26aafcbdd9e6573896bc0b4afc25e251d153005d75433c999ed"},
        {"tools/harness/FunctionalCensusCheck.java",
                "aa91f7b06a3ddae48538fc845df487d23b53ba15bda7c229098aa5c62c164ae8",
                "27efdbfebba38683455287df021c8a66f407ef0c5fb2b767cc31d3fe94448f8f"},
        {"docs/ATLAS.md",
                "d1a7b092a98c191dc404c72d30e3bc99f47da2168cb4d4300c55b48b3f4c2617",
                "648d48a8782e5e60a61ed3cc8a4947c48bb4ca01f649966ae07c213622fc0676"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "83d473a1a60158669a2d0d91e0db9f2044f3df0157da8944244a5891430b11ae",
                "dcffe33de952aaf48b82ac2f8969dcbf911e7d5f10a2553ab36354166570cd92"},
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "731f52e8940f40d14b59c482c5c5443e994533eb45cd31ec196db2418a039e03",
                "0eb1c0d9f17e5042c15344b7f545f40d4e6de7709f6774e75dce3dbbb2333dac"},
        {"behavior/functional-census/families.tsv",
                "3b7dbecc5a2379065218b4548f1a2674e1d9f877ada26e45b30fffb9a464734c",
                "b1c472963f3635800db69acb31a55342533065082d617c1c682b286d04c3c71d"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "406bf6901fc4596856a8661924ad1f6ca85332d2961f8d988f8fec993e967485",
                "784106bc29fbea3b1ec018e7ff1a9e3e009036ba0ce34bda888f0052e3ec916a"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "65208a912c865a21b3bdd59d32aceb57bd5534d416819eeee2e679c90d3bd1ae",
                "435c9e7e24803d04480dbbe31ccda92399191c1299a112b45ad7c47b5185d24c"}
    };

    private EntityLifecycleTestKitSuccessor() { }

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
                    "entity lifecycle TestKit successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "entity lifecycle TestKit successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "entity lifecycle TestKit successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
