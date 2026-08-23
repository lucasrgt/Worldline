package worldline.smoke.mushroomspreadsetb173;

import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173WireClient;

/** Bounded random-tick wait and fresh-login persist checks for mushroom spread. */
public final class MushroomSpreadSetWait {
    static void waitSpread(B173WireClient a, BlockPosition[] sources, BlockPosition[] targets,
            BlockPosition glassAir, int window, int windows) throws Exception {
        RemoteWorldView spread = worldline.test.WorldlineSmokeAwait.awaitWorld(a,
                v -> anySpread(v, targets), "dark opaque mushroom spread", window * windows);
        int i = 0;
        while (i < sources.length) {
            int n = MushroomSpreadSetArm.id(spread, sources[i]);
            MushroomSpreadSetArm.require(n == 39 || n == 40, "source mushroom died during wait");
            i++;
        }
        MushroomSpreadSetArm.require(MushroomSpreadSetArm.id(spread, glassAir) == 0,
                "glass cell converted during wait");
    }

    static void persist(RemoteChunkSnapshot after, int cx, int cz, BlockPosition[] sources,
            BlockPosition[] targets, BlockPosition glass, BlockPosition glassAir,
            BlockPosition cover) {
        int i = 0;
        while (i < sources.length) {
            int n = MushroomSpreadSetArm.at(after, sources[i], cx, cz).legacyId();
            MushroomSpreadSetArm.require(n == 39 || n == 40, "source mushroom persist drift");
            i++;
        }
        MushroomSpreadSetArm.require(MushroomSpreadSetArm.at(after, glass, cx, cz)
                .equals(new BlockState(20, 0)), "glass persist drift");
        MushroomSpreadSetArm.require(MushroomSpreadSetArm.at(after, glassAir, cx, cz).legacyId() == 0,
                "glass air persist drift");
        MushroomSpreadSetArm.require(MushroomSpreadSetArm.at(after, cover, cx, cz)
                .equals(new BlockState(1, 0)), "roof persist drift");
        boolean spread = false;
        i = 0;
        while (i < targets.length) {
            int n = MushroomSpreadSetArm.at(after, targets[i], cx, cz).legacyId();
            MushroomSpreadSetArm.require(n == 0 || n == 39 || n == 40, "target persist drift " + n);
            if (n == 39 || n == 40) spread = true;
            i++;
        }
        MushroomSpreadSetArm.require(spread, "dark opaque air did not persist a spread mushroom");
    }

    static boolean anySpread(RemoteWorldView v, BlockPosition[] targets) {
        int i = 0;
        while (i < targets.length) {
            int n = MushroomSpreadSetArm.id(v, targets[i]);
            if (n == 39 || n == 40) return true;
            i++;
        }
        return false;
    }
}
