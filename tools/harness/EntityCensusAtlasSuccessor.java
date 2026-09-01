/** Exact train source successors introduced by the persistent-entity Census integration. */
final class EntityCensusAtlasSuccessor {
    private static final String[][] SUCCESSORS = {
        {"coordination/swarm/OX_ALPHA_PROMPT.md",
                "865fa85f54659ce53b882dd4f582baf7114ec710eecff84a259298c21d918c79",
                "acc01489061e2a0c6653d1d4ebe0cbe015376f06ab2bb83d99ca549e82d04a80"},
        {"behavior/functional-census/schema.properties",
                "b4a5c6ea76f10da1c05cae0121e36ae34fdb5a54bf4fd66056219cf3c2b0c302",
                "3111bed8b8c3c7baf03835911c9a687c85f2a26e9f4fa7c58f3f3d44080dc853"},
        {"coordination/swarm/objectives/TEMPLATE.properties",
                "a04d876b304e974531f7ef00a9ebda4d3a1d87f700a70748e3e652350955c583",
                "d720c5f1066cc5b76edf8c0e2bc8bab9bcb289acd0f7e15deee58116447973ba"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "b33315f3c8a8caba68135c347269f63d7f6983c7794a30f53e949bbef54880c2",
                "4493f711f34de9998d64d6a20f2eee4ed98988931852dcaa7d7e755b3bb9a9e4"},
        {"tools/harness/FunctionalCensusCheck.java",
                "ff38a181f2bc0f14604a5221f10840985f3dc1f81bec4e6778bdcf9e9f5d8cce",
                "aa91f7b06a3ddae48538fc845df487d23b53ba15bda7c229098aa5c62c164ae8"},
        {"tools/integration/MilestoneObjective.java",
                "0c37ef72bd43e969b92382184a823a366254f6f9d5baf7265df3eaa09a8265a4",
                "66936e3f8965fd727bb435bd3095460cc4601ca1eaaf358c74dd3315de68c10a"},
        {"docs/ATLAS.md",
                "d36448f0d2650064f7e709251471f487a2407b855db6eb5c656215e840e03387",
                "d1a7b092a98c191dc404c72d30e3bc99f47da2168cb4d4300c55b48b3f4c2617"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasFunctionalCensusImport.java",
                "17289a7c2520766c23ba12e74b4ea626f831aac2af8df955eed51a8b5d93b036",
                "427a91461cf06d5b6e62e0614048e3e38b9eed8eefebfc26e4840a1eb4a54e5b"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSynchronization.java",
                "e861fcd8023d6338ad27daf1e24e4869076c27adad693a7be8697e93abf1def2",
                "6b124eaa2eb51094ff17b25aac917db52541806d68392d5187093f16c0e859b2"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "539cf2f52549378276429ce375d4450da4210c25bbedba6b2aa11ce0688be6fd",
                "83d473a1a60158669a2d0d91e0db9f2044f3df0157da8944244a5891430b11ae"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "066b4012c8c04c20b0b8044929cfcc1e524423f8c20e9d3f54f7dc9b890d614f",
                "406bf6901fc4596856a8661924ad1f6ca85332d2961f8d988f8fec993e967485"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "01bfff223bfb4cdfdfb7df79ee153b14e4ff4ee39f658c4652771521e56f3043",
                "65208a912c865a21b3bdd59d32aceb57bd5534d416819eeee2e679c90d3bd1ae"}
    };

    private EntityCensusAtlasSuccessor() { }

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
                    "entity Census Atlas successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "entity Census Atlas successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "entity Census Atlas successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
