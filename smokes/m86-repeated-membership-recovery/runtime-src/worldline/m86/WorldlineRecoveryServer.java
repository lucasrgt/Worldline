package worldline.m86;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import worldline.m74.WorldlineCensusBlockEntity;
import worldline.m74.WorldlineCensusMod;

/** Validates two exact natural remove/restore generations on the same cell. */
public final class WorldlineRecoveryServer {
    private static int step; private static int x, y, z, root;
    private WorldlineRecoveryServer() {}
    public static synchronized void change(PlayerEntity player, int[] values) {
        int expected = Integer.getInteger("worldline.census.nonce", 0); if (!(player instanceof ServerPlayerEntity server) || values == null || values.length != 6
                || values[3] != expected || expected <= 0 || values[4] != step / 2 + 1 || values[5] != step % 2 + 1) throw new IllegalStateException("invalid M86 request");
        if (values[5] == 1) remove(player, values); else restore(player, server, values); step++;
        MessagePacket ack = new MessagePacket(WorldlineRecoveryMod.CHANGE); ack.ints = values.clone(); server.networkHandler.sendPacket(ack);
    }
    private static void remove(PlayerEntity player, int[] values) {
        x = values[0]; y = values[1]; z = values[2]; root = values[3];
        if (player.world.getBlockId(x, y, z) != WorldlineCensusMod.block.id || !(player.world.getBlockEntity(x, y, z) instanceof WorldlineCensusBlockEntity be)
                || be.nonce() != root * 100 + 1) throw new IllegalStateException("M86 remove target drift");
        if (!player.world.setBlock(x, y, z, 0) || player.world.getBlockId(x, y, z) != 0 || player.world.getBlockEntity(x, y, z) != null)
            throw new IllegalStateException("M86 removal rejected");
        System.out.println("[WorldlineRecovery] removed generation=" + values[4] + " x=" + x + " y=" + y + " z=" + z + " nonce=" + root);
    }
    private static void restore(PlayerEntity player, ServerPlayerEntity server, int[] values) {
        if (values[0] != x || values[1] != y || values[2] != z || values[3] != root || player.world.getBlockId(x, y, z) != 0
                || player.world.getBlockEntity(x, y, z) != null) throw new IllegalStateException("M86 restore target drift");
        if (!player.world.setBlock(x, y, z, WorldlineCensusMod.block.id) || !(player.world.getBlockEntity(x, y, z) instanceof WorldlineCensusBlockEntity be))
            throw new IllegalStateException("M86 restoration rejected");
        int nonce = root * 100 + 1; be.setNonce(nonce); MessagePacket state = new MessagePacket(WorldlineRecoveryMod.RESTORE);
        state.ints = new int[]{x, y, z, nonce, values[4]}; server.networkHandler.sendPacket(state);
        System.out.println("[WorldlineRecovery] restored generation=" + values[4] + " x=" + x + " y=" + y + " z=" + z + " nonce=" + root);
    }
}
