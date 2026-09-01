import java.util.Random;

/** Official-name counterpart of the native two-cell iron-door probe. */
final class OracleIronDoorProbe {
    final int lowerMask, upperMask, strengthClass;
    final int lowerBefore, lowerAfter, lowerDropId, lowerDropCount;
    final int upperBefore, upperAfter, upperDropDelta;
    final int closedCollision, openCollision, opaque, cube, lightCode, tickMask;
    final int tickLowerBefore, tickLowerAfter, tickUpperBefore, tickUpperAfter;
    final int pairLower, pairUpper, orphanLower, orphanLowerDropId, orphanLowerDropCount;
    final int orphanUpper, orphanUpperDropDelta, supportLower, supportUpper;
    final int supportDropId, supportDropCount;
    private OracleIronDoorProbe(int lowerMask, int upperMask, int strengthClass,
            int lowerBefore, int lowerAfter, int lowerDropId, int lowerDropCount,
            int upperBefore, int upperAfter, int upperDropDelta, int closedCollision,
            int openCollision, int opaque, int cube, int lightCode, int tickMask,
            int tickLowerBefore, int tickLowerAfter, int tickUpperBefore, int tickUpperAfter,
            int pairLower, int pairUpper, int orphanLower, int orphanLowerDropId,
            int orphanLowerDropCount, int orphanUpper, int orphanUpperDropDelta,
            int supportLower, int supportUpper, int supportDropId, int supportDropCount) {
        this.lowerMask = lowerMask;
        this.upperMask = upperMask;
        this.strengthClass = strengthClass;
        this.lowerBefore = lowerBefore;
        this.lowerAfter = lowerAfter;
        this.lowerDropId = lowerDropId;
        this.lowerDropCount = lowerDropCount;
        this.upperBefore = upperBefore;
        this.upperAfter = upperAfter;
        this.upperDropDelta = upperDropDelta;
        this.closedCollision = closedCollision;
        this.openCollision = openCollision;
        this.opaque = opaque;
        this.cube = cube;
        this.lightCode = lightCode;
        this.tickMask = tickMask;
        this.tickLowerBefore = tickLowerBefore;
        this.tickLowerAfter = tickLowerAfter;
        this.tickUpperBefore = tickUpperBefore;
        this.tickUpperAfter = tickUpperAfter;
        this.pairLower = pairLower;
        this.pairUpper = pairUpper;
        this.orphanLower = orphanLower;
        this.orphanLowerDropId = orphanLowerDropId;
        this.orphanLowerDropCount = orphanLowerDropCount;
        this.orphanUpper = orphanUpper;
        this.orphanUpperDropDelta = orphanUpperDropDelta;
        this.supportLower = supportLower;
        this.supportUpper = supportUpper;
        this.supportDropId = supportDropId;
        this.supportDropCount = supportDropCount;
    }
    static OracleIronDoorProbe execute(dj world) {
        int lowerMask = 0, upperMask = 0;
        for (int direction = 0; direction < 4; direction++) {
            int x = 48 + direction * 3;
            placePair(world, x, direction);
            lowerMask |= 1 << world.c(x, 80, 20);
            upperMask |= 1 << world.c(x, 81, 20);
            ((hc) na.aM).a(world, x, 80, 20, true);
            lowerMask |= 1 << world.c(x, 80, 20);
            upperMask |= 1 << world.c(x, 81, 20);
        }
        em player = new em(world) { };
        require(world.b(24, 80, 20, 71, 0), "iron-door lower break fixture failed");
        float strength = na.aM.a(player);
        int lowerBefore = state(world, 24, 80, 20), lowerEntities = world.b.size();
        require(world.e(24, 80, 20, 0), "iron-door lower removal failed");
        na.aM.a(world, player, 24, 80, 20, 0);
        ez lowerDrop = lastDrop(world, lowerEntities);
        int lowerAfter = state(world, 24, 80, 20);
        require(world.b(28, 80, 20, 71, 8), "iron-door upper break fixture failed");
        int upperEntities = world.b.size(), upperBefore = state(world, 28, 80, 20);
        require(world.e(28, 80, 20, 0), "iron-door upper removal failed");
        na.aM.a(world, player, 28, 80, 20, 8);
        int upperAfter = state(world, 28, 80, 20);
        int upperDropDelta = world.b.size() - upperEntities;

        placePair(world, 20, 0);
        cz closed = na.aM.e(world, 20, 80, 20);
        int closedCollision = exact(closed, 20D, 80D, 20D, 20.1875D, 81D, 21D);
        ((hc) na.aM).a(world, 20, 80, 20, true);
        cz open = na.aM.e(world, 20, 80, 20);
        int openCollision = exact(open, 20D, 80D, 20D, 21D, 81D, 20.1875D);
        ((hc) na.aM).a(world, 20, 80, 20, false);
        int tickLowerBefore = state(world, 20, 80, 20), tickUpperBefore = state(world, 20, 81, 20);
        na.aM.a(world, 20, 80, 20, new Random(17320110771L));
        na.aM.a(world, 20, 81, 20, new Random(17320110771L));

        placePair(world, 32, 0);
        na.aM.b(world, 32, 80, 20, 1);
        int pairLower = state(world, 32, 80, 20), pairUpper = state(world, 32, 81, 20);
        require(world.e(36, 79, 20, 1) && world.b(36, 80, 20, 71, 0),
                "orphan lower fixture failed");
        int orphanLowerEntities = world.b.size();
        na.aM.b(world, 36, 80, 20, 1);
        ez orphanLowerDrop = lastDrop(world, orphanLowerEntities);
        require(world.b(40, 81, 20, 71, 8), "orphan upper fixture failed");
        int orphanUpperEntities = world.b.size();
        na.aM.b(world, 40, 81, 20, 1);
        int orphanUpperDelta = world.b.size() - orphanUpperEntities;
        placePair(world, 44, 0);
        int supportEntities = world.b.size();
        require(world.b(44, 79, 20, 0), "support removal failed");
        na.aM.b(world, 44, 80, 20, 1);
        ez supportDrop = lastDrop(world, supportEntities);

        OracleIronDoorProbe result = new OracleIronDoorProbe(lowerMask, upperMask,
                strength > 0F && !Float.isInfinite(strength) ? 1 : 0, lowerBefore, lowerAfter,
                id(lowerDrop), count(lowerDrop), upperBefore, upperAfter, upperDropDelta,
                closedCollision, openCollision, na.aM.a() ? 1 : 0, na.aM.b() ? 1 : 0,
                na.q[71] * 100 + na.s[71], na.n[71] ? 1 : 0, tickLowerBefore,
                state(world, 20, 80, 20), tickUpperBefore, state(world, 20, 81, 20),
                pairLower, pairUpper, state(world, 36, 80, 20), id(orphanLowerDrop),
                count(orphanLowerDrop), state(world, 40, 81, 20), orphanUpperDelta,
                state(world, 44, 80, 20), state(world, 44, 81, 20), id(supportDrop),
                count(supportDrop));
        result.validate();
        return result;
    }
    String domains() { return "71=lower:0..7,upper:8..15,open-bit=4"; }
    String lifecycle() { return "break=lower+upper->air,drops=lower:330x1+upper:none,strength=finite"; }
    String physics() { return "collision=closed-x-3/16+open-z-3/16,opaque=F,cube=F,light=0:0"; }
    String timing() { return "scheduled=F,callback-stable=71:0+71:8"; }
    String neighbors() {
        return "paired=stable,orphan-lower=air+330x1,orphan-upper=air+none,"
                + "support-loss=both-air+330x1";
    }
    private void validate() {
        require(lowerMask == 255 && upperMask == 65280, "iron-door metadata domain drifted");
        require(strengthClass == 1 && lowerBefore == 7100 && lowerAfter == 0
                && lowerDropId == 330 && lowerDropCount == 1 && upperBefore == 7108
                && upperAfter == 0 && upperDropDelta == 0, "iron-door lifecycle drifted");
        require(closedCollision == 1 && openCollision == 1 && opaque == 0 && cube == 0
                && lightCode == 0, "iron-door physical envelope drifted");
        require(tickMask == 0 && tickLowerBefore == 7100 && tickLowerAfter == 7100
                && tickUpperBefore == 7108 && tickUpperAfter == 7108, "iron-door tick policy drifted");
        require(pairLower == 7100 && pairUpper == 7108 && orphanLower == 0
                && orphanLowerDropId == 330 && orphanLowerDropCount == 1
                && orphanUpper == 0 && orphanUpperDropDelta == 0 && supportLower == 0
                && supportUpper == 0 && supportDropId == 330 && supportDropCount == 1,
                "iron-door neighbor response drifted");
    }
    private static void placePair(dj world, int x, int metadata) {
        require(world.e(x, 79, 20, 1) && world.b(x, 80, 20, 71, metadata)
                && world.b(x, 81, 20, 71, metadata + 8), "iron-door pair failed");
    }
    private static int exact(cz box, double a, double b, double c,
            double d, double e, double f) {
        return box != null && box.a == a && box.b == b && box.c == c
                && box.d == d && box.e == e && box.f == f ? 1 : 0;
    }
    private static ez lastDrop(dj world, int first) {
        for (int index = world.b.size() - 1; index >= first; index--)
            if (world.b.get(index) instanceof ez) return (ez) world.b.get(index);
        return null;
    }
    private static int id(ez drop) { return drop == null ? 0 : drop.a.c; }
    private static int count(ez drop) { return drop == null ? 0 : drop.a.a; }
    private static int state(dj world, int x, int y, int z) {
        return world.a(x, y, z) * 100 + world.c(x, y, z);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
