# Game UI Tree

The first Game UI Tree slice promotes the existing inventory selectors to a
neutral, lifecycle-guarded public contract. Callers use `UiMinecraftRuntime.ui()`
after a world is loaded. The API contains no mapped, RetroMCP, LWJGL, or
Minecraft types.

## Stable contracts

| Type | Behavior |
| --- | --- |
| `GameUi.screen()` | `inventory` while `GuiInventory` is open; Butter `HostUi` screen id when a Butter GUI is open; empty when no screen is open |
| `GameUi.nodes()` | Inventory: one `screen/inventory` node plus 45 `slot` nodes. Butter: flattened `HostUi` nodes (`screen`, `slot`, `tank`, `energy`, `button`, ...) |
| `GameUi.click(node)` | Inventory: left-clicks a `slot` through vanilla container click. Butter: `HostUi.click(name)` |
| `GameUi.node(role, name)` | Exact role+name lookup; missing nodes fail closed |
| `GameUi.slot(index)` | Inventory slot `0..44` as a `GameUiNode` |
| `GameUi.openInventory()` | Queues the vanilla inventory key; the screen appears on the next tick |
| `GameUi.close()` | Queues Escape when a promoted screen is open; no-op when closed |

`GameUiNode` is an immutable value. Item ID `-1` means empty or not applicable.
On Butter screens, numeric widget values (energy, tanks, progress) travel in
`count`. `Slot` stacks travel in `itemId` and `count`; Butter slot `name` is the
semantic id, not the vanilla `"0"`..`"44"` inventory index. Unsupported vanilla screens fail closed instead of leaking class names.
Butter screens are recognized by implementing `butter.testing.HostUi`; the
adapter binds that contract reflectively so `worldline-api` stays Butter-free.

`GameUiSpec` is the declared form of the same tree. `Ui.screen/row/slot`
authors it; a builder can emit it; a live `GameUi` can match it. See
`docs/GUI_SPEC.md`.

## Non-claims

This slice does not cover every Minecraft GUI, layout/pixel geometry, text
fields, drag/drop, item transfers, visual regression, or native rendering.
Butter `HostUi` is consumed as a semantic tree, not as an official-JAR pixel
oracle. Those require later contracts and their own official-JAR evidence.
