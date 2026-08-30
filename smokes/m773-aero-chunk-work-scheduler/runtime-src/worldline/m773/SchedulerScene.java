package worldline.m773;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.entity.player.ClientPlayerEntity;

/** Selects and repeatedly invalidates real visible and hidden chunk builders. */
final class SchedulerScene {
    private static ChunkBuilder[] visible, hidden;

    private SchedulerScene() {}

    static void select(Minecraft game) {
        ChunkBuilder[] chunks = ((SchedulerRendererStats) game.worldRenderer).worldlineChunks();
        List<ChunkBuilder> shown = new ArrayList<ChunkBuilder>();
        List<ChunkBuilder> concealed = new ArrayList<ChunkBuilder>();
        for (ChunkBuilder chunk : chunks) {
            if (chunk == null || chunk.y != 64) continue;
            (chunk.inFrustum ? shown : concealed).add(chunk);
        }
        require(shown.size() >= 8 && concealed.size() >= 8,
                "M773 visible/hidden fixture unavailable: " + shown.size() + "/" + concealed.size());
        visible = shown.subList(0, 8).toArray(new ChunkBuilder[8]);
        hidden = concealed.subList(0, 8).toArray(new ChunkBuilder[8]);
        SchedulerProbe.target(hidden[0]);
    }

    static void priority(Minecraft game, int frame) {
        if (frame == 1) dirty(game, hidden[0]);
        dirty(game, visible[frame % visible.length]);
    }

    static void stress(Minecraft game) {
        for (ChunkBuilder chunk : visible) dirty(game, chunk);
        for (ChunkBuilder chunk : hidden) dirty(game, chunk);
    }

    static void place(ClientPlayerEntity player, int frame, boolean moving) {
        float yaw = moving ? (float) ((frame * 7) % 360) : 45.0F;
        float pitch = moving ? (float) (Math.sin(frame * 0.09D) * 25.0D) : 4.0F;
        player.velocityX = player.velocityY = player.velocityZ = 0.0D;
        player.setPositionAndAngles(8.5D, 65.0D, 8.5D, yaw, pitch);
    }

    private static void dirty(Minecraft game, ChunkBuilder chunk) {
        int x = chunk.x + 8, y = chunk.y + 8, z = chunk.z + 8;
        game.worldRenderer.setBlocksDirty(x, y, z, x, y, z);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
