package worldline.b173;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.World;

/** Runs reusable movement probes through the mapped Beta 1.7.3 player physics root. */
public final class B173PhysicsProbe {
    private static final int X = 8;
    private static final int Y = 65;
    private static final int Z = 8;

    private B173PhysicsProbe() {}

    public static SlowBlocks slowBlocks(B173Runtime runtime, int ticks) {
        require(runtime != null && ticks > 0 && ticks <= 32, "invalid slow-block probe");
        B173Boundaries.Client client = runtime.backend().client();
        World world = client.theWorld; EntityPlayerSP player = client.thePlayer;
        corridor(world, 1, 0); int air = horizontal(player, ticks);
        corridor(world, 1, 30); int web = horizontal(player, ticks);
        corridor(world, 88, 0); int soul = horizontal(player, ticks);
        require(web < air && soul < air && web >= 0 && soul >= 0,
                "official slow-block ordering absent " + air + "/" + web + "/" + soul);
        return new SlowBlocks(ticks, air, web, soul);
    }

    public static LadderClimb ladder(B173Runtime runtime, int ticks) {
        require(runtime != null && ticks > 0 && ticks <= 32, "invalid ladder probe");
        B173Boundaries.Client client = runtime.backend().client();
        World world = client.theWorld; EntityPlayerSP player = client.thePlayer;
        ladderFixture(world, false); int air = vertical(player, ticks);
        ladderFixture(world, true); int climb = vertical(player, ticks);
        require(climb > air, "official ladder climb ordering absent " + air + "/" + climb);
        return new LadderClimb(ticks, air, climb);
    }

    private static int horizontal(EntityPlayerSP player, int ticks) {
        reset(player, X + 0.5D, Y, Z + 0.5D, 0F);
        double startX = player.posX, startZ = player.posZ;
        for (int index = 0; index < ticks; index++) player.moveEntityWithHeading(0F, 1F);
        return milli(Math.hypot(player.posX - startX, player.posZ - startZ));
    }

    private static int vertical(EntityPlayerSP player, int ticks) {
        reset(player, X + 1.15D, Y, Z + 0.5D, 90F);
        double start = player.posY;
        for (int index = 0; index < ticks; index++) player.moveEntityWithHeading(0F, 1F);
        return milli(player.posY - start);
    }

    private static void corridor(World world, int floor, int body) {
        for (int z = Z; z <= Z + 12; z++) {
            set(world, X, Y - 1, z, floor, 0);
            set(world, X, Y, z, body, 0);
            set(world, X, Y + 1, z, 0, 0);
        }
    }

    private static void ladderFixture(World world, boolean ladder) {
        for (int y = Y; y <= Y + 3; y++) {
            set(world, X, y, Z, 1, 0);
            set(world, X + 1, y, Z, ladder && y <= Y + 1 ? 65 : 0, 5);
        }
        set(world, X + 1, Y - 1, Z, 1, 0);
    }

    private static void set(World world, int x, int y, int z, int id, int damage) {
        boolean changed = damage == 0 ? world.setBlockWithNotify(x, y, z, id)
                : world.setBlockAndMetadataWithNotify(x, y, z, id, damage);
        require(changed || damage == 0 && world.getBlockId(x, y, z) == id,
                "physics fixture write failed at " + x + "," + y + "," + z);
    }

    private static void reset(EntityPlayerSP player, double x, double y, double z, float yaw) {
        player.setLocationAndAngles(x, y, z, yaw, 0F);
        player.motionX = 0D; player.motionY = 0D; player.motionZ = 0D;
        player.onGround = true; player.isCollidedHorizontally = false;
        player.isInWeb = false;
        B173Reflect.setFloat(Entity.class, "fallDistance", player, 0F);
    }

    private static int milli(double value) { return (int) Math.round(value * 1000D); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static final class SlowBlocks {
        public final int ticks, airMilli, webMilli, soulMilli;
        SlowBlocks(int ticks, int air, int web, int soul) {
            this.ticks = ticks; airMilli = air; webMilli = web; soulMilli = soul;
        }
        public String trace() { return "ticks=" + ticks + ",air=" + airMilli
                + ",web=" + webMilli + ",soul=" + soulMilli; }
    }

    public static final class LadderClimb {
        public final int ticks, airMilli, climbMilli;
        LadderClimb(int ticks, int air, int climb) {
            this.ticks = ticks; airMilli = air; climbMilli = climb;
        }
        public String trace() { return "ticks=" + ticks + ",air=" + airMilli
                + ",climb=" + climbMilli; }
    }
}
