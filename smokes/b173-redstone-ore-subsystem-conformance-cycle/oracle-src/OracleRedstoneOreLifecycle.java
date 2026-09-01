/** Official-name glowing lifecycle, persistence, collision, and light probe. */
final class OracleRedstoneOreLifecycle {
    final int breakAfter, dropCount, dropItem, savedUnlit, savedGlowing;
    final int unlitCollision, glowingCollision, lightCode;
    private OracleRedstoneOreLifecycle(int breakAfter, int dropCount, int dropItem,
            int savedUnlit, int savedGlowing, int unlitCollision, int glowingCollision,
            int lightCode) {
        this.breakAfter = breakAfter;
        this.dropCount = dropCount;
        this.dropItem = dropItem;
        this.savedUnlit = savedUnlit;
        this.savedGlowing = savedGlowing;
        this.unlitCollision = unlitCollision;
        this.glowingCollision = glowingCollision;
        this.lightCode = lightCode;
    }
    static OracleRedstoneOreLifecycle execute(dj world) {
        int y = 92, z = 36;
        place(world, 36, y, z, 73);
        place(world, 40, y, z, 74);
        iq tag = new iq();
        mg.a(world.c(2, 2), world, tag);
        hi loaded = mg.a(world, tag);
        int savedUnlit = loaded.a(4, y, 4) * 100 + loaded.b(4, y, 4);
        int savedGlowing = loaded.a(8, y, 4) * 100 + loaded.b(8, y, 4);

        int x = 44;
        place(world, x, y, z, 74);
        int before = world.b.size();
        world.r.setSeed(17320110707L);
        na.aP.a(world, x, y, z, 0, 1.0F);
        world.e(x, y, z, 0);
        int[] drops = drops(world, before);
        int unlitCollision = na.aO.e(world, x, y, z) == null ? 0 : 1;
        int glowingCollision = na.aP.e(world, x, y, z) == null ? 0 : 1;
        int light = na.q[73] * 100000 + na.s[73] * 1000 + na.q[74] * 100 + na.s[74];
        OracleRedstoneOreLifecycle result = new OracleRedstoneOreLifecycle(
                OracleRedstoneOreDomain.state(world, x, y, z), drops[0], drops[1],
                savedUnlit, savedGlowing, unlitCollision, glowingCollision, light);
        result.validate();
        return result;
    }
    String lifecycle() {
        return "break=74:0->0:0,drop=331x4..5:0,saved=73:0+74:0";
    }
    String physics() {
        return "collision=73:full+74:full,light=73:255:0+74:255:9";
    }
    private void validate() {
        OracleRedstoneOreDomain.require(breakAfter == 0,
                "glowing redstone ore break drifted");
        OracleRedstoneOreDomain.require(dropCount >= 4 && dropCount <= 5 && dropItem == 331,
                "glowing redstone ore drop drifted: " + dropCount + "/" + dropItem);
        OracleRedstoneOreDomain.require(savedUnlit == 7300 && savedGlowing == 7400,
                "redstone ore chunk round trip drifted");
        OracleRedstoneOreDomain.require(unlitCollision == 1 && glowingCollision == 1,
                "redstone ore collision drifted");
        OracleRedstoneOreDomain.require(lightCode == 25525509,
                "redstone ore light drifted: " + lightCode);
        OracleRedstoneOreDomain.require(na.aP.a(0,
                new java.util.Random(17320110707L)) == 331,
                "glowing redstone ore item route drifted");
    }
    private static void place(dj world, int x, int y, int z, int id) {
        OracleRedstoneOreDomain.require(world.b(x, y, z, id, 0),
                "redstone ore lifecycle placement failed: " + id);
    }
    private static int[] drops(dj world, int index) {
        int count = 0, item = 0;
        for (int current = index; current < world.b.size(); current++) {
            lq entity = (lq) world.b.get(current);
            if (entity instanceof ez) {
                fy stack = ((ez) entity).a;
                count += stack.a;
                item = item == 0 ? stack.c : item;
                OracleRedstoneOreDomain.require(item == stack.c && stack.h() == 0,
                        "mixed glowing redstone ore drops");
            }
        }
        return new int[] {count, item};
    }
}
