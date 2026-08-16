package worldline.b173;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityRenderer;
import net.minecraft.src.GuiIngame;
import net.minecraft.src.RenderGlobal;
import net.minecraft.src.World;

/** Stable neutral observation exported by the b1.7.3 client adapter. */
public final class B173Observation {
    private final int clientTick;
    private final long worldTime;
    private final long rngSeed;
    private final long clientClock;
    private final int entityCount;
    private final int cloudTick;
    private final int guiTick;
    private final int rendererTick;
    private final long playerX;
    private final long playerY;
    private final long playerZ;
    private final int health;
    private final int selectedSlot;
    private final int[] blockColumn;

    B173Observation(int clientTick, long worldTime, long rngSeed, long clientClock, int entityCount,
            int cloudTick, int guiTick, int rendererTick, long playerX, long playerY,
            long playerZ, int health, int selectedSlot, int... blockColumn) {
        this.clientTick = clientTick;
        this.worldTime = worldTime;
        this.rngSeed = rngSeed;
        this.clientClock = clientClock;
        this.entityCount = entityCount;
        this.cloudTick = cloudTick;
        this.guiTick = guiTick;
        this.rendererTick = rendererTick;
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerZ = playerZ;
        this.health = health;
        this.selectedSlot = selectedSlot;
        this.blockColumn = blockColumn.clone();
    }

    public int clientTick() { return clientTick; }

    public long worldTime() { return worldTime; }

    public long rngSeed() { return rngSeed; }

    public long clientClockMillis() { return clientClock; }

    public int entityCount() { return entityCount; }

    public int cloudTick() { return cloudTick; }

    public int guiTick() { return guiTick; }

    public int rendererTick() { return rendererTick; }

    public long playerXBits() { return playerX; }

    public long playerYBits() { return playerY; }

    public long playerZBits() { return playerZ; }

    public int health() { return health; }

    public int selectedSlot() { return selectedSlot; }

    public int[] blockColumn() { return blockColumn.clone(); }

    public String fingerprint() {
        return clientTick + ":" + worldTime + ":" + rngSeed + ":" + clientClock + ":"
                + entityCount + ":" + cloudTick + ":" + guiTick + ":" + rendererTick + ":"
                + playerX + ":" + playerY + ":" + playerZ + ":" + health + ":"
                + selectedSlot + ":" + blockColumn[0] + ":" + blockColumn[1];
    }

    static B173Observation capture(B173Boundaries.Client client, long rngSeed) {
        World world = client.theWorld;
        if (world == null) throw new IllegalStateException("world is not loaded");
        return new B173Observation(
                B173Reflect.getInt(Minecraft.class, "ticksRan", client), world.getWorldTime(),
                rngSeed, B173Reflect.getLong(Minecraft.class, "systemTime", client),
                world.loadedEntityList.size(),
                B173Reflect.getInt(RenderGlobal.class, "cloudOffsetX", client.renderGlobal),
                B173Reflect.getInt(GuiIngame.class, "updateCounter", client.ingameGUI),
                B173Reflect.getInt(EntityRenderer.class, "rendererUpdateCount", client.entityRenderer),
                Double.doubleToLongBits(client.thePlayer.posX),
                Double.doubleToLongBits(client.thePlayer.posY),
                Double.doubleToLongBits(client.thePlayer.posZ), client.thePlayer.health,
                client.thePlayer.inventory.currentItem, world.getBlockId(8, 64, 8),
                world.getBlockId(8, 65, 8));
    }
}
