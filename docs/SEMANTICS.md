# Semantic Mappings

The semantic catalog annotates controlled b1.7.3 symbols without
trusting decompiled source as exact. Each entry binds a mapped or Worldline
boundary symbol to a role, category, read/write sets, external dependency,
evidence tokens, an optional official client alias, and a confidence in parts
per 10,000.

```text
SemanticCatalog.standard()
  -->  24 categories, 188 required roles
  -->  role("CLIENT_TICK_ROOT") / category("clock") / symbol(owner, name)
```

Unknown roles, unknown categories, duplicate roles, duplicate owner+name
pairs, confidence below 7000, and incomplete categories fail closed. The
official JAR remains the behavioral oracle; this catalog records what
Worldline already proved.

Official aliases come from `symbols.map` files and official-JAR oracles when
the named symbol is present there. Worldline-only symbols keep an empty
official name.

## Categories

| Category | Required roles |
| --- | --- |
| `clock` | `CLIENT_CLOCK_SOURCE`, `CLIENT_CLOCK_ACCUMULATOR`, `WORLD_TIME`, `CLIENT_SYSTEM_TIME` |
| `rng` | `WORLD_RANDOM`, `ENTITY_RANDOM`, `CONTROLLED_SEED` |
| `input` | `KEYBOARD`, `KEYBOARD_NEXT`, `KEYBOARD_PUSH`, `KEYBOARD_RESET`, `MOUSE`, `MOUSE_BUTTON`, `MOUSE_PUSH`, `MOUSE_RESET`, `MOVEMENT`, `INVENTORY_KEY`, `ESCAPE_KEY` |
| `tick` | `CLIENT_TICK_ROOT`, `CLIENT_TICK_COUNTER`, `WORLD_TICK`, `ENTITY_UPDATE`, `CONTROLLER_TYPE`, `CONTROLLER_TICK`, `EFFECT_TICK` |
| `filesystem` | `VIRTUAL_FILESYSTEM`, `SAVE_HANDLER`, `STAT_FILE`, `STAT_WRITER`, `FS_FAIL`, `FS_JOURNAL`, `WORLD_LOAD`, `WORLD_LOCK`, `WORLD_FILE`, `CHUNK_LOAD` |
| `network` | `OFFLINE_SESSION`, `NETWORK_DISABLED` |
| `scheduler` | `TIMER_THREAD`, `TASK_SCHEDULER`, `SCHEDULER_ADVANCE` |
| `world` | `WORLD_TYPE`, `LOADED_ENTITY_LIST`, `TILE_ENTITIES`, `BLOCK_ACCESS`, `BLOCK_ID_READ`, `BLOCK_READ`, `BLOCK_WRITE`, `BLOCK_NOTIFY`, `WORLD_DIFFICULTY`, `WORLD_PROVIDER` |
| `block` | `BLOCK_TYPE`, `BLOCK_ID`, `BLOCK_STONE`, `BLOCK_BEDROCK`, `BLOCK_SAND`, `BLOCK_SAND_TYPE`, `BLOCK_SAND_FALL` |
| `chunk` | `CHUNK_TYPE`, `CHUNK_LOOKUP`, `CHUNK_POPULATE`, `CHUNK_POPULATED`, `CHUNK_NEVER_SAVE`, `CHUNK_RELIGHT`, `CHUNK_LOADER`, `LOADER_LOAD`, `LOADER_SAVE`, `LOADER_FLUSH` |
| `player` | `LOCAL_PLAYER`, `PLAYER_TYPE`, `LIVING_TYPE`, `PLAYER_NAME`, `PLAYER_HEALTH`, `HOTBAR_SLOT`, `PLAYER_INVENTORY`, `INVENTORY_FIELD` |
| `entity` | `ENTITY_TYPE`, `ENTITY_ID`, `ENTITY_POS_X`, `ENTITY_POS_Y`, `ENTITY_POS_Z`, `ENTITY_DEAD`, `ENTITY_SET_POSITION`, `ENTITY_SET_LOCATION`, `ENTITY_ITEM` |
| `inventory` | `INVENTORY_TYPE`, `PLAYER_ITEMS`, `WORLD_ITEMS`, `WORLD_BLOCKS`, `MAIN_ITEMS`, `ARMOR_ITEMS`, `CURSOR_STACK` |
| `item` | `ITEM_STACK`, `ITEM_ID`, `STACK_SIZE`, `ITEM_TYPE`, `ITEM_LOOKUP`, `ITEM_DAMAGE`, `CONTAINER_TYPE`, `SLOT_COUNT`, `SLOT_GET`, `ENTITY_ITEM_STACK` |
| `recipe` | `CRAFTING`, `CRAFTING_LIST`, `FURNACE`, `FURNACE_LIST`, `RECIPE_TYPE`, `RECIPE_OUTPUT`, `RECIPE_SHAPED`, `RECIPE_SHAPELESS` |
| `gui` | `CURRENT_SCREEN`, `INVENTORY_SCREEN`, `CONTAINER_CLICK`, `HUD_TYPE`, `HUD_TICK`, `HUD_COUNTER`, `GUI_OPEN`, `GUI_CLOSE`, `GUI_SLOT`, `GUI_CLICK`, `GUI_SCREEN`, `GUI_CONTAINER`, `GUI_SLOT_TYPE`, `CONTAINER_SLOTS`, `WINDOW_ID` |
| `render` | `DISPLAY`, `DISPLAY_CREATED`, `RENDER_ENGINE`, `RENDER_ENGINE_FIELD`, `ENTITY_RENDERER`, `ENTITY_RENDERER_FIELD`, `RENDERER_UPDATE`, `RENDERER_COUNTER`, `MOUSE_OVER`, `RENDER_GLOBAL`, `RENDER_GLOBAL_FIELD`, `CLOUD_UPDATE`, `CLOUD_OFFSET`, `EFFECT_RENDERER`, `EFFECT_UPDATE`, `HUD_FIELD`, `PLAYER_CONTROLLER_FIELD` |
| `audio` | `SOUND_MANAGER`, `HEADLESS_AUDIO` |
| `resource` | `TEXTURE_LOOKUP`, `DYNAMIC_TEXTURE` |
| `persistence` | `WORLD_SAVE`, `CHUNK_SAVE`, `PLAYER_SAVE`, `LOAD_INFO`, `SAVE_INTERFACE`, `EXTRA_CHUNK`, `CHUNK_FLUSH`, `EXTRA_DATA`, `SPAWN_SET`, `SPAWN_POSITION` |
| `save` | `HANDLER_LOAD`, `HANDLER_LOCK`, `HANDLER_LOADER`, `HANDLER_PLAYERS`, `HANDLER_INFO`, `HANDLER_PLAYER_DATA`, `HANDLER_CLOSE`, `HANDLER_FILE`, `PLAYER_FILES`, `PLAYER_WRITE`, `PLAYER_READ` |
| `lifecycle` | `CLIENT_TYPE`, `CLIENT_WORLD`, `CLIENT_PLAYER`, `CLIENT_SESSION`, `RUNTIME_FACTORY`, `BOOT_HEADLESS`, `LOAD_WORLD`, `MANUAL_TICK`, `CLOSE` |
| `lab` | `OBSERVATION`, `SNAPSHOT`, `CHECKPOINT`, `HYPOTHESIS`, `COMPARISON` |
| `domain` | `WORLD_API`, `PLAYER_API`, `ENTITY_API`, `BLOCK_STATE`, `READ_BLOCK`, `WRITE_BLOCK`, `LIST_ENTITIES`, `TELEPORT` |

