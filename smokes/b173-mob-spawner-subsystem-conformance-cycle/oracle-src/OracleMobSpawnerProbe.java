/** Official-name counterpart of the mob-spawner subsystem probe. */
final class OracleMobSpawnerProbe {
    final int registryMask, placementRoute, placedState, stackAfter, placedTile;
    final int strengthClass, breakBefore, breakAfter, dropDelta;
    final int savedState, savedEntity, savedDelay, tickMask, farDelay, nearDelay;
    final int neighborState, neighborEntity, neighborDelay;
    private OracleMobSpawnerProbe(int registryMask, int placementRoute, int placedState,
            int stackAfter, int placedTile, int strengthClass, int breakBefore, int breakAfter,
            int dropDelta, int savedState, int savedEntity, int savedDelay, int tickMask,
            int farDelay, int nearDelay, int neighborState, int neighborEntity, int neighborDelay) {
        this.registryMask = registryMask;
        this.placementRoute = placementRoute;
        this.placedState = placedState;
        this.stackAfter = stackAfter;
        this.placedTile = placedTile;
        this.strengthClass = strengthClass;
        this.breakBefore = breakBefore;
        this.breakAfter = breakAfter;
        this.dropDelta = dropDelta;
        this.savedState = savedState;
        this.savedEntity = savedEntity;
        this.savedDelay = savedDelay;
        this.tickMask = tickMask;
        this.farDelay = farDelay;
        this.nearDelay = nearDelay;
        this.neighborState = neighborState;
        this.neighborEntity = neighborEntity;
        this.neighborDelay = neighborDelay;
    }
    static OracleMobSpawnerProbe execute(dj world) {
        em player = new em(world) { };
        int registry = na.m[52] == na.at && na.at instanceof bz ? 1 : 0;
        registry |= ej.c[52] instanceof bk ? 2 : 0;

        require(world.b(20, 79, 20, 1, 0), "mob-spawner placement support failed");
        fy stack = new fy(52, 1, 0);
        boolean placed = ej.c[52].a(stack, player, world, 20, 79, 20, 1);
        bx placedEntity = tile(world, 20, 80, 20);
        registry |= placedEntity != null ? 4 : 0;
        int placedTile = matches(placedEntity, "Pig", 20);

        require(world.b(24, 80, 20, 52, 0), "mob-spawner break fixture failed");
        int entities = world.b.size();
        int breakBefore = state(world, 24, 80, 20);
        float strength = na.at.a(player);
        require(world.e(24, 80, 20, 0), "mob-spawner removal failed");
        na.at.a(world, player, 24, 80, 20, 0);
        int breakAfter = state(world, 24, 80, 20);
        int dropDelta = world.b.size() - entities;

        require(world.b(36, 92, 36, 52, 0), "mob-spawner persistence fixture failed");
        bx savedTile = tile(world, 36, 92, 36);
        savedTile.a("Zombie");
        savedTile.a = 37;
        iq chunkTag = new iq();
        mg.a(world.c(2, 2), world, chunkTag);
        hi loaded = mg.a(world, chunkTag);
        bx restored = (bx) loaded.d(4, 92, 4);
        int savedState = loaded.a(4, 92, 4) * 100 + loaded.b(4, 92, 4);

        require(world.b(20, 80, 28, 52, 0), "mob-spawner tick fixture failed");
        bx ticking = tile(world, 20, 80, 28);
        ticking.g_();
        int farDelay = ticking.a;
        player.c(20.5D, 80.5D, 28.5D);
        require(world.b(player), "mob-spawner player fixture failed");
        ticking.g_();
        int nearDelay = ticking.a;

        require(world.b(32, 80, 20, 52, 0), "mob-spawner neighbor fixture failed");
        na.at.b(world, 32, 80, 20, 1);
        na.at.b(world, 32, 80, 20, 69);
        bx neighbor = tile(world, 32, 80, 20);

        OracleMobSpawnerProbe result = new OracleMobSpawnerProbe(registry, placed ? 1 : 0,
                state(world, 20, 80, 20), stack.a, placedTile,
                strength > 0F && !Float.isInfinite(strength) ? 1 : 0, breakBefore, breakAfter,
                dropDelta, savedState, matches(restored, "Zombie", 37), restored.a,
                na.n[52] ? 1 : 0, farDelay, nearDelay, state(world, 32, 80, 20),
                matches(neighbor, "Pig", 20), neighbor.a);
        result.validate();
        return result;
    }
    String registry() {
        return "block=52:BlockMobSpawner,item=52:ItemBlock,tile=TileEntityMobSpawner";
    }
    String placement() { return "item=52x1->0,placed=52:0,tile=Pig:20"; }
    String lifecycle() { return "break=52:0->0:0,strength=finite,drops=none"; }
    String persistence() { return "chunk-nbt=52:0+Zombie:37"; }
    String timing() { return "scheduled=F,out-of-range=20,near-player=19"; }
    String neighbors() { return "stone+lever=stable-52:0+Pig:20"; }
    private void validate() {
        require(registryMask == 7, "mob-spawner registry drifted");
        require(placementRoute == 1 && placedState == 5200 && stackAfter == 0
                && placedTile == 1, "mob-spawner item placement drifted");
        require(strengthClass == 1 && breakBefore == 5200 && breakAfter == 0
                && dropDelta == 0, "mob-spawner break or drop drifted");
        require(savedState == 5200 && savedEntity == 1 && savedDelay == 37,
                "mob-spawner chunk NBT drifted");
        require(tickMask == 0 && farDelay == 20 && nearDelay == 19,
                "mob-spawner activation tick drifted");
        require(neighborState == 5200 && neighborEntity == 1 && neighborDelay == 20,
                "mob-spawner neighbor stability drifted");
    }
    private static int matches(bx tile, String id, int delay) {
        if (tile == null || tile.a != delay)
            return 0;
        iq tag = new iq();
        tile.b(tag);
        return id.equals(tag.i("EntityId")) ? 1 : 0;
    }
    private static bx tile(dj world, int x, int y, int z) {
        return (bx) world.b(x, y, z);
    }
    private static int state(dj world, int x, int y, int z) {
        return world.a(x, y, z) * 100 + world.c(x, y, z);
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new IllegalStateException(message);
    }
}
