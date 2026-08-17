# Invariant Engine

The Invariant Engine is a fail-closed observer and a stable milestone. A
scenario can feed samples
directly, or attach the engine so every controlled tick records items, blocks,
entities, chunk imports, wear, health, and world time.

```text
runtime.watch(InvariantEngine.standard(runtime))
runtime.tick()
    -->  items, blocks, entities, imports, wear, health, time
```

`GamePlayer.items()` totals main inventory, armor, and the cursor stack.
`GameWorld.items()` totals dropped `EntityItem` stacks and `IInventory`
containers. `GameWorld.blocks()` totals non-air block IDs in loaded chunks.
`GameWorld.loadedChunks()` / `itemsInChunks` / `blocksInChunks` mark values
that appeared only because a chunk loaded. `GamePlayer.wear()` and
`GameWorld.wear()` total damageable-stack counts and summed damage.
None of these methods open a screen or mutate state.

`InvariantEngine.itemConservation()` stays item-only.
`InvariantEngine.standard(runtime)` runs the six rules below. Trace and
mod-test diffs print `invariant=<rule>` for known conservation fields
(`block65` to `block-conservation`, `health` to `health-conservation`).
Unknown fields stay empty.

```java
InvariantMinecraftRuntime runtime = B173Runtimes.create(seed);
runtime.bootHeadless();
runtime.loadWorld(source);
runtime.watch(InvariantEngine.standard(runtime));
runtime.tick(8);
```

## Rules

| Rule | Holds when | Fails when |
| --- | --- | --- |
| `item-conservation` | Loss, transfers, chunk-item imports, recipes, or cause drops explain a gain | An item ID appears with no matching spend |
| `entity-spawn` | Chunk-entity imports, thrown eggs (item 344), one baby per two remaining parents, grass/water/spawner hosts, or slime split | A living type appears without a cause |
| `block-conservation` | Chunk-block imports, 1:1 ID swaps, fluid/fire/plant presence, sapling-to-tree, or cobble/obsidian from fluid | A block ID appears with no matching spend |
| `health-conservation` | Health falls, peaceful regen heals at most one point, lost food covers the rise, or a cake block covers one bite | Health jumps past food plus peaceful regen |
| `durability-conservation` | Damageable stacks wear or disappear | The same or fewer stacks of an ID lose damage points |
| `time-monotonic` | World time stays or advances | World time moves backward |

Item conservation compares consecutive samples. Extra loss is allowed.
Unexplained creation fails closed.

The Beta 1.7.3 adapter snapshots crafting, smelting, block drops (including
sampled random quantities), mob death maxima, chicken eggs, caught fish,
sheep wool, dirt/grass/farmland and fluid/fire/lit-block transforms, plant
growth, cobble/obsidian from fluid, sapling-to-tree, portals, falling sand,
thrown items, grass/water/spawner/netherrack hosts, slime split, `ItemFood`
heal amounts, and one cake-block bite. `GameWorld.peaceful()` follows
`World.difficultySetting`. The controlled-client 16-tick cycle watches
`standard(runtime)` on the live mapped client.

There is no vanilla energy bus. Fluid belongs in block transforms. Do not
invent an energy invariant.

## Stable contracts

| Type | Behavior |
| --- | --- |
| `ItemCensus` | Immutable item-ID totals |
| `EntityCensus` | Immutable living-entity type totals |
| `WearCensus` | Immutable damageable-stack counts and summed damage |
| `ItemRecipe` | Consume inputs / produce outputs, including container leftovers |
| `CauseDrop` | Death or presence cause with maximum outputs |
| `FoodHeal` | One food item ID and the health it restores |
| `InvariantSample` | One tick: items, blocks, entities, imports, wear, health, time |
| `GameWorld.blocks()` | Loaded non-air block ID census |
| `GameWorld.loadedChunks()` | Packed loaded chunk keys |
| `InvariantMinecraftRuntime.recipes` | Crafting, smelting, and block drops |
| `InvariantMinecraftRuntime.drops` | Mob, fishing, laying, and shear causes |
| `InvariantMinecraftRuntime.transforms` | Block-ID swaps such as dirt to grass |
| `InvariantMinecraftRuntime.fluids` | Fluid and fire presence causes |
| `InvariantMinecraftRuntime.foods` | Food item IDs, heal amounts, and cake-block bite |
| `InvariantMinecraftRuntime.spawns` | Host-to-entity spawn rules |
| `ItemConservation` | Consecutive samples; unexplained item gains fail closed |
| `EntitySpawn` | Unexplained living-entity creation fails closed |
| `BlockConservation` | Unexplained block-ID creation fails closed |
| `HealthConservation` | Unearned health gain fails closed |
| `DurabilityConservation` | Repair without a new stack fails closed |
| `TimeMonotonic` | World time does not move backward |

## Non-claims

Player-death inventory dumps and dye/damage variants that share one item ID
are not distinguished. Durability wear is a separate `WearCensus`, so a used
pickaxe is not treated as a new item. Glass and ice quantity 0 is extra loss.
Double-chest GUI wrappers are not counted separately; each chest tile entity
is counted once. There is no energy invariant. Particle `EntityFX` types are
not in the living census. Host rules allow up to `max` creations per
observation when the host is present; they do not prove a specific spawn
algorithm.
