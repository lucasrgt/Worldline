# Screen Export Contract

## Scope

Exports the open inventory semantic tree as a self-contained page: boot,
open the vanilla inventory through the controlled GUI boundary, walk the
semantic node list (role/name/index/item/count per node), and render a
deterministic HTML table.

## Command

```text
worldline ui <output.html>
```

Byte-deterministic across runs; slots are highlighted and empty stacks are
marked; no scripts. Exit 0 on success.

## Boundary

`worldline.analysis.UiPageRunner` is the neutral contract; the adapter opens
`GuiInventory` over the controlled client and serializes the same
`GameUiNode` tree used by GUI evidence milestones and Butter screens.

## Non-claims

No arbitrary-screen support beyond promoted GUI boundaries, no pixel
screenshots, no interaction replay from the page itself.
