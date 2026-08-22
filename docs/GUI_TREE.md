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
An advanced Butter or Aero screen may instead implement neutral `GameUi`
directly. The bridge then preserves its declared input, layout, and visual
capability interfaces without adding a Worldline dependency to the vanilla
adapter itself.

`GameUiSpec` is the declared form of the same tree. `Ui.screen/row/slot`
authors it; a builder can emit it; a live `GameUi` can match it. See
`docs/GUI_SPEC.md`.

## Cypress-style TestKit surface

Tests obtain the current semantic UI through `TestContext.ui()`. Lazy locators
are re-evaluated against the live tree:

```java
GameUi ui = context.ui();
ui.getByRole("slot").shouldHaveCount(45);
ui.getByLabel("Input").click().shouldHaveItem(265, 4);
ui.getByText("Search").focus().type("iron").press(GameUiKey.ENTER);
ui.getByName("source").dragTo(ui.getByName("target"));
ui.getByName("panel").shouldBeWithinViewport();
expect(context.screenshot("machine")).toMatchSnapshot("machine");
```

Every optional action is capability-gated. An adapter must implement
`GameUiInput`, `GameUiLayout`, or `GameUiVisual` and declare the corresponding
`GameUiCapability`; inconsistent declarations fail with an `E23xx` diagnostic.
`GameUiContract.validate` is the shared consumer gate for vanilla, Butter, and
Aero adapters.

| Capability | Neutral API | b1.7.3 vanilla adapter | Butter bridge | Aero consumer |
| --- | --- | --- | --- | --- |
| Semantic tree and locators | GO | GO | GO | available through bridge |
| Inventory lifecycle and slot click | GO | GO | semantic click only | consumer-specific |
| Keyboard, focus, pointer, drag/drop | GO | pending runtime evidence | pending extended host contract | pending consumer evidence |
| Bounds, viewport, clipping, overlap | GO | pending runtime evidence | pending extended host contract | pending consumer evidence |
| ARGB capture, exact diff, snapshots | GO | pending native capture | pending native capture | pending native capture |

The API being present is not evidence that an adapter supports it. Butter and
Aero become complete only after their external consumer suites pass the shared
contract plus serialized native runtime evidence.
