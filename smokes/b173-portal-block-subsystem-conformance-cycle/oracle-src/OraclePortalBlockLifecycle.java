/** Official-name portal destruction, persistence, collision, and light probe. */
final class OraclePortalBlockLifecycle {
    final int breakAfter, dropCount, savedCount, savedStateSum, collision, lightCode;
    private OraclePortalBlockLifecycle(int breakAfter, int dropCount, int savedCount,
            int savedStateSum, int collision, int lightCode) {
        this.breakAfter = breakAfter;
        this.dropCount = dropCount;
        this.savedCount = savedCount;
        this.savedStateSum = savedStateSum;
        this.collision = collision;
        this.lightCode = lightCode;
    }
    static OraclePortalBlockLifecycle execute(dj world) {
        OraclePortalBlockFrame.buildX(world, 36, 90, 36);
        iq tag = new iq();
        mg.a(world.c(2, 2), world, tag);
        hi loaded = mg.a(world, tag);
        int saved = 0, sum = 0;
        for (int x = 5; x <= 6; x++)
            for (int y = 91; y <= 93; y++) {
                int state = loaded.a(x, y, 4) * 100 + loaded.b(x, y, 4);
                if (state == 9000) saved++;
                sum += state;
            }
        OraclePortalBlockFrame.buildX(world, 44, 90, 36);
        int before = world.b.size();
        na.bf.a(world, 45, 91, 36, 0, 1.0F);
        world.e(45, 91, 36, 0);
        int collision = na.bf.e(world, 37, 91, 36) == null ? 0 : 1;
        int light = na.q[90] * 100 + na.s[90];
        OraclePortalBlockLifecycle result = new OraclePortalBlockLifecycle(
                OraclePortalBlockFrame.state(world, 45, 91, 36), world.b.size() - before,
                saved, sum, collision, light);
        result.validate();
        return result;
    }
    String lifecycle() { return "break=90:0->0:0,drop=none"; }
    String persistence() { return "chunk-nbt=6x90:0"; }
    String physics() { return "collision=none,light=0:11"; }
    private void validate() {
        OraclePortalBlockFrame.require(breakAfter == 0 && dropCount == 0,
                "portal lifecycle or drop drifted");
        OraclePortalBlockFrame.require(savedCount == 6 && savedStateSum == 54000,
                "portal chunk round trip drifted");
        OraclePortalBlockFrame.require(collision == 0 && lightCode == 11,
                "portal collision or light drifted: " + collision + "/" + lightCode);
    }
}
