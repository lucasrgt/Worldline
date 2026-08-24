package worldline.smoke.chunkreloadb173;

import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.b173server.B173WireClient;

/** Metadata-neutral placement wait for the oriented furnace in the M627 fixture. */
final class M627FixtureSupport {
    private M627FixtureSupport() { }

    static BlockPosition placeLegacy(B173WireClient client, BlockPosition support,
            BlockFace face, int legacyId) throws Exception {
        BlockPosition target = face.adjacent(support);
        client.placeHeldBlock(support, face);
        worldline.test.WorldlineSmokeAwait.awaitEntity(client,
                () -> client.sustainTicks(1).blockAt(target.x(), target.y(), target.z()),
                state -> state.legacyId() == legacyId, "oriented block placement", 40);
        return target;
    }
}
