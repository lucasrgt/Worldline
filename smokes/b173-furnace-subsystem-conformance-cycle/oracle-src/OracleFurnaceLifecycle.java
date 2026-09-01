/** Official-name active lifecycle, chunk progress, collision, and light. */
final class OracleFurnaceLifecycle {
    final int breakAfter, dropCount, dropCode, savedState, savedProgress;
    final long savedSlots;
    final int idleCollision, activeCollision, lightCode;
    private OracleFurnaceLifecycle(int breakAfter, int dropCount, int dropCode, int savedState,
            int savedProgress, long savedSlots, int idleCollision, int activeCollision,
            int lightCode) {
        this.breakAfter = breakAfter;
        this.dropCount = dropCount;
        this.dropCode = dropCode;
        this.savedState = savedState;
        this.savedProgress = savedProgress;
        this.savedSlots = savedSlots;
        this.idleCollision = idleCollision;
        this.activeCollision = activeCollision;
        this.lightCode = lightCode;
    }
    static OracleFurnaceLifecycle execute(dj world) {
        int savedX = 36;
        int y = 92;
        int savedZ = 36;
        place(world, savedX, y, savedZ, 62, 5);
        ln saved = tile(world, savedX, y, savedZ);
        populate(saved);
        saved.a = 777;
        saved.b = 1600;
        saved.c = 88;
        iq tag = new iq();
        mg.a(world.c(2, 2), world, tag);
        hi loaded = mg.a(world, tag);
        ln restored = (ln) loaded.d(4, y, 4);
        int savedState = loaded.a(4, y, 4) * 100 + loaded.b(4, y, 4);
        int savedProgress = restored.a * 1000 + restored.c;
        long savedSlots = stack(restored.d_(0)) * 100000000L
                + stack(restored.d_(1)) * 10000L + stack(restored.d_(2));

        int x = 44;
        int z = 36;
        place(world, x, y, z, 62, 4);
        ln broken = tile(world, x, y, z);
        populate(broken);
        int before = world.b.size();
        na.aD.a(world, x, y, z, 4, 1.0F);
        world.e(x, y, z, 0);
        int[] drops = drops(world, before);
        int breakAfter = state(world, x, y, z);
        int idleCollision = na.aC.e(world, x, y, z) == null ? 0 : 1;
        int activeCollision = na.aD.e(world, x, y, z) == null ? 0 : 1;
        int light = na.q[61] * 100000 + na.s[61] * 1000 + na.q[62] * 100 + na.s[62];
        OracleFurnaceLifecycle result = new OracleFurnaceLifecycle(breakAfter, drops[0], drops[1],
                savedState, savedProgress, savedSlots, idleCollision, activeCollision, light);
        result.validate();
        return result;
    }
    String lifecycle() {
        return "active=62:4->0:0,drops=61+12+263+20,saved=62:5+burn777+cook88";
    }
    String physics() {
        return "collision=61:full+62:full,light=61:255:0+62:255:13";
    }
    private void validate() {
        OracleFurnaceDomain.require(breakAfter == 0 && dropCount == 4 && dropCode == 35604,
                "active lifecycle drifted: " + dropCount + "/" + dropCode);
        OracleFurnaceDomain.require(savedState == 6205 && savedProgress == 777088,
                "chunk progress drifted: " + savedState + "/" + savedProgress);
        OracleFurnaceDomain.require(savedSlots == 120363012001L,
                "chunk inventory drifted: " + savedSlots);
        OracleFurnaceDomain.require(idleCollision == 1 && activeCollision == 1,
                "collision drifted");
        OracleFurnaceDomain.require(lightCode == 25525513, "light drifted: " + lightCode);
        OracleFurnaceDomain.require(na.aD.a(4, new java.util.Random(17320110707L)) == 61,
                "active item route drifted");
    }
    private static void populate(ln tile) {
        tile.a(0, new fy(12, 1, 0));
        tile.a(1, new fy(263, 1, 0));
        tile.a(2, new fy(20, 1, 0));
    }
    private static ln tile(dj world, int x, int y, int z) {
        return (ln) world.b(x, y, z);
    }
    private static void place(dj world, int x, int y, int z, int id, int metadata) {
        OracleFurnaceDomain.require(world.e(x, y, z, id), "furnace block placement failed");
        world.c(x, y, z, metadata);
    }
    private static int[] drops(dj world, int index) {
        int count = 0;
        int sum = 0;
        for (int current = index; current < world.b.size(); current++) {
            lq entity = (lq) world.b.get(current);
            if (entity instanceof ez) {
                fy stack = ((ez) entity).a;
                count += stack.a;
                sum += stack.c;
            }
        }
        return new int[] {count, sum * 100 + count};
    }
    private static int stack(fy value) {
        return value.c * 100 + value.a;
    }
    private static int state(dj world, int x, int y, int z) {
        return world.a(x, y, z) * 100 + world.c(x, y, z);
    }
}
