/** Exact train successors introduced by the reviewed fluid Atlas integration. */
final class FluidAtlasTrainSourceSuccessor {
    private static final String[][] SUCCESSORS = {
        {"docs/SEMANTICS_CYCLE.md",
                "98b5b93b968bebc6900ade4bdff83da00d63d5894df07e21498a3b75b377a8de",
                "0f1ab0219b5a94d9855067cf265d453356221d8d4134d8959443bcd47af1df9b"},
        {"docs/generated/MILESTONES.md",
                "e55084a899ee2b6719ad571034e1a637389bce20db4619bdd65da2d9716b8c02",
                "277ada9b2471a208454a2a7264b8b989cc4053a10a99cdb40387bfdea03b5aae"},
        {"docs/generated/STATUS.md",
                "87f2915d19b939c829cde86622255a2596e82dd84b177c4d0fe98c421edf4416",
                "d3d828bc8d02d43048efbb2aa2a18b2c1c16296f84d45db8680f408b70f592c0"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java",
                "1f1ccd230c9a664d1240fc8f4b1e48f763d35ecf5cf9c9a894c0bcc1e93ea0b2",
                "89f97c82756b255cec6dcd4fdb41b05ae05aa54edb7ca555c9bc9ac8a40eee56"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                "d8903528386d183a98c9063d685839a301e7733b177caff536c78b98e5f47dfa",
                "f00d3ab0b949f34d24ac8e6168f043c05d12143ca33c8134bc7fe1be5ae37733"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticCatalog.java",
                "a821c7c3e56ca1d8b84766e453452307a847b54232d4dcbcb7063b51d2daeab2",
                "a5309c42833406a7b626a1595519e1fa54601cb41807e46550916a295ecf9e0a"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java",
                "df2b2f617f2f354f2185623c679bb49a38bad8e14755e5b985cc862128915d41",
                "fc49df973b7c489a122b890f52295ee7048cabef0eecb1ad7f0878eddb7c72b6"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java",
                "a921ac769d10ea303a3f29779820e5689f73b1ac39d04f164fb0e735a51c8b82",
                "8174d6507ef18f654adc8f4405dd178d9e15b3669f1b5c417a9450140b5304ef"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java",
                "bbc30b3f85fae24d99536c980a626b6982d5d53c242999903c3df232194d523c",
                "719f8e16cddf532e1151995dd2267d97feae49f420e794f04f3ba379da8d4b86"},
        {"release/testkit-artifacts.lock",
                "abcdd8baad3ef00a4190c0abea2e2812286656554d7179cdaa25a8c084bece69",
                "8b4d3bfa80227d938800b74c2836e50f677599862990194c714e3156bb032b28"},
        {"release/worldline.properties",
                "db47951a429d19aa2230a1e5f77ea33850e74262b7e8c5176a9237bcac0fe10c",
                "109749ff82be55709b990759989327e87d862dc85397f79093dcf51f44a951cf"},
        {"smokes/b173-flowing-fluid-lifecycle-cycle/smoke.properties",
                "46852c60ac2309f334d505df81ff74d164a6eec4ac1b805b322f4acd6736d77c",
                "205e36a67cdfe2be52fc99bbd03db0caf51528ef04f797aedb4506e4d2e8fcac"},
        {"smokes/b173-fluid-frozen-matter-lifecycle-conformance-cycle/smoke.properties",
                "5b714a370c704cd0fffcd030ff172926766fa5cbb142e2a4159f01a5196f2057",
                "630176a6224a8c0ad9248dd194058ecfd2fc5879184ebea257728127ab83c2cf"},
        {"smokes/b173-source-fluid-dynamics-cycle/smoke.properties",
                "9f63c75038e7fab8b2f7e5e372c1fdbc4210883e170052e858958f9401ed9330",
                "1812f154817bd6909f9f6f7966ddc38210ca1b19c832852eff900b153124392c"},
        {"smokes/b173-source-fluid-physical-envelope-cycle/smoke.properties",
                "fea8ae2fca6f9f33b157fe8038f2bc465b6b0ec78f532d4a1b1f73ea6b61e459",
                "5e57d03c880796a6501727988166271d461bacaf0a7924f269ed19f41b8ca4b7"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "d8163b11c4b0a28db96accfdb70fe06592b7c3160e36a8ebdc0ef5ade5be83f6",
                "d19a66f3ae6bb97985023242b6c760c751f399546aed6be6cdf3810e14c5d8e7"},
        {"tools/harness/TileEntityAtlasDocumentationSuccessor.java",
                "549ae6662ab5d08fb7e7fe7b92fe6e1ffd69b00c191a6953b83ff9446b157e38",
                "a94f491475e2a49c06e943a1a43fbda83dc189b095f25f4c9c0b6c6c9d9527ef"}
    };

    private FluidAtlasTrainSourceSuccessor() { }

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
                    "fluid Atlas train successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "fluid Atlas train successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "fluid Atlas train successor allowlist drifted");
        FluidAtlasDocumentationSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
