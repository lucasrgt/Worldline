import java.util.Random;

/** Official-name idle lifecycle, chunk persistence, collision, and light probe. */
final class OracleRedstoneTorchLifecycle {
    final int dropCount, breakAfter, dropStack, savedOff, savedOn;
    final int offCollision, onCollision, lightCode;
    private OracleRedstoneTorchLifecycle(int dropCount, int breakAfter, int dropStack,
            int savedOff, int savedOn, int offCollision, int onCollision, int lightCode) {
        this.dropCount = dropCount;
        this.breakAfter = breakAfter;
        this.dropStack = dropStack;
        this.savedOff = savedOff;
        this.savedOn = savedOn;
        this.offCollision = offCollision;
        this.onCollision = onCollision;
        this.lightCode = lightCode;
    }
    static OracleRedstoneTorchLifecycle execute(dj world) {
        int before = world.b.size(), x = 20, y = 88, z = 32;
        world.b(x, y - 1, z, 1, 0);
        world.b(x, y, z, 75, 5);
        na.aQ.a(world, x, y, z, 5, 1.0F);
        ez item = itemAfter(world, before);
        world.e(x, y, z, 0);
        int after = state(world, x, y, z), drop = stack(item);

        int persistentY = 90, persistentZ = 34;
        world.b(4, persistentY - 1, persistentZ, 1, 0);
        world.b(4, persistentY, persistentZ, 75, 1);
        world.b(8, persistentY - 1, persistentZ, 1, 0);
        world.b(8, persistentY, persistentZ, 76, 5);
        iq tag = new iq();
        mg.a(world.c(0, 2), world, tag);
        hi loaded = mg.a(world, tag);
        int savedOff = loaded.a(4, persistentY, 2) * 100 + loaded.b(4, persistentY, 2);
        int savedOn = loaded.a(8, persistentY, 2) * 100 + loaded.b(8, persistentY, 2);
        int offCollision = na.aQ.e(world, x, y, z) == null ? 0 : 1;
        int onCollision = na.aR.e(world, x, y, z) == null ? 0 : 1;
        int light = na.q[75] * 1000 + na.s[75] * 100 + na.q[76] * 10 + na.s[76];
        OracleRedstoneTorchLifecycle result = new OracleRedstoneTorchLifecycle(
                world.b.size() - before, after, drop, savedOff, savedOn,
                offCollision, onCollision, light);
        result.validate();
        return result;
    }
    String lifecycle() {
        return "off=75:5->0:0+drop=76x1:0,saved=75:1+76:5";
    }
    String physics() {
        return "collision=75:none+76:none,light=75:0:0+76:0:7";
    }
    private void validate() {
        OracleRedstoneTorchTiming.require(breakAfter == 0 && dropStack == 760100,
                "idle torch break/drop drifted");
        OracleRedstoneTorchTiming.require(savedOff == 7501 && savedOn == 7605,
                "torch chunk round trip drifted");
        OracleRedstoneTorchTiming.require(offCollision == 0 && onCollision == 0,
                "torch collision drifted");
        OracleRedstoneTorchTiming.require(lightCode == 7, "torch light drifted: " + lightCode);
        OracleRedstoneTorchTiming.require(na.aQ.a(5, new Random(17320110707L)) == 76,
                "idle torch item route drifted");
    }
    private static int state(dj world, int x, int y, int z) {
        return world.a(x, y, z) * 100 + world.c(x, y, z);
    }
    private static ez itemAfter(dj world, int index) {
        for (int current = world.b.size() - 1; current >= index; current--) {
            lq entity = (lq) world.b.get(current);
            if (entity instanceof ez)
                return (ez) entity;
        }
        throw new IllegalStateException("expected idle torch drop was absent");
    }
    private static int stack(ez item) {
        return item.a.c * 10000 + item.a.a * 100 + item.a.h();
    }
}
