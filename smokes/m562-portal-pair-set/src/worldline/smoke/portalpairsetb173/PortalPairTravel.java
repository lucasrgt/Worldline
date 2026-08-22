package worldline.smoke.portalpairsetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Discovers generated portal 90 geometry and performs one official Packet9 crossing. */
final class PortalPairTravel {
    private PortalPairTravel() {}

    static PlayerPose enter(B173WireClient actor, PlayerPose pose, double x, double y, double z, int travel,
            int dimension) throws Exception {
        pose = actor.moveAndObserve(x - pose.x(), y - pose.y(), z - pose.z(), 1).resulting();
        actor.sustainTicks(travel);
        PortalPairFrames.require(actor.awaitDimension(dimension) == dimension,
                "portal-pair Packet9 " + dimension + " absent");
        return actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
    }

    static PlayerPose leave(B173WireClient actor, Portal portal, PlayerPose pose) throws Exception {
        double x = portal.minX + 0.5D, z = portal.minZ + 0.5D, y = portal.minY;
        if (portal.maxX > portal.minX) z += 2.5D; else x += 2.5D;
        pose = actor.moveAndObserve(x - pose.x(), y - pose.y(), z - pose.z(), 5).resulting();
        PortalPairFrames.require(actor.dimension() == portal.stayDimension,
                "left portal changed dimension early");
        return pose;
    }

    static PlayerPose walk(B173WireClient actor, PlayerPose pose, double x, double y, double z) throws Exception {
        for (int step = 0; step < 48; step++) {
            double dx = x - pose.x(), dy = y - pose.y(), dz = z - pose.z();
            if (Math.abs(dx) <= 1.1D && Math.abs(dy) <= 1.1D && Math.abs(dz) <= 1.1D) break;
            pose = actor.moveAndObserve(clamp(dx), clamp(dy), clamp(dz), 1).resulting();
        }
        return actor.moveAndObserve(clamp(x - pose.x()), clamp(y - pose.y()), clamp(z - pose.z()), 1)
                .resulting();
    }

    private static double clamp(double value) {
        if (value > 1D) return 1D;
        if (value < -1D) return -1D;
        return value;
    }

    static Portal find(RemoteWorldView view, PlayerPose pose, int stayDimension) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = 999, maxY = -1,
                minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE, count = 0;
        for (RemoteChunkSnapshot chunk : view.chunks()) {
            int baseX = chunk.observation().x(), baseZ = chunk.observation().z();
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 0; y < 128; y++)
                if (chunk.blockAt(x, y, z).legacyId() == 90 && Math.abs(baseX + x - pose.x()) < 8D
                        && Math.abs(y - pose.y()) < 8D && Math.abs(baseZ + z - pose.z()) < 8D) {
                    count++;
                    minX = Math.min(minX, baseX + x); maxX = Math.max(maxX, baseX + x);
                    minY = Math.min(minY, y); maxY = Math.max(maxY, y);
                    minZ = Math.min(minZ, baseZ + z); maxZ = Math.max(maxZ, baseZ + z);
                }
        }
        PortalPairFrames.require(count == 6 && maxY - minY == 2
                && ((maxX - minX == 1 && maxZ == minZ) || (maxZ - minZ == 1 && maxX == minX)),
                "near-pose portal geometry drift " + count);
        return new Portal(minX, maxX, minY, minZ, maxZ, count, stayDimension);
    }

    static int nearPortalBlocks(RemoteWorldView view, PlayerPose pose) {
        int count = 0;
        for (RemoteChunkSnapshot chunk : view.chunks()) {
            int baseX = chunk.observation().x(), baseZ = chunk.observation().z();
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 0; y < 128; y++)
                if (chunk.blockAt(x, y, z).legacyId() == 90 && Math.abs(baseX + x - pose.x()) < 16D
                        && Math.abs(baseZ + z - pose.z()) < 16D) count++;
        }
        return count;
    }

    static int frame(RemoteWorldView view, Portal portal) {
        int count = 0;
        if (portal.maxX > portal.minX) {
            for (int x = portal.minX - 1; x <= portal.maxX + 1; x++) {
                if (view.blockAt(x, portal.minY - 1, portal.minZ).legacyId() == 49) count++;
                if (view.blockAt(x, portal.minY + 3, portal.minZ).legacyId() == 49) count++;
            }
            for (int y = portal.minY; y <= portal.minY + 2; y++) {
                if (view.blockAt(portal.minX - 1, y, portal.minZ).legacyId() == 49) count++;
                if (view.blockAt(portal.maxX + 1, y, portal.minZ).legacyId() == 49) count++;
            }
        } else {
            for (int z = portal.minZ - 1; z <= portal.maxZ + 1; z++) {
                if (view.blockAt(portal.minX, portal.minY - 1, z).legacyId() == 49) count++;
                if (view.blockAt(portal.minX, portal.minY + 3, z).legacyId() == 49) count++;
            }
            for (int y = portal.minY; y <= portal.minY + 2; y++) {
                if (view.blockAt(portal.minX, y, portal.minZ - 1).legacyId() == 49) count++;
                if (view.blockAt(portal.minX, y, portal.maxZ + 1).legacyId() == 49) count++;
            }
        }
        return count;
    }

    static int sky(RemoteChunkSnapshot chunk) {
        int count = 0;
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 0; y < 128; y++)
            if (chunk.skyLightAt(x, y, z) > 0) count++;
        return count;
    }

    static void awaitPlayers(B173DedicatedServer server, int count) throws Exception {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            if (server.players().size() == count) return;
            Thread.sleep(100);
        }
        throw new IllegalStateException("player count drift");
    }

    static String sha(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte item : digest) hex.append(String.format("%02x", item & 255));
        return hex.toString();
    }

    static final class Portal {
        final int minX, maxX, minY, minZ, maxZ, count, stayDimension;
        Portal(int minX, int maxX, int minY, int minZ, int maxZ, int count, int stayDimension) {
            this.minX = minX; this.maxX = maxX; this.minY = minY; this.minZ = minZ; this.maxZ = maxZ;
            this.count = count; this.stayDimension = stayDimension;
        }
        boolean sameExit(Portal other) {
            return other != null && minX == other.minX && maxX == other.maxX && minY == other.minY
                    && minZ == other.minZ && maxZ == other.maxZ && count == other.count;
        }
    }
}
