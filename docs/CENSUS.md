# Registry Census Contract

## Scope

Exports the complete b1.7.3 registry state of the controlled client as
canonical checksum-protected documents: every registered block, every item
with stack/damage bounds, all crafting recipes, and all furnace smelts.

## Command

```text
worldline census <out-dir>
```

Writes `<out-dir>/<section>.wlcensus` (create-new) for sections
`blocks`, `items`, `recipes`, `smelts`. Each file is a canonical
`WORLDLINE-CENSUS/1` document: sorted rows (`b020=BlockGlass`, `i00265=ItemIngotIron|stack=64|damage=0`,
`r0007 out=5x4 in=17,17,17,17`, `s00000015 in=15 out=265x1`) plus a body
SHA-256. Byte-deterministic across runs; exit 0 on success.

## Boundary

`worldline.analysis.CensusRunner` is the neutral contract; the b1.7.3 adapter reads `Block.blocksList`,
`Item.itemsList`, `CraftingManager`, and `FurnaceRecipes` after a controlled
boot. Recipe inputs are read through mapped-field reflection.

## Non-claims

No localized display names (b1.7.3 has no stable identifiers), no entity-type
registry (the era predates one), no fuel burn values, no metadata-state
enumeration.
