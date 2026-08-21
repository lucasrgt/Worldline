# M11 Mod API v2 Contract

## Scope

M11 expands the b1.7.3 mod surface from a narrow block probe into the full
controlled domain. Mods keep the stable `B173Mod` entrypoint and gain lifecycle
hooks, the M3 handles, deterministic scheduling, entity spawn/removal,
inventory give, and container reads. All additions are additive; format 1
packages and existing `onTick`-only mods keep working unchanged.

## Entrypoint

```java
public interface B173Mod {
    void onTick(B173ModContext context);
    default void onLoad(B173ModContext context) {}
    default void onDispose() {}
}
```

`onLoad` runs once when the mod is installed into a loaded controlled world.
`onDispose` runs once in reverse install order before the runtime closes.
Exceptions propagate to the caller; installation rolls the mod out of the
callback list.

## Context

```java
public interface B173ModContext {
    int clientTick();
    int blockAt(int x, int y, int z);
    boolean setBlock(int x, int y, int z, int blockId);
    GameWorld world();   // stable M3 handle
    GamePlayer player(); // stable M3 local-player handle
    void at(int tick, Runnable action);
}
```

`at(tick, action)` schedules `action` for the start of controlled tick
`tick`, before that tick's mod callbacks, in insertion order. Scheduling a
past tick fails closed.

## Expanded world surface

Stable default methods on the neutral M3 types (adapters opt in by override;
the default implementations throw):

- `GameWorld.spawn(String type, GamePosition position)` - constructs one
  entity from a bounded semantic registry (`minecraft:pig`, `cow`, `sheep`,
  `chicken`, `zombie`, `skeleton`, `creeper`, `spider`, `slime`, `squid`,
  `wolf`) via mapped constructors and `World.entityJoinedWorld`.
- `GameWorld.remove(GameEntity entity)` - unmounts and marks the entity dead
  through `World.setEntityDead`; returns false for already-dead entities.
- `GameWorld.itemsAt(BlockPosition position)` - read-only item totals of the
  container tile entity at the position (lazy creation through
  `getBlockTileEntity`); empty census for non-containers.
- `GamePlayer.give(int itemId, int count)` - vanilla stack merging through
  `InventoryPlayer.addItemStackToInventory`; fails closed when the inventory
  is full or the request is unbounded.

## Multi-mod execution

`B173Runtime.installMods(List<B173Mod>)` installs several mods in list order.
Callbacks run in install order per tick; disposal runs in reverse order.

## Evidence

The m11 smoke executes a format 2 lifecycle mod against the controlled client
in two fresh processes with byte-identical traces, verifies scheduled and
lifecycle effects, removal, container census, and fail-closed rejection of
past scheduling and unknown spawn types. Frozen evidence SHA-256 lives in
`smokes/m11-mod-api/smoke.properties`.

## Non-claims

M11 does not claim arbitrary entity-type registration, tile-entity mutation,
container writes, dimension access, permissions, isolation between mods, or a
security sandbox. Spawn entries are a closed registry; new types require their
own evidence.
