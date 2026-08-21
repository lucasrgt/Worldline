# Semantic Mapping Audit

Status: rebased onto `origin/main` at `1755fa8` (Worldline v1.55.0 /
M67). Inventory started at M35; M36-M47 added Worldline route/batch API
composition only. M48-M67 added oracled protocol-14 inventory, place,
combat, and window packets. The closed catalog had 196 roles on main;
earlier mapping commits promoted 25 boundaries to 221; this pass adds
17 packet classes to 238.

The official Beta 1.7.3 JAR remains the behavioral oracle. Mixins,
`symbols.map` rows, `mappings.tiny`, and executable smokes confirm
owners and descriptors. Decompiled source is not treated as exact.

Aero Model Lib stays an external project. Overlay mixins live under
`worldline.aero.mixin`. `adapters/aero-model-lib/semantics/manifest.properties`
lists nine oracled `worldline/aero/` sites. `AdapterManifest.rejectExternal`
allows that prefix and still rejects `aero/` and `/aero/modellib`.
`aero.modellib.*` stays out of `SemanticCatalog.standard()`. The M18
Aero spike remains a NON-CLAIM; this catalog refresh does not solve it.

## Coverage matrix

| Domain | Catalogued | Oracled gap promoted now | Inventory only | Blocked |
| --- | --- | --- | --- | --- |
| Player lifecycle | pose fields, death flag, teleport, Packet10/12/13, stance | Packet8 health, Packet7/38 combat wire | collision, gravity, AABB, respawn, dimension | physics smokes |
| Multiplayer world | Packet12/13/stance, Packet3 chat, Packet50-53 cache sites on b173-server | Packet5/16 equipment and hotbar, Packet21/22/29 drop-collect | NetClientHandler, WorldClient, weather, handshake/login/keep-alive | native MP client smoke |
| Native rendering | COMPILE_CHUNKS, Display, EntityRenderer tick, Tessellator cluster, loadRenderers, camera, CHUNK_REBUILD | (none this pass) | invalidate, sort, mark-blocks, particles vs EFFECT_UPDATE alias | non-zero oracle for remaining Aero intercepts |
| Gameplay / UI | inventory, recipes, GUI tree, input, Packet14 wire dig, Container.slots, Slot.getStack | Packet100-106 window wire, Packet15 place, Packet200 statistic | pause, clickBlock, sendUseItem, client attack methods | dedicated fixtures for those client members |

## Promoted this pass

| Role | Owner.member | Evidence |
| --- | --- | --- |
| `PACKET5_PLAYER_INVENTORY` | `Packet5PlayerInventory` | mappings.tiny `s`; M49/M65; `B173PeerEquipmentTracker#equipment` |
| `PACKET7_USE_ENTITY` | `Packet7UseEntity` | mappings.tiny `a`; M66; `B173CombatChannel#attack` |
| `PACKET8_UPDATE_HEALTH` | `Packet8UpdateHealth` | mappings.tiny `eu`; M66; `B173CombatTracker#health` |
| `PACKET15_PLACE` | `Packet15Place` | mappings.tiny `gx`; M53/M54; `B173HeldItemChannel#place` |
| `PACKET16_BLOCK_ITEM_SWITCH` | `Packet16BlockItemSwitch` | mappings.tiny `ho`; M49; `B173HeldItemChannel#select` |
| `PACKET21_PICKUP_SPAWN` | `Packet21PickupSpawn` | mappings.tiny `nd`; M51/M52; `B173DroppedItemTracker#spawn` |
| `PACKET22_COLLECT` | `Packet22Collect` | mappings.tiny `di`; M52; `B173DroppedItemTracker#collect` |
| `PACKET29_DESTROY_ENTITY` | `Packet29DestroyEntity` | mappings.tiny `rv`; M52; `B173DroppedItemTracker#destroy` |
| `PACKET38_ENTITY_STATUS` | `Packet38EntityStatus` | mappings.tiny `jf`; M66; `B173CombatTracker#status` |
| `PACKET100_OPEN_WINDOW` | `Packet100OpenWindow` | mappings.tiny `iw`; M54/M62; `B173WindowTracker#open` |
| `PACKET101_CLOSE_WINDOW` | `Packet101CloseWindow` | mappings.tiny `mn`; M58; `B173HeldItemChannel#closeWindow` |
| `PACKET102_WINDOW_CLICK` | `Packet102WindowClick` | mappings.tiny `qs`; M55; `B173ContainerPacket#write` |
| `PACKET103_SET_SLOT` | `Packet103SetSlot` | mappings.tiny `hq`; M48; `B173InventoryCodec#update` |
| `PACKET104_WINDOW_ITEMS` | `Packet104WindowItems` | mappings.tiny `kb`; M48/M54; `B173InventoryCodec#window` |
| `PACKET105_UPDATE_PROGRESSBAR` | `Packet105UpdateProgressbar` | mappings.tiny `mv`; M60; `B173FurnaceTracker#progress` |
| `PACKET106_TRANSACTION` | `Packet106Transaction` | mappings.tiny `oj`; M55/M56; `B173PersonalTransactionTracker#acknowledge` |
| `PACKET200_STATISTIC` | `Packet200Statistic` | mappings.tiny `of`; M61/M64; `B173ContainerTransactionTracker#statistic` |

