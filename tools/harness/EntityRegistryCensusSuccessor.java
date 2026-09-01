/** Exact reviewed successors for the universal entity registry census. */
final class EntityRegistryCensusSuccessor {
    private static final String[][] SUCCESSORS = {
        {"behavior/functional-census/b1.7.3/claims.tsv",
                "47e089bdb36dbaac408663f3e611979134c7452a53b8238bff73c5b0ea28cdff",
                "10f690b1b0bd2ff6f4d9cf931a21cf285b967cea7d3bac880b7d0a9b4a2cdb3d"},
        {"behavior/functional-census/b1.7.3/entities/claims.tsv",
                "b1d2a2f10a419664b64b7580f846f3a17a3b2662890ef375a25ca965956bc674",
                "b8171c36e83f8c8cb4cdb4fec6b857bb26914694a85793e7ec71c6992e35415f"},
        {"behavior/functional-census/b1.7.3/entities/testkit-bindings.tsv",
                "ef9bf97a3f4dcf7a53d92c8f69156d23e9caa3ebb2ab718f97b73d48a0ec3f23",
                "91fc76dd926397e7453ecccef7b9ea82d7d946e6acf1e706dd7ff8ea4a49bb87"},
        {"behavior/functional-census/families.tsv",
                "51f9a2c6beb4720f58a890bfcc661507fb8ab290ddea8f99f6889523a44b4b11",
                "b70755d5878041afd1055bfa68b912d76a8f64ca4c4f577a329f126219e37f10"},
        {"docs/ATLAS.md",
                "93e94841c73ede0667e8bd011cf6e189317c84f52fa150ca1d0b0b4c7e4b99ed",
                "e140c94aa7ace1cb1c7f21ae5eea81fbaa50f27989bc00a0483e12259d3c7025"},
        {"docs/FUNCTIONAL_CENSUS.md",
                "c9c3dc5dc0167d5db5b144ab8aaa70d6c59968a1ed96706a5e9031848c18cae5",
                "0bcc3e5cb9587b3af8eca8ec4b249ec3ca2ec8775047d9926130ed29d5d98b6c"},
        {"docs/TESTKIT.md",
                "233206a2dd7d3d4d15dbec1a34ec62af5c16a6e4a2d150283fbbd70d6b06537d",
                "d60bccc6ab3c5fbee227e172656188ddc24bdc9233fcbc2e2a3c4680adca0d2b"},
        {"modules/atlas/src/test/java/worldline/atlas/AtlasStoreTest.java",
                "c9e3756c416470a23311e3913b37025ca961e8ec806b8a5dbe527a81803541ee",
                "23c7689bc32f20a5ddc6cd0de1c9c2cb580c3e8de3d105a7cc175b28f56597af"},
        {"modules/cli/src/main/java/worldline/cli/CensusCommand.java",
                "a79923fe8ee2ae3beda49fdb7f45e346eaaa08c47a15dd21004269d3de2f613d",
                "50466e319b68078fd0b34418b8f43e30a9a9a5dd50d26e77c7cc9d1eeac2ccf5"},
        {"modules/testkit/src/test/java/worldline/testkit/TestKitContractTest.java",
                "16e285747241cbe189d36445327f77bec751510576aa839b36a88d24fde32f5c",
                "a22fce8801724c598423d3b7f94913e7e41dd39023959ee06171a6c5e71ffb01"},
        {"release/testkit-artifacts.lock",
                "ab9bcdbee1f6f519e3f20162600991b57a18f749d7df33cd488a4cb10872beba",
                "44989fe93025ff21e456cc3b70dffb7f3d152a997aa7da9f9771759a86270cbc"},
        {"smokes/census-cycle/smoke.properties",
                "0dc9b10c0455736bed7386853f741c03ffba746549bd8ec159fe7fbda23e2720",
                "5a9ead25655036baddbdc2e36ab706f8ef5c435737a6dce50d3379ac5c28903b"},
        {"tools/smoke/CensusCycle.java",
                "0f2ac75999f2158154a939d8a84c2d22c3d4002c14cbc79c5d43a21607819480",
                "59383fbd4bc42c52db9988465c221bee083375370102ebb3311a18562679f3a2"},
        {"tools/harness/TrainGeneratedDocumentationMigration.java",
                "a2ac76e433d6ff6808583b718d73db9cafe618051f4072df33ef3766300fbea3",
                "027bfd12c7370a0e10d8fb1b4a14ac49d2ec536365348935246512d1ac045a89"},
        {"tools/harness/HarnessFeatureSelfTest.java",
                "4c845306ee1c2ee1fd01be017464798589c401efd27d565d6c73bc9a7835e6ca",
                "c77efe1f04a2cefa35b3ae4cfb53d95c88894d6c9cf21518f872907b70e3c50e"}
    };

    private EntityRegistryCensusSuccessor() { }

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
                    "entity registry census successor rejected " + successor[0]);
            require(!carries(successor[0], successor[1], "unreviewed"),
                    "entity registry census successor accepted drift " + successor[0]);
        }
        require(!carries("unreviewed", "old", "new"),
                "entity registry census successor allowlist drifted");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
