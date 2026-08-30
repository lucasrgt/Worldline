/** Exact reviewed successors for mod ecosystem semantics and Atlas coverage. */
final class ModEcosystemAtlasSuccessor {
    private static final String[][] SUCCESSORS = {
        {"release/testkit-artifacts.lock",
                "4d646f9bef864a733259f39ae8ef25e04720065ece5b6c6c8b45f32e73b48e6d",
                "afcec8bdc6d67beeb3dad1e9aa47f41e97711b76b2c46588597305dd5c377596"},
        {"release/worldline.properties",
                "46d8334401a3c07e2e5b5923abd255c08000b397db966659ce6c3a7549ac217b",
                "96b4e3c9cfca9c6cdb3f74b3b682f2479a2a5a5e674eda79158fe1c197a9e401"},
        {"docs/ATLAS.md",
                "4369aed02a275b1112d765f5eab8762dbbbf830df670b478f7c01c32d72dffbc",
                "d36448f0d2650064f7e709251471f487a2407b855db6eb5c656215e840e03387"},
        {"modules/atlas/src/main/java/worldline/atlas/AtlasSubsystems.java",
                "59bdebe18e849d03477f33a9f22ea54fc2d76b4913b7757fa610ba47b1a20548",
                "1f2a11dba8177660775f74e042de9f1357224215957bb7f033936c86061069fe"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasCoverageTest.java",
                "a54d5115406065e1676d059edfe292924d8fcc25e09f5877b6766be9e268308f",
                "707cb7db89e8a979bb0d0e43a12dbf8cca5ea002f4cbcb79d7839ad439433891"},
        {"docs/SEMANTICS_CYCLE.md",
                "936a1c25b80a30e5591a4104adbfffa556a5721b0c4810a3fde96de6f5caee5a",
                "05678b8a590fc84c91c739bb88901868e1804f3f8d6bafb9645f7881d02d39ef"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticCatalog.java",
                "8d7a8ff102cc4bf16d4f2137403fd594fab318c466a7c9ed556fb69f18f20a44",
                "6b7f5bda888599055a121ce0ea409b5a6b3af079ad82efe0ef0239b803cd67a8"},
        {"modules/semantics/src/main/java/worldline/semantics/SemanticRoles.java",
                "83655113d59930897ec18b464ade31f4f91dc85d6a8c5706e548d020b6748fc4",
                "a53833532bdc434af48a7dcf372cd916b4d7836797a61c24c018df8be8b23ee1"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticCatalogTest.java",
                "dd60964356c4eca715698b2ca2156bea688899cec658885ef44a5319f5318dda",
                "1013a69ed78848e091aa77415bc64b0f59c83c27afafd7b07b7878b116bf91fc"},
        {"modules/semantics/src/test/java/worldline/semantics/SemanticGraphTest.java",
                "b87cded252f1a088c31c202eb7ad94edf497853e8b017873c2c809c010d0a78a",
                "f3e74ae9afd7448912ab38c029b580c002a8f3f71c961252e4794fb903660c32"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "ebd423ee411bfc27c0daf2bc41a2eff9bcadef7a8e2a0d84a47b81c19cc9c1b3",
                "fde54406e4a019ad5ae19a1ace4dbaa82dd84b2f525e8bba2cc0649f6610b702"},
        {"tools/harness/AeroDocumentationSuccessor.java",
                "0ed5c9c0f023aad339f4f8bbc85478ded059e92d448e22e7d59a7a5892328442",
                "89cd3d10a514f1ae170391fdc12b5e3dc864f12f45060bbce44df9fb50d8b070"}
    };

    private ModEcosystemAtlasSuccessor() { }

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
                    "mod ecosystem Atlas successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "mod ecosystem Atlas successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "mod ecosystem Atlas successor allowlist drifted");
        ModEcosystemDocumentationSuccessor.selfTest();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
