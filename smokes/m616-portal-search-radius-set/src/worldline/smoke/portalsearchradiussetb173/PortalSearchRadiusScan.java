package worldline.smoke.portalsearchradiussetb173;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Discovers portal 90 geometry and walks with an 8-block Packet13 cap. */
final class PortalSearchRadiusScan {
    private PortalSearchRadiusScan() {}

    static PlayerPose enter(B173WireClient actor, PlayerPose pose, double x, double y, double z,
            int travel, int dimension) throws Exception {
        pose = go(actor, pose, x, y, z);
        worldline.test.WorldlineSmokeAwait.observe(actor, travel);
        PortalSearchRadiusKit.require(actor.awaitDimension(dimension) == dimension,
                "portal-search-radius Packet9 " + dimension + " absent");
        return actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
    }

    static PlayerPose go(B173WireClient actor, PlayerPose pose, double x, double y, double z)
            throws Exception {
        int steps = 0;
        while (Math.abs(pose.x() - x) + Math.abs(pose.y() - y) + Math.abs(pose.z() - z) > 0.4D) {
            PortalSearchRadiusKit.require(++steps <= 800, "portal-search-radius relocate stalled");
            double dx = x - pose.x(), dy = y - pose.y(), dz = z - pose.z();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 8D) { dx = dx * 8D / dist; dy = dy * 8D / dist; dz = dz * 8D / dist; }
            PlayerPose next = actor.moveAndObserve(dx, dy, dz, 1).resulting();
            if (Math.abs(next.x() - pose.x()) + Math.abs(next.y() - pose.y())
                    + Math.abs(next.z() - pose.z()) < 0.05D)
                next = actor.moveAndObserve(0D, 8D, 0D, 1).resulting();
            pose = next;
        }
        return pose;
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
        PortalSearchRadiusKit.require(count == 6 && maxY - minY == 2
                && ((maxX - minX == 1 && maxZ == minZ) || (maxZ - minZ == 1 && maxX == minX)),
                "near-pose portal geometry drift " + count);
        return new Portal(minX, maxX, minY, minZ, maxZ, count, stayDimension);
    }

    static int sky(RemoteChunkSnapshot chunk) {
        int count = 0;
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 0; y < 128; y++)
            if (chunk.skyLightAt(x, y, z) > 0) count++;
        return count;
    }

    static int near90(RemoteWorldView view, int originX, int originZ, int radius) {
        int count = 0;
        for (RemoteChunkSnapshot chunk : view.chunks()) {
            int baseX = chunk.observation().x(), baseZ = chunk.observation().z();
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 0; y < 128; y++)
                if (chunk.blockAt(x, y, z).legacyId() == 90
                        && Math.abs(baseX + x - originX) <= radius
                        && Math.abs(baseZ + z - originZ) <= radius) count++;
        }
        return count;
    }

    static boolean same(Portal left, Portal right) {
        return left.minX == right.minX && left.minY == right.minY && left.minZ == right.minZ;
    }

    static String prove(Portal existing, PlayerPose dest, RemoteWorldView world, int shift) {
        Portal arrived = find(world, dest, -1);
        int scaledX = existing.minX + shift, scaledZ = existing.minZ;
        int dist = Math.max(Math.abs(arrived.minX - scaledX), Math.abs(arrived.minZ - scaledZ));
        PortalSearchRadiusKit.require(sky(world.chunkAt(((int) Math.floor(dest.x())) >> 4,
                ((int) Math.floor(dest.z())) >> 4)) == 0, "search destination is not Nether");
        PortalSearchRadiusKit.require(same(arrived, existing) && arrived.count == 6,
                "search trip missed the existing Nether frame");
        PortalSearchRadiusKit.require(dist > PortalSearchRadiusKit.CREATE
                && dist <= PortalSearchRadiusKit.SEARCH,
                "existing frame is outside the search-vs-create window dist=" + dist);
        PortalSearchRadiusKit.require(near90(world, scaledX, scaledZ, PortalSearchRadiusKit.CREATE) == 0,
                "scaled destination grew a new portal 90 frame");
        return "dimensions=0->-1,shift=" + shift + ",search=existing,radius="
                + PortalSearchRadiusKit.SEARCH + ",create-window=" + PortalSearchRadiusKit.CREATE
                + ",found=6x90,created=0,obsidian=49,portal=90,not-m560-scale-only,"
                + "not-m563-create,not-m562-pair,persisted=true,clients=3,disconnect=clean,"
                + "packet9=0->-1";
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
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte item : digest) hex.append(String.format("%02x", item & 255));
        return hex.toString();
    }

    static final class Portal {
        final int minX, maxX, minY, minZ, maxZ, count, stayDimension;
        Portal(int minX, int maxX, int minY, int minZ, int maxZ, int count, int stayDimension) {
            this.minX = minX; this.maxX = maxX; this.minY = minY; this.minZ = minZ;
            this.maxZ = maxZ; this.count = count; this.stayDimension = stayDimension;
        }
    }
}
