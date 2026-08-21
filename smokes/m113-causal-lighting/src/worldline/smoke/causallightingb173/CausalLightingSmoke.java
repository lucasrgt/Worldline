package worldline.smoke.causallightingb173;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Adds one official glowstone source and observes server-authored light after a fresh chunk send. */
public final class CausalLightingSmoke {
    private CausalLightingSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 8) throw new IllegalArgumentException(
                "usage: CausalLightingSmoke server.jar workspace port seed username chunkX chunkZ sourceId");
        Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
        int port = Integer.parseInt(arguments[2]), chunkX = Integer.parseInt(arguments[5]);
        int chunkZ = Integer.parseInt(arguments[6]), sourceId = Integer.parseInt(arguments[7]);
        long seed = Long.parseLong(arguments[3]); String username = arguments[4];
        Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
        B173WireClient actor = new B173WireClient("127.0.0.1", port, username, timeout), reader = null;
        RemoteChunkSnapshot before, after; BlockPosition support, target;
        try { server.boot(); B173PlayerSeed.writeHolding(workspace, username, 8.5D, 120D, 8.5D, sourceId, 1, 0);
            actor.connect(); PlayerPose pose = actor.synchronizePose(); RemoteInventoryView inventory = actor.awaitInventory();
            require(inventory.occupiedSlots() == 1 && inventory.slot(36).item().equals(
                    new RemoteItemStack(sourceId, 1, 0)), "seeded source inventory drift");
            before = actor.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            support = support(before, chunkX, chunkZ); target = BlockFace.UP.adjacent(support);
            pose = actor.moveAndObserve(target.x() + 0.5D - pose.x(), 0D,
                    target.z() + 0.5D - pose.z(), 3).resulting();
            while (pose.y() > target.y() + 3D) { MovementOutcome move = actor.moveAndObserve(0D, -1D, 0D, 1);
                pose = move.resulting(); require(!move.corrected() || pose.y() <= target.y() + 4D,
                        "descent corrected above placement range"); }
            actor.selectHeldSlot(0); actor.placeHeldBlock(support, BlockFace.UP);
            actor.awaitBlock(target, new BlockState(sourceId, 0)); actor.sustainTicks(40);
            actor.close(); awaitPlayers(server, 0); server.save();
            reader = new B173WireClient("127.0.0.1", port, username, timeout); reader.connect();
            reader.synchronizePose(); after = reader.awaitRemoteChunk(chunkX, chunkZ).chunkAt(chunkX, chunkZ);
            require(before.blockAt(local(target.x(), chunkX), target.y(), local(target.z(), chunkZ)).legacyId() != sourceId
                    && after.blockAt(local(target.x(), chunkX), target.y(), local(target.z(), chunkZ)).legacyId()
                    == sourceId, "source block transition drift");
        } finally { actor.close(); if (reader != null) reader.close(); server.close(); }
        Delta block = delta(before, after, false), sky = delta(before, after, true);
        int lx = local(target.x(), chunkX), lz = local(target.z(), chunkZ);
        require(block.changed > 0 && block.increased > 0 && before.blockLightAt(lx, target.y(), lz) == 0
                && after.blockLightAt(lx, target.y(), lz) == 15, "causal block-light source absent");
        String evidence = "target=" + target.x() + ":" + target.y() + ":" + target.z()
                + ",support=" + before.blockAt(lx, support.y(), lz).legacyId()
                + ",block=" + block + ",sky=" + sky;
        String trace = "v1|server=official-b1.7.3|seed=" + seed + "|chunk=" + chunkX + "," + chunkZ
                + "|intervention=packet15-glowstone89|confirmation=packet53|settle=40ticks"
                + "|observation=fresh-login-packet51|" + evidence + "|disconnect=clean";
        System.out.println("WORLDLINE_M113_LIGHT=" + evidence);
        System.out.println("WORLDLINE_M113_TRACE=" + trace);
        System.out.println("WORLDLINE_M113_SIGNATURE=" + sha256(trace));
    }

    private static BlockPosition support(RemoteChunkSnapshot chunk, int chunkX, int chunkZ) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++) for (int y = 126; y >= 1; y--)
            if (solid(chunk.blockAt(x, y, z).legacyId()) && replaceable(chunk.blockAt(x, y + 1, z).legacyId()))
                return new BlockPosition(chunkX * 16 + x, y, chunkZ * 16 + z);
        throw new IllegalStateException("no deterministic source support");
    }
    private static boolean solid(int id) { return id != 0 && id != 8 && id != 9 && id != 10 && id != 11
            && id != 31 && id != 37 && id != 38 && id != 39 && id != 40 && id != 78; }
    private static boolean replaceable(int id) { return id == 0 || id == 8 || id == 9 || id == 10
            || id == 11 || id == 78; }
    private static Delta delta(RemoteChunkSnapshot before, RemoteChunkSnapshot after, boolean sky) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); ByteBuffer row = ByteBuffer.allocate(8);
        int changed = 0, increased = 0, decreased = 0, max = 0;
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 0; y < 128; y++) {
            int a = sky ? before.skyLightAt(x, y, z) : before.blockLightAt(x, y, z);
            int b = sky ? after.skyLightAt(x, y, z) : after.blockLightAt(x, y, z);
            if (a != b) { changed++; if (b > a) increased++; else decreased++; max = Math.max(max, Math.abs(b - a));
                row.clear(); row.putShort((short) x).putShort((short) y).putShort((short) z).put((byte) a).put((byte) b);
                digest.update(row.array()); }
        } return new Delta(changed, increased, decreased, max, hex(digest.digest()));
    }
    private static int local(int value, int chunk) { return value - chunk * 16; }
    private static void awaitPlayers(B173DedicatedServer server, int count) throws Exception { long end=System.currentTimeMillis()+5000;
        while(System.currentTimeMillis()<end){if(server.players().size()==count)return;Thread.sleep(100);}throw new IllegalStateException("player count drift"); }
    private static String sha256(String value) throws Exception { return hex(MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8))); }
    private static String hex(byte[] value) { StringBuilder result=new StringBuilder();
        for(byte item:value)result.append(String.format("%02x",item&255));return result.toString(); }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private static final class Delta { final int changed,increased,decreased,max; final String hash;
        Delta(int c,int i,int d,int m,String h){changed=c;increased=i;decreased=d;max=m;hash=h;}
        @Override public String toString(){return changed+":"+increased+":"+decreased+":"+max+":"+hash;} }
}
