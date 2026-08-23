# Bidirectional Game UI Spec

Aero Machine Maker already authors machine GUIs as `guiComponents`
(`slot`, `big_slot`, `energy_bar`, `progress_arrow`, `flame`, tanks, search,
scroll). Worldline now speaks the same tree without pixels.

```text
Ui.screen / row / slot     -->  GameUiSpec  -->  declared tree
builder guiComponents      -->  GameUiSpec.fromBuilder(...)
live GameUi.nodes()        -->  spec.matchesStructure(live)
```

The declaration language is Flutter-inspired, not a Flutter engine. `row` and
`column` flatten. Leaves become DOM nodes. There is no constraint solver and
no runtime rebuild.

```java
Ui.screen("crusher",
    Ui.row("process",
        Ui.slot("input"),
        Ui.progress("craft"),
        Ui.slot("output")),
    Ui.energy("energy"),
    Ui.playerInventory());
```

The builder remains the author of layout and textures. Worldline remains the
author of observation and click. The shared document is role + name + slot
index.

## Role map

| Builder type | DOM role | Default name |
| --- | --- | --- |
| `slot`, `big_slot` | `slot` | `slotType` (`input`, `output`, `fuel`) |
| `progress_arrow` | `progress` | `craft` |
| `flame` | `progress` | `flame` |
| `energy_bar` | `energy` | `energy` |
| `fluid_tank*` | `tank` | `fluid` |
| `gas_tank*` | `tank` | `gas` |
| `search_box*` | `search` | `search` |
| `scrollbar*` | `scroll` | `scroll` |
| `separator` | omitted | |

Duplicate names become `input.1`, `input.2`. Machine specs append the same 36
player slots the Aero container codegen emits (`player.0` .. `player.35`).
An explicit `Part.name` overrides the default.

Vanilla containers are the other direction: `GameUiSpec.inventory()` and
`GameUiSpec.workbench()` are matched against the official JAR. The workbench
contract covers its result slot, 3x3 matrix, and 36 player slots as one exact
47-node tree.

## Intended Aero emit

When generating a machine, also write the declared tree Worldline can load:

```text
ui.node("slot", "input")
ui.node("slot", "output")
ui.node("progress", "craft")
ui.node("energy", "energy")
```

A generated `GuiCrusher` can use the same comparison. The official workbench
cycle already requires `spec.matchesStructure(runtime.ui().nodes())`; custom
machine runtime hookup remains a separate adapter concern.

## Non-claims

The spec does not encode x/y/w/h, PNG bytes, Java class names, or item
contents. Those stay in Aero codegen and live `GameUiNode` item fields.
