package worldline.smoke.suffocationsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.b173server.B173WireClient;

/** Raised-stone pad, falling-sand bury helpers, and SHA for the suffocation SET. */
final class SuffocationSetSupport {
    private SuffocationSetSupport() {}

    static BlockPosition place(B173WireClient actor, BlockPosition support, BlockFace face, int id)
            throws Exception {
        BlockPosition target = face.adjacent(support);
        actor.placeHeldBlock(support, face);
        actor.awaitBlock(target, new BlockState(id, 0));
        return target;
    }

    static PlayerPose walk(B173WireClient actor, PlayerPose here, double x, double y, double z)
            throws Exception {
        int step = 0;
        while (step < 32) {
            double dx = x - here.x();
            double dy = y - here.y();
            double dz = z - here.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist <= 0.05D) {
                return here;
            }
            double scale = Math.min(1D, 9D / Math.max(dist, 1.0E-9D));
            here = actor.moveAndObserve(dx * scale, dy * scale, dz * scale, 1).resulting();
            step++;
        }
        throw new IllegalStateException("suffocation walk failed x=" + here.x() + " y=" + here.y()
                + " z=" + here.z());
    }

    static BlockPosition foundation(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
        int x = 4;
        while (x <= 11) {
            int z = 4;
            while (z <= 11) {
                int y = 126;
                while (y >= 1) {
                    if (chunk.blockAt(x, y, z).legacyId() == 3
                            && water(chunk.blockAt(x, y + 1, z).legacyId())) {
                        return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
                    }
                    y--;
                }
                z++;
            }
            x++;
        }
        throw new IllegalStateException("no deterministic suffocation foundation");
    }

    static boolean water(int id) {
        return id == 8 || id == 9;
    }

    static int local(int value, int chunk) {
        return value - chunk * 16;
    }

    static String sha(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte item : bytes) {
            result.append(String.format("%02x", item & 255));
        }
        return result.toString();
    }

    static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }
}
