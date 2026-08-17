# Game UI Tree Differential Map

## Claim

Worldline exposes a neutral inventory UI tree. A subject using `GameUi`
produces the same observable screen, node count, and slot contents as an
independent oracle using official obfuscated b1.7.3 GUI fields and methods.

## Scenario

Both sides construct the same headless world and local player. They record the
closed tree, open inventory with key 18, advance one client tick, click slot 0,
record the open tree, close with Escape, advance one tick, and record the
closed tree again.

The trace contains screen occupancy, node count, and item id/count for slots 0
and 44. Four fresh JVMs - two per side - must agree and equal:

```text
ab13a631ed766de32f2947fae1a6e0a86d9b6cde3cbc7e1557ff76f76ccc60cf
```

## Exact boundary

The subject imports b1.7.3 code only to obtain the runtime factory; every GUI
interaction is through `UiMinecraftRuntime` and `worldline-api` types. The
oracle is compiled directly against the hash-verified official JAR and uses no
Game UI implementation classes.

Official names used by the oracle: `currentScreen` `r`, `GuiInventory` `ue`,
`GuiContainer.inventorySlots` `j`, `Container.slots` `e` / `windowId` `f`,
`Slot.getStack` `a`, `ItemStack.itemID` `c` / `stackSize` `a`, and
`PlayerController.func_27174_a` `a`.

## Non-claims

The fixture covers one empty player inventory and the inventory screen only.
It does not establish item movement, other screens, layout, or render pixels.
The normative scope is in `docs/GUI_TREE.md`. The GO audit is
`docs/GUI_CYCLE.md`.
