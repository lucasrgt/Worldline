/** Exact train successors introduced by the reviewed redstone Atlas integration. */
final class RedstoneAtlasTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/generated/MILESTONES.md",
                "0a2a41227387f89d13cb31db4306bc31045dfab4fd74ac1c458a7cf16bf9f3f0",
                "e285c3d85c1ec171ca450a7e23a90e5f187fdbd620a9ff05df2a656f189c9b09"},
        {"docs/generated/STATUS.md",
                "d6b516c8635d7ad3a2b28d996382fed6b8f652c763a3dcaf59ed89401c1434ea",
                "4427a26207df2e5fe976d021fcf9cd16fed69b98ce7f4bcae227ff896cc26155"},
        {"release/testkit-artifacts.lock",
                "e28de382e36501a77ae786bd4ebb246a8b2d8558611195da7a98469c2e067e3c",
                "d30282c42992d27c2378d01dfe4c16030f1f0fff5eeefec20b4d64a6b4daba60"},
        {"release/worldline.properties",
                "8269550e1943ab537c13b2ccdf0a8bd06f7639ee884628ca000fd60f503756be",
                "c01204d8d2400237d31b5aa1ea2a84d14311b1fc0bb005f3c675c4f0daf3ac0a"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java",
                "19fbd78e4812ee6ea37baed2c0e1dacd79ca054eb42cbdeba73b02d87a62f819",
                "ad88b64f65d4dd058c000dc7ad6f84dd3303baebdc3d1de3798445c6e1f26679"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                "a44d7ccbf1b35d87761771a6c175456b5b43bae032f3ba62ff40e820fdb33b7d",
                "75a4567f439360f6dae5ada487cacb30f037eb133a1616aa7197492f70b07495"},
        {"docs/SEMANTICS_CYCLE.md",
                "3aae047a39c38a3cedba0510c348137d282d72656da858300db914c72fdf9dd5",
                "e1c58aa2b08e0787ae47c7b4f1f2d5242bf9df31705eee0cead1cd73c088dafa"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java",
                "daff28406f903ad066fc90865acc210419133e7cde539398d1a18c591d27dfe7",
                "e0f385daaf7aeeaec9bddf93af8a732359a46965fe8b51e3dd94d325bd76c5ff"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java",
                "d4c643ccec070cb75b612dcf035d1b7c208ed5f984dd680129f7e0c8926b04cb",
                "d9132e8ba75e1abe32fcb1fe5580187ba9c13277f6c0e48f67e190b0c2a2432c"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java",
                "1b8e1c168a5773b1aa47975c482ab30a9a30fc877a4ac1747a446d95d7eed0e6",
                "1dab114d54ec29812273d5e2a36823e2c1fa36508ed11a45ee497449fb5bf563"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "17411fe81fbf959b16ca9e349bb732c12a6f8c3fbabb0ef5dae93fd3639a385e",
                "cc9033f7e59c4a18a2462dbd61d078d5c88976c0a2095cd9479ea5f88528586d"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "06214c272a99ab7c40f0d0ad383860e04fe6f0b6b746aef55ccc47314149c622",
                "01bfff223bfb4cdfdfb7df79ee153b14e4ff4ee39f658c4652771521e56f3043"},
        {"tools/harness/TrainPinMigration.java",
                "f08028a332e3d7f890e8c7e8004a02e0f46b5c14064e2b91af7ad5c56a9db9fa",
                "32925decb335a2f4152076103c2d490c24413ea492e74344a6fe63e037ef1534"}
    };

    private RedstoneAtlasTrainSourceSuccessor() { }

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
                    "redstone Atlas train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "redstone Atlas train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "redstone Atlas train successor allowlist drifted");
        RedstoneAtlasDocumentationSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