Earlier mapping commits already catalogued Packet10/12/13/stance, Packet3,
Packet14, Packet50-53, Tessellator, `CHUNK_REBUILD`, `NibbleArray`,
`CONTAINER_SLOT_LIST`, and `SLOT_STACK`.

Packet50 `yPosition` is not catalogued: MCP name contradicts the oracled
chunk-Z wire. Packet200 is protocol-14 `Packet200Statistic`, not a guessed
later-protocol identity.

## Confirmed missing high-value roles (not promoted)

1. Handshake/login/keep-alive/kick packet classes until each has a
   unique named adapter site (Packet0 currently writes Packet10;
   Packet1/2 share `#connect`).
2. `WorldRenderer.markDirty` (`CHUNK_INVALIDATE`) — M14 usually sees zero
   invalidates. Not `RENDERER_UPDATE`.
3. `RenderGlobal.markRenderersForNewPosition` and `func_949_a`.
4. `Minecraft.isGamePaused`, `PlayerController.clickBlock`,
   `sendUseItem`, `Entity.attackEntityFrom`.
5. Native `NetClientHandler` / `WorldClient` — mappings.tiny only.
6. Aero intercepts `SORT_RENDERERS`, `MARK_BLOCKS_FOR_UPDATE`,
   `AMBIENT_DARKNESS`, and `CHUNK_INVALIDATE` — names exist, no non-zero
   oracle. Stay unpromoted and off the Aero adapter manifest.
7. Packet20 named-player spawn remains identity correlation, not a
   dedicated catalog role in this pass.

## Duplicates and conflicts

- `MOVEMENT` is `movementInput`, not physics or Packet13.
- `ENTITY_POS_*` vs `PACKET_*` vs `PlayerPose` are three boundaries.
- `SPAWN_SET` is world-info spawn, not player respawn.
- `CHUNK_LOAD` is disk; Packet50 is wire lifecycle.
- `WORLD_TIME` vs Packet4 vs `level.dat` Time.
- `EFFECT_UPDATE` is `B173Observation.rendererTick`, not
  `EffectRenderer.updateEffects` (`EFFECT_TICK`).
- `HEADLESS_AUDIO` is bound to `B173Boundaries`, which does not own audio.
- Yarn `WorldRenderer` is MCP `RenderGlobal`; yarn `ChunkBuilder` is MCP
  `WorldRenderer`.
- `CHUNK_REBUILD` is `WorldRenderer.updateRenderer`; `RENDERER_UPDATE` is
  `EntityRenderer.updateRenderer`.
- `PACKET14_BLOCK_DIG` is the wire packet; `clickBlock` is the client
  controller and stays uncatalogued.
- `PACKET15_PLACE` is the wire packet; `sendUseItem` stays uncatalogued.
- `PACKET7_USE_ENTITY` is the wire packet; `attackEntityFrom` stays
  uncatalogued.
- `PACKET102_WINDOW_CLICK` is the wire packet; `CONTAINER_CLICK` is
  `PlayerController.func_27174_a`.
- `PACKET104_WINDOW_ITEMS` is the wire snapshot; `CONTAINER_SLOT_LIST` is
  `Container.slots`; `CONTAINER_SLOTS` is `GuiContainer.inventorySlots`.
- `SLOT_STACK` is `Slot.getStack`; `SLOT_GET` is `IInventory.getStackInSlot`.
- `NibbleArray` is the M29 layout oracle; it is not `PACKET51_MAP_CHUNK`.
- Packet5 is equipment, not Packet50 prechunk.

## Unresolved, needing runtime evidence

Collision/gravity/AABB, death-to-respawn, dimension transition, native
client packet handlers, weather, audio play path, headless pause, and
`notifyAmbientDarknessChanged` MCP identity. The Aero compile spike is
still a NON-CLAIM.

## Next mappings

1. Split `B173WireClient#connect` so handshake and login can own unique
   sites; add a Packet255 kick oracle before cataloguing kick.
2. `CHUNK_INVALIDATE` / `SORT_RENDERERS` after a smoke asserts non-zero
   marks/sorts. Overlay mixins already live under `worldline.aero.mixin`.
3. `GAME_PAUSED` and GUI resolution once headless pause is asserted.
4. Do not promote AABB/gravity until an official-JAR collision oracle
   exists. Do not alias Packet14 with `clickBlock`.

Domain reports: `SEMANTICS_AUDIT_PLAYER.md`,
`SEMANTICS_AUDIT_MULTIPLAYER.md`, `SEMANTICS_AUDIT_RENDER.md`,
`SEMANTICS_AUDIT_GAMEPLAY.md`.
