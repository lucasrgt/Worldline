package worldline.smoke.netherexitcreatesetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Discovers portal 90 geometry and walks with an 8-block Packet13 cap. */
final class NetherExitPortalScan {
    private NetherExitPortalScan() {}

    static PlayerPose enter(B173WireClient actor, PlayerPose pose, double x, double y, double z, int travel,
            int dimension) throws Exception {
        pose = go(actor, pose, x, y, z);
        actor.sustainTicks(travel);
        NetherExitPortalKit.require(actor.awaitDimension(dimension) == dimension,
                "nether-exit-create Packet9 " + dimension + " absent");
        return actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
    }

    static PlayerPose leave(B173WireClient actor, Portal portal, PlayerPose pose, int cooldown) throws Exception {
        double x = portal.minX + 0.5D, z = portal.minZ + 0.5D, y = portal.minY;
        if (portal.maxX > portal.minX) z += 2.5D; else x += 2.5D;
        pose = actor.moveAndObserve(x - pose.x(), y - pose.y(), z - pose.z(), 5).resulting();
        NetherExitPortalKit.require(actor.dimension() == portal.stayDimension, "left portal changed dimension early");
        actor.sustainTicks(cooldown);
        return actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
    }

    static PlayerPose go(B173WireClient actor, PlayerPose pose, double x, double y, double z) throws Exception {
        int steps = 0;
        while (Math.abs(pose.x() - x) + Math.abs(pose.y() - y) + Math.abs(pose.z() - z) > 0.4D) {
            NetherExitPortalKit.require(++steps <= 800, "nether-exit-create relocate stalled");
            double dx = x - pose.x(), dy = y - pose.y(), dz = z - pose.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 8D) { dx = dx * 8D / dist; dy = dy * 8D / dist; dz = dz * 8D / dist; }
            PlayerPose next = actor.moveAndObserve(dx, dy, dz, 1).resulting();
            if (Math.abs(next.x() - pose.x()) + Math.abs(next.y() - pose.y()) + Math.abs(next.z() - pose.z()) < 0.05D)
                next = actor.moveAndObserve(0D, 8D, 0D, 1).resulting();
            pose = next;
        }
        return pose;
    }

    static PlayerPose hoverUntilChunk(B173WireClient actor, PlayerPose pose, int chunkX, int chunkZ)
            throws Exception {
        for (int n = 0; n < 80; n++) {
            pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
            if (actor.sustainTicks(1).containsChunk(chunkX, chunkZ)) return pose;
        }
        throw new IllegalStateException("nether-exit-create far chunk absent");
    }

    static Portal find(RemoteWorldView view, PlayerPose pose, int stayDimension) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = 999, maxY = -1,
                minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE, count = 0;
        for (RemoteChunkSnapshot chunk : view.chunks()) {
            int baseX = chunk.observation().x(), baseZ = chunk.observation().z();
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 0; y < 128; y++)
                if (chunk.blockAt(x, y, z).legacyId() == 90 && Math.abs(baseX + x - pose.x()) < 16D
                        && Math.abs(y - pose.y()) < 16D && Math.abs(baseZ + z - pose.z()) < 16D) {
                    count++;
                    minX = Math.min(minX, baseX + x); maxX = Math.max(maxX, baseX + x);
                    minY = Math.min(minY, y); maxY = Math.max(maxY, y);
                    minZ = Math.min(minZ, baseZ + z); maxZ = Math.max(maxZ, baseZ + z);
                }
        }
        NetherExitPortalKit.require(count == 6 && maxY - minY == 2
                && ((maxX - minX == 1 && maxZ == minZ) || (maxZ - minZ == 1 && maxX == minX)),
                "near-pose portal geometry drift " + count);
        return new Portal(minX, maxX, minY, minZ, maxZ, count, stayDimension);
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

    static boolean sameSource(Portal created, NetherExitPortalKit.Frame source) {
        return created.minX == source.bottom.x() + 1 && created.minZ == source.bottom.z()
                && created.minY == source.bottom.y() + 1;
    }

    static int rangeChebyshev(Portal created, NetherExitPortalKit.Frame source) {
        int dx = Math.abs(created.minX - (source.bottom.x() + 1));
        int dz = Math.abs(created.minZ - source.bottom.z());
        return Math.max(dx, dz);
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
    }
}
