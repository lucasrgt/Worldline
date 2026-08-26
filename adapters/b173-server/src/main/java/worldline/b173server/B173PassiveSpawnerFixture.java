package worldline.b173server;

import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteWorldView;

/** Verifies one observed vanilla passive-animal spawn cell before a Packet24 wait. */
public final class B173PassiveSpawnerFixture {
    private static final int AIR = 0;
    private static final int GRASS = 2;

    private B173PassiveSpawnerFixture() {
    }

    public static void verifyGrassSpawnCell(RemoteWorldView view, BlockPosition support) {
        if (view == null || support == null) {
            throw new IllegalArgumentException("passive spawner observation is required");
        }
        BlockState floor = view.blockAt(support.x(), support.y(), support.z());
        BlockState body = view.blockAt(support.x(), support.y() + 1, support.z());
        BlockState head = view.blockAt(support.x(), support.y() + 2, support.z());
        require(floor.legacyId() == GRASS && floor.metadata() == 0,
                "passive spawner grass substrate absent");
        require(body.legacyId() == AIR && head.legacyId() == AIR,
                "passive spawner vertical clearance absent");
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
