/** Official-name break/drop delegation and chunk-NBT round-trip probe. */
final class OraclePistonLifecycle {
    final int dropCount, headAfter, baseAfter, headDrop, movingAfter, movingDrop;
    final int savedHead, savedMoving, storedId, storedMetadata, storedDirection;
    final boolean extending;

    private OraclePistonLifecycle(int dropCount, int headAfter, int baseAfter, int headDrop,
            int movingAfter, int movingDrop, int savedHead, int savedMoving, int storedId,
            int storedMetadata, int storedDirection, boolean extending) {
        this.dropCount = dropCount;
        this.headAfter = headAfter;
        this.baseAfter = baseAfter;
        this.headDrop = headDrop;
        this.movingAfter = movingAfter;
        this.movingDrop = movingDrop;
        this.savedHead = savedHead;
        this.savedMoving = savedMoving;
        this.storedId = storedId;
        this.storedMetadata = storedMetadata;
        this.storedDirection = storedDirection;
        this.extending = extending;
    }

    static OraclePistonLifecycle execute(dj world) {
        int before = world.b.size();
        int baseX = 0, y = 85, z = 20, headX = baseX + 1;
        boolean priorRemote = world.B;
        world.B = true;
        try {
            OraclePistonDomain.require(world.a(baseX, y, z, 33, 13),
                    "extended base fixture placement failed");
            OraclePistonDomain.require(world.a(headX, y, z, 34, 5),
                    "piston head fixture placement failed");
        } finally {
            world.B = priorRemote;
        }
        world.e(headX, y, z, 0);
        int headAfter = OraclePistonDomain.state(world, headX, y, z);
        int baseAfter = OraclePistonDomain.state(world, baseX, y, z);
        ez headItem = itemAfter(world, before, "head after=" + headAfter
                + ",base=" + baseAfter + ",remote=" + world.B);
        int headDrop = stack(headItem);

        int movingX = 8, movingZ = 20, beforeMoving = world.b.size();
        world.b(movingX, y, movingZ, 36, 0);
        world.a(movingX, y, movingZ, mz.a(1, 0, 5, true, false));
        na.ad.a(world, movingX, y, movingZ, 0, 1.0F);
        ez movingItem = itemAfter(world, beforeMoving, "moving state="
                + OraclePistonDomain.state(world, movingX, y, movingZ)
                + ",tile=" + (world.b(movingX, y, movingZ) != null)
                + ",remote=" + world.B);
        world.e(movingX, y, movingZ, 0);
        int movingAfter = OraclePistonDomain.state(world, movingX, y, movingZ);
        int movingDrop = stack(movingItem);

        int persistentY = 90, persistentZ = 34;
        world.b(4, persistentY, persistentZ, 34, 5);
        world.b(8, persistentY, persistentZ, 36, 5);
        world.a(8, persistentY, persistentZ, mz.a(34, 5, 5, true, false));
        iq tag = new iq();
        mg.a(world.c(0, 2), world, tag);
        hi loaded = mg.a(world, tag);
        int savedHead = loaded.a(4, persistentY, 2) * 100
                + loaded.b(4, persistentY, 2);
        int savedMoving = loaded.a(8, persistentY, 2) * 100
                + loaded.b(8, persistentY, 2);
        mu tile = (mu) loaded.d(8, persistentY, 2);
        OraclePistonDomain.require(tile != null, "moving piston NBT was not restored");
        OraclePistonLifecycle result = new OraclePistonLifecycle(world.b.size() - before,
                headAfter, baseAfter, headDrop, movingAfter, movingDrop, savedHead,
                savedMoving, tile.a(), tile.e(), tile.d(), tile.c());
        result.validate();
        return result;
    }

    String breakAndDrops() {
        return "head=34:5->0:0+base=33:13->0:0+drop=33x1:0,"
                + "moving=36:0->0:0+drop=4x1:0";
    }
    String persistence() {
        return "head=34:5,moving=36:5+te=34:5:5:true";
    }

    private void validate() {
        OraclePistonDomain.require(headAfter == 0 && baseAfter == 0 && headDrop == 330100,
                "piston head delegated break/drop drifted");
        OraclePistonDomain.require(movingAfter == 0 && movingDrop == 40100,
                "moving piston delegated drop drifted: after=" + movingAfter
                        + ",drop=" + movingDrop);
        OraclePistonDomain.require(savedHead == 3405 && savedMoving == 3605
                && storedId == 34 && storedMetadata == 5 && storedDirection == 5 && extending,
                "piston chunk-NBT round trip drifted");
    }
    private static ez itemAfter(dj world, int index, String context) {
        for (int current = world.b.size() - 1; current >= index; current--) {
            lq entity = (lq) world.b.get(current);
            if (entity instanceof ez)
                return (ez) entity;
        }
        throw new IllegalStateException("expected piston drop was absent: " + context
                + ",before=" + index + ",after=" + world.b.size());
    }
    private static int stack(ez item) {
        return item.a.c * 10000 + item.a.a * 100 + item.a.h();
    }
}
