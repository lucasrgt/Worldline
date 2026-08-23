/** Runs the movement probes through the official obfuscated client physics root. */
final class OfficialPhysicsProbe {
    private static final int X = 8;
    private static final int Y = 65;
    private static final int Z = 8;

    private OfficialPhysicsProbe() {}

    static String trace(fd world, dc player) {
        int slowTicks = 8, ladderTicks = 10;
        corridor(world, 1, 0); int air = horizontal(player, slowTicks);
        corridor(world, 1, 30); int web = horizontal(player, slowTicks);
        corridor(world, 88, 0); int soul = horizontal(player, slowTicks);
        require(web < air && soul < air && web >= 0 && soul >= 0,
                "official slow-block ordering absent " + air + "/" + web + "/" + soul);
        ladderFixture(world, false); int ladderAir = vertical(player, ladderTicks);
        ladderFixture(world, true); int climb = vertical(player, ladderTicks);
        require(climb > ladderAir,
                "official ladder climb ordering absent " + ladderAir + "/" + climb);
        return "v1|slow=ticks=" + slowTicks + ",air=" + air + ",web=" + web
                + ",soul=" + soul + "|ladder=ticks=" + ladderTicks + ",air="
                + ladderAir + ",climb=" + climb;
    }

    private static int horizontal(dc player, int ticks) {
        reset(player, X + 0.5D, Y, Z + 0.5D, 0F);
        double startX = player.aM, startZ = player.aO;
        for (int index = 0; index < ticks; index++) player.a_(0F, 1F);
        return milli(Math.hypot(player.aM - startX, player.aO - startZ));
    }

    private static int vertical(dc player, int ticks) {
        reset(player, X + 1.15D, Y, Z + 0.5D, 90F);
        double start = player.aN;
        for (int index = 0; index < ticks; index++) player.a_(0F, 1F);
        return milli(player.aN - start);
    }

    private static void corridor(fd world, int floor, int body) {
        for (int z = Z; z <= Z + 12; z++) {
            set(world, X, Y - 1, z, floor, 0);
            set(world, X, Y, z, body, 0);
            set(world, X, Y + 1, z, 0, 0);
        }
    }

    private static void ladderFixture(fd world, boolean ladder) {
        for (int y = Y; y <= Y + 3; y++) {
            set(world, X, y, Z, 1, 0);
            boolean placed = ladder && y <= Y + 1;
            set(world, X + 1, y, Z, placed ? 65 : 0, placed ? 5 : 0);
        }
        set(world, X + 1, Y - 1, Z, 1, 0);
    }

    private static void set(fd world, int x, int y, int z, int id, int damage) {
        boolean changed = damage == 0 ? world.f(x, y, z, id) : world.b(x, y, z, id, damage);
        require(changed || damage == 0 && world.a(x, y, z) == id,
                "physics fixture write failed at " + x + "," + y + "," + z);
    }

    private static void reset(dc player, double x, double y, double z, float yaw) {
        player.c(x, y, z, yaw, 0F);
        player.aP = 0D; player.aQ = 0D; player.aR = 0D;
        player.aX = true; player.aY = false; player.bc = false; player.bk = 0F;
    }

    private static int milli(double value) { return (int) Math.round(value * 1000D); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
