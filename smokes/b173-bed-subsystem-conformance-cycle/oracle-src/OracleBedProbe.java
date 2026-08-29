import java.util.Random;

/** Official-name counterpart of the native two-cell bed probe. */
final class OracleBedProbe {
    final int footMask, headMask, strengthClass;
    final int footBefore, footAfter, footDropId, footDropCount;
    final int headBefore, headAfter, headDropDelta;
    final int collision, height, opaque, cube, lightCode, tickMask;
    final int tickFootBefore, tickFootAfter, tickHeadBefore, tickHeadAfter;
    final int pairFoot, pairHead, orphanFoot, orphanFootDropId, orphanFootDropCount;
    final int orphanHead, orphanHeadDropDelta;
    private OracleBedProbe(int footMask, int headMask, int strengthClass,
            int footBefore, int footAfter, int footDropId, int footDropCount,
            int headBefore, int headAfter, int headDropDelta, int collision, int height,
            int opaque, int cube, int lightCode, int tickMask, int tickFootBefore,
            int tickFootAfter, int tickHeadBefore, int tickHeadAfter, int pairFoot,
            int pairHead, int orphanFoot, int orphanFootDropId, int orphanFootDropCount,
            int orphanHead, int orphanHeadDropDelta) {
        this.footMask = footMask;
        this.headMask = headMask;
        this.strengthClass = strengthClass;
        this.footBefore = footBefore;
        this.footAfter = footAfter;
        this.footDropId = footDropId;
        this.footDropCount = footDropCount;
        this.headBefore = headBefore;
        this.headAfter = headAfter;
        this.headDropDelta = headDropDelta;
        this.collision = collision;
        this.height = height;
        this.opaque = opaque;
        this.cube = cube;
        this.lightCode = lightCode;
        this.tickMask = tickMask;
        this.tickFootBefore = tickFootBefore;
        this.tickFootAfter = tickFootAfter;
        this.tickHeadBefore = tickHeadBefore;
        this.tickHeadAfter = tickHeadAfter;
        this.pairFoot = pairFoot;
        this.pairHead = pairHead;
        this.orphanFoot = orphanFoot;
        this.orphanFootDropId = orphanFootDropId;
        this.orphanFootDropCount = orphanFootDropCount;
        this.orphanHead = orphanHead;
        this.orphanHeadDropDelta = orphanHeadDropDelta;
    }
    static OracleBedProbe execute(dj world) {
        int footMask = 0, headMask = 0;
        for (int direction = 0; direction < 4; direction++) {
            int x = 44 + direction * 4, z = 20;
            int headX = x + nj.a[direction][0];
            int headZ = z + nj.a[direction][1];
            require(world.b(x, 80, z, 26, direction)
                    && world.b(headX, 80, headZ, 26, direction + 8),
                    "bed state-domain pair failed");
            footMask |= 1 << world.c(x, 80, z);
            headMask |= 1 << world.c(headX, 80, headZ);
            nj.a(world, headX, 80, headZ, true);
            headMask |= 1 << world.c(headX, 80, headZ);
            nj.a(world, headX, 80, headZ, false);
        }

        em player = new em(world) { };
        require(world.b(24, 80, 20, 26, 0), "bed foot break fixture failed");
        float strength = na.T.a(player);
        int footBefore = state(world, 24, 80, 20);
        int footEntities = world.b.size();
        require(world.e(24, 80, 20, 0), "bed foot removal failed");
        na.T.a(world, player, 24, 80, 20, 0);
        ez footDrop = lastDrop(world, footEntities);
        int footAfter = state(world, 24, 80, 20);

        require(world.b(28, 80, 20, 26, 8), "bed head break fixture failed");
        int headEntities = world.b.size();
        int headBefore = state(world, 28, 80, 20);
        require(world.e(28, 80, 20, 0), "bed head removal failed");
        na.T.a(world, player, 28, 80, 20, 8);
        int headAfter = state(world, 28, 80, 20);
        int headDropDelta = world.b.size() - headEntities;

        require(world.b(20, 80, 20, 26, 0) && world.b(20, 80, 21, 26, 8),
                "bed physical pair failed");
        cz box = na.T.e(world, 20, 80, 20);
        int collision = box != null && box.a == 20D && box.b == 80D && box.c == 20D
                && box.d == 21D && box.e == 80.5625D && box.f == 21D ? 1 : 0;
        int tickFootBefore = state(world, 20, 80, 20);
        int tickHeadBefore = state(world, 20, 80, 21);
        na.T.a(world, 20, 80, 20, new Random(17320110726L));
        na.T.a(world, 20, 80, 21, new Random(17320110726L));

        require(world.b(32, 80, 20, 26, 0) && world.b(32, 80, 21, 26, 8),
                "bed neighbor pair failed");
        na.T.b(world, 32, 80, 20, 1);
        na.T.b(world, 32, 80, 21, 1);
        int pairFoot = state(world, 32, 80, 20), pairHead = state(world, 32, 80, 21);

        require(world.b(36, 80, 20, 26, 0), "orphan bed foot failed");
        int orphanFootEntities = world.b.size();
        na.T.b(world, 36, 80, 20, 1);
        ez orphanDrop = lastDrop(world, orphanFootEntities);
        require(world.b(40, 80, 20, 26, 8), "orphan bed head failed");
        int orphanHeadEntities = world.b.size();
        na.T.b(world, 40, 80, 20, 1);

        OracleBedProbe result = new OracleBedProbe(footMask, headMask,
                strength > 0F && !Float.isInfinite(strength) ? 1 : 0,
                footBefore, footAfter, footDrop == null ? 0 : footDrop.a.c,
                footDrop == null ? 0 : footDrop.a.a, headBefore, headAfter, headDropDelta,
                collision, (int) Math.round(na.T.bw * 10000D),
                na.T.a() ? 1 : 0, na.T.b() ? 1 : 0,
                na.q[26] * 100 + na.s[26], na.n[26] ? 1 : 0,
                tickFootBefore, state(world, 20, 80, 20), tickHeadBefore,
                state(world, 20, 80, 21), pairFoot, pairHead,
                state(world, 36, 80, 20), orphanDrop == null ? 0 : orphanDrop.a.c,
                orphanDrop == null ? 0 : orphanDrop.a.a, state(world, 40, 80, 20),
                world.b.size() - orphanHeadEntities);
        result.validate();
        return result;
    }
    String domains() {
        return "26=foot:0..3,head:8..15,occupied-head:12..15";
    }
    String lifecycle() {
        return "break=foot+head->air,drops=foot:355x1+head:none,strength=finite";
    }
    String physics() {
        return "collision=1x9/16x1,opaque=F,cube=F,light=0:0";
    }
    String timing() {
        return "scheduled=F,callback-stable=26:0+26:8";
    }
    String neighbors() {
        return "paired=stable,orphan-foot=air+355x1,orphan-head=air+none";
    }
    private void validate() {
        require(footMask == 15 && headMask == 65280, "bed metadata domain drifted");
        require(strengthClass == 1 && footBefore == 2600 && footAfter == 0
                && footDropId == 355 && footDropCount == 1 && headBefore == 2608
                && headAfter == 0 && headDropDelta == 0, "bed break or drop matrix drifted");
        require(collision == 1 && height == 5625 && opaque == 0 && cube == 0
                && lightCode == 0, "bed physical envelope drifted");
        require(tickMask == 0 && tickFootBefore == 2600 && tickFootAfter == 2600
                && tickHeadBefore == 2608 && tickHeadAfter == 2608,
                "bed tick policy drifted");
        require(pairFoot == 2600 && pairHead == 2608 && orphanFoot == 0
                && orphanFootDropId == 355 && orphanFootDropCount == 1
                && orphanHead == 0 && orphanHeadDropDelta == 0,
                "bed neighbor response drifted");
    }
    private static ez lastDrop(dj world, int first) {
        for (int index = world.b.size() - 1; index >= first; index--) {
            Object entity = world.b.get(index);
            if (entity instanceof ez)
                return (ez) entity;
        }
        return null;
    }
    private static int state(dj world, int x, int y, int z) {
        return world.a(x, y, z) * 100 + world.c(x, y, z);
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
}
