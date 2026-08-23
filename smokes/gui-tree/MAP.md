<!-- worldline-map-schema=1 -->
<!-- boundary=ui-tree-equivalence -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=87256440f1db54387671e3ad0c47a464afbe991d1933d339f37afce460e11b00 -->

# Game UI Tree Differential Map

## Claim

Worldline exposes neutral inventory and workbench UI trees. A subject using
`GameUi` produces the same observable screen, node count, and slot contents as
an independent oracle using official obfuscated b1.7.3 GUI fields and methods.
The authored `GameUiSpec.workbench()` must match all 47 live runtime nodes.

## Scenario

Both sides construct the same headless world and local player. They record the
closed tree, open inventory with key 18, advance one client tick, click slot 0,
record the open tree, and close with Escape. They then open the official
workbench container, compare the authored spec to the live tree, record its 46
slots plus root, and close it.

The trace contains screen identity, node count, and item id/count for the first
and last slots. Four fresh JVMs - two per side - must agree and equal:

```text
87256440f1db54387671e3ad0c47a464afbe991d1933d339f37afce460e11b00
```

## Exact boundary

The subject uses the b1.7.3 driver only to open the official workbench screen;
all structure comparison uses `GameUi`, `GameUiSpec`, and `worldline-api`
types. The oracle is compiled directly against the hash-verified official JAR
and uses no Game UI implementation classes.

Official names used by the oracle: `currentScreen` `r`, `GuiInventory` `ue`,
`GuiCrafting` `oo`,
`GuiContainer.inventorySlots` `j`, `Container.slots` `e` / `windowId` `f`,
`Slot.getStack` `a`, `ItemStack.itemID` `c` / `stackSize` `a`, and
`PlayerController.func_27174_a` `a`.

## Non-claims

The fixture covers one empty player inventory plus one empty workbench screen.
It does not establish item movement, other screens, layout, or render pixels.
The normative scope is in `docs/GUI_TREE.md`. The GO audit is
`docs/GUI_CYCLE.md`.
