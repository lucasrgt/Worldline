import java.util.ArrayList;
import java.util.Random;

/** Official-name lifecycle, chunk persistence, collision, and light probe. */
final class OracleRepeaterLifecycle {
    final int dropCount, breakAfter, dropStack, savedOff, savedOn;
    final int offBoxes, onBoxes, offHeight, onHeight, lightCode;

    private OracleRepeaterLifecycle(int dropCount, int breakAfter, int dropStack,
            int savedOff, int savedOn, int offBoxes, int onBoxes, int offHeight,
            int onHeight, int lightCode) {
        this.dropCount = dropCount;
        this.breakAfter = breakAfter;
        this.dropStack = dropStack;
        this.savedOff = savedOff;
        this.savedOn = savedOn;
        this.offBoxes = offBoxes;
        this.onBoxes = onBoxes;
        this.offHeight = offHeight;
        this.onHeight = onHeight;
        this.lightCode = lightCode;
    }

    static OracleRepeaterLifecycle execute(dj world) {
        int before = world.b.size(), x = 20, y = 88, z = 20;
        world.b(x, y - 1, z, 1, 0);
        world.b(x, y, z, 94, 15);
        na.bj.a(world, x, y, z, 15, 1.0F);
        ez item = itemAfter(world, before);
        world.e(x, y, z, 0);
        int after = state(world, x, y, z), drop = stack(item);

        int persistentY = 90, persistentZ = 34;
        world.b(4, persistentY - 1, persistentZ, 1, 0);
        world.b(4, persistentY, persistentZ, 93, 0);
        world.b(8, persistentY - 1, persistentZ, 1, 0);
        world.b(8, persistentY, persistentZ, 94, 15);
        iq tag = new iq();
        mg.a(world.c(0, 2), world, tag);
        hi loaded = mg.a(world, tag);
        int savedOff = loaded.a(4, persistentY, 2) * 100 + loaded.b(4, persistentY, 2);
        int savedOn = loaded.a(8, persistentY, 2) * 100 + loaded.b(8, persistentY, 2);

        int[] offShape = shape(world, na.bi, -16, 96, 20);
        int[] onShape = shape(world, na.bj, -8, 96, 20);
        int light = na.q[93] * 1000 + na.s[93] * 100 + na.q[94] * 10 + na.s[94];
        OracleRepeaterLifecycle result = new OracleRepeaterLifecycle(world.b.size() - before,
                after, drop, savedOff, savedOn, offShape[0], onShape[0], offShape[1],
                onShape[1], light);
        result.validate();
        return result;
    }

    String lifecycle() { return "on=94:15->0:0+drop=356x1:0,saved=93:0+94:15"; }
    String physics() { return "collision=93:1/8+94:1/8,light=93:0:0+94:0:9"; }

    private void validate() {
        OracleRepeaterTiming.require(breakAfter == 0 && dropStack == 3560100,
                "active repeater break/drop drifted");
        OracleRepeaterTiming.require(savedOff == 9300 && savedOn == 9415,
                "repeater chunk round trip drifted");
        OracleRepeaterTiming.require(offBoxes == 1 && onBoxes == 1
                && offHeight == 1 && onHeight == 1, "repeater collision drifted");
        OracleRepeaterTiming.require(lightCode == 9, "repeater light drifted: " + lightCode);
        OracleRepeaterTiming.require(na.bi.a(0, new Random(17320110707L)) == 356
                && na.bj.a(15, new Random(17320110707L)) == 356,
                "repeater item route drifted");
    }

    private static int[] shape(dj world, na block, int x, int y, int z) {
        world.b(x, y - 1, z, 1, 0);
        world.b(x, y, z, block.bn, 15);
        ArrayList<cz> values = new ArrayList<cz>();
        block.a(world, x, y, z, cz.a(x - 1, y - 1, z - 1, x + 2, y + 2, z + 2), values);
        cz box = block.e(world, x, y, z);
        int height = (int) Math.round((box.e - box.b) * 8.0D);
        return new int[] {values.size(), height};
    }
    private static int state(dj world, int x, int y, int z) {
        return world.a(x, y, z) * 100 + world.c(x, y, z);
    }
    private static ez itemAfter(dj world, int index) {
        for (int current = world.b.size() - 1; current >= index; current--) {
            lq entity = (lq) world.b.get(current);
            if (entity instanceof ez) return (ez) entity;
        }
        throw new IllegalStateException("expected active repeater drop was absent");
    }
    private static int stack(ez item) { return item.a.c * 10000 + item.a.a * 100 + item.a.h(); }
}
