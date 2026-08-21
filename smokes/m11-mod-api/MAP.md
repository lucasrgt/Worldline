# M11 Mod API v2 Smoke Map

## Objective

Prove that a descriptor-selected mod can use the expanded b1.7.3 mod surface:
lifecycle hooks (`onLoad`/`onDispose`), stable domain handles
(`world()`/`player()`), scheduled actions (`at`), entity spawn/remove,
inventory give, and container reads, all under the controlled runtime.

## Oracle

The controlled mapped client is exercised twice in fresh processes; both runs
must produce byte-identical stdout including the canonical trace signature.
Rejection modes assert fail-closed propagation of past scheduling and unknown
spawn types through `installMod`.

## Mappings

- `worldline.b173.B173ModContext` delegates to the M3 `GameWorld`/`GamePlayer`
  adapter handles already covered by the M3 differential oracle.
- Spawn table entries construct mapped `Entity*` classes and register them via
  `World.entityJoinedWorld`; removal uses `World.setEntityDead`.
- Give delegates to `InventoryPlayer.addItemStackToInventory` merge semantics.
- Container reads delegate to `World.getBlockTileEntity` lazy creation for
  `Block.isBlockContainer` blocks.

## Exclusions

- No official-JAR differential is re-run here; the underlying block, entity,
  and inventory mutations reuse boundaries already differentially proven.
- Only registered semantic spawn types are claimed; unregistered types must
  fail closed.
- Multi-mod ordering evidence lives in m13; attested run evidence lives in m12.

## Pass conditions

- `WORLDLINE_M11_MOD=PASS` with identical traces across two processes.
- onLoad effects visible: glass at (8,65,8), five iron given, live pig spawned.
- Scheduled action fires exactly at tick 3: glass at (9,65,9).
- remove() retires the pig; second remove reports false; census reads work.
- DisposeMarker observes onDispose after runtime close.
- reject-schedule and reject-spawn exit cleanly with WORLDLINE_M11_REJECT.
- Frozen evidence SHA-256 matches smoke.properties.

Frozen expected signature SHA-256: `bf7f56d37682866ccbc26739474858244b2f70a84394df10f4fbf9277ff36f44`
