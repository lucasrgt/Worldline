# Registry Census Contract

## Scope

Exports the complete b1.7.3 registry state of the controlled client as
canonical checksum-protected documents: every registered block, every item
with stack/damage bounds, every `EntityList` entry, all crafting recipes, and
all furnace smelts.

## Command

```text
worldline census <out-dir>
```

Writes `<out-dir>/<section>.wlcensus` (create-new) for sections
`blocks`, `items`, `entities`, `recipes`, `smelts`. Each file is a canonical
`WORLDLINE-CENSUS/1` document: sorted rows (`b020=BlockGlass`, `i00265=ItemIngotIron|stack=64|damage=0`,
`e090=name=Pig|class=EntityPig`, `r0007 out=5x4 in=17,17,17,17`,
`s00000015 in=15 out=265x1`) plus a body
SHA-256. Byte-deterministic across runs; exit 0 on success.

The command also writes `registry.wlevidence` and
`entity-registry.wlevidence`. Those public TestKit documents deterministically
expand the two registry captures into 96 block and 24 entity
`registry-presence` claims.

## Boundary

`worldline.analysis.CensusRunner` is the neutral contract; the b1.7.3 adapter reads `Block.blocksList`,
`Item.itemsList`, `EntityList`, `CraftingManager`, and `FurnaceRecipes` after a
controlled boot. Entity IDs, canonical names, and mapped implementation classes
come from the era's exact `IDtoClassMapping` and `classToStringMapping` tables.
Recipe inputs are read through mapped-field reflection.

## Non-claims

No localized display names (b1.7.3 has no stable resource identifiers), no
entity behavior or spawn-rule inference from registry membership, no fuel burn
values, and no metadata-state enumeration.
