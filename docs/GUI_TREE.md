# Game UI Tree

The first Game UI Tree slice promotes the existing inventory selectors to a
neutral, lifecycle-guarded public contract. Callers use `UiMinecraftRuntime.ui()`
after a world is loaded. The API contains no mapped, RetroMCP, LWJGL, or
Minecraft types.

## Stable contracts

| Type | Behavior |
| --- | --- |
| `GameUi.screen()` | `inventory` while `GuiInventory` is open; empty when no screen is open |
| `GameUi.nodes()` | Immutable snapshot: one `screen/inventory` node plus 45 `slot` nodes |
| `GameUi.node(role, name)` | Exact role+name lookup; missing nodes fail closed |
| `GameUi.slot(index)` | Inventory slot `0..44` as a `GameUiNode` |
| `GameUi.openInventory()` | Queues the vanilla inventory key; the screen appears on the next tick |
| `GameUi.close()` | Queues Escape when a promoted screen is open; no-op when closed |
| `GameUi.click(node)` | Left-clicks a `slot` node through vanilla container click |

`GameUiNode` is an immutable value. Item ID `-1` means empty or not applicable.
Unsupported screens fail closed instead of leaking class names.

`GameUiSpec` is the declared form of the same tree. `Ui.screen/row/slot`
authors it; a builder can emit it; a live `GameUi` can match it. See
`docs/GUI_SPEC.md`.

## Non-claims

This slice does not cover every Minecraft GUI, layout/pixel geometry, text
fields, buttons, drag/drop, item transfers, visual regression, or native
rendering. Those require later contracts and their own official-JAR evidence.