These are the PDF control boundaries, every named leftover from both
`symbols.map` files, adapter observation fields, GUI/M3 oracle aliases, and
the item/recipe/domain surfaces Worldline already executes. There is no
energy category.

## Stable contracts

| Type | Behavior |
| --- | --- |
| `SemanticMapping` | Immutable symbol annotation; empty tokens and confidence `0` fail closed |
| `SemanticMapping.official` | Obfuscated client alias, or empty for Worldline-only symbols |
| `SemanticMapping.known` | True when confidence is at least `7000` |
| `SemanticCatalog.standard` | Assembles every category; incomplete sets fail closed |
| `SemanticCatalog.role` | Exact role lookup |
| `SemanticCatalog.category` | Immutable snapshot of one category |
| `SemanticCatalog.symbol` | Exact owner+name lookup; ambiguous or duplicate symbols fail closed |
| `SemanticCatalog.canonical` / `sha256` | Stable catalog document |
| `SemanticGraph` | Fail-closed read/write/dep edges over uppercase category tokens |

The API owns `SemanticMapping`. The `semantics` module owns roles, category
files, and the catalog. Neither module contains RetroMCP, LWJGL, or Minecraft
types.

## CLI

```text
worldline semantics show
worldline semantics graph
worldline semantics category item
worldline semantics role CLIENT_WORLD
```

## Non-claims

This catalog does not rename the official JAR, prove every Minecraft field,
run a new official-JAR differential, or treat confidence as a probability of
correct decompilation. It catalogs the controlled boundary Worldline already
executes. Unlisted symbols remain unknown and fail closed. Richer PDF mapping
features (live call-graph `written_by` / `read_by`, per-symbol invariants, and
Hypothesis Engine experiments) remain out of scope.
