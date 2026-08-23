# Worldline GUI

Standalone Beta 1.7.3 GUI builder. It authors a Flutter-inspired tree
(`Ui.screen` / `row` / `slot`) and emits the same `GameUiSpec` Worldline
matches against a live client. It does not own tiles, ports, or multiblock
structure — those stay in Aero Machine Maker or any other mod tool.

```text
tree  →  GameUiSpec JSON  →  Worldline GameUi.nodes()
      →  Ui.screen(...) Java
      →  176×166 pixel preview
```

Hero UI is chrome only. The canvas stays vanilla Minecraft pixels.

## Run

```text
npm install
npm test
npm run dev
```

## First slice

- Declare `screen`, `row`, `column`, `slot`, `progress`, `energy`, `tank`,
  `search`, and optional player inventory
- Compile to Worldline's crusher node order (41 nodes with 36 `player.N` slots)
- Pack `row` / `column` onto the 176×166 panel; energy and tanks hug the left
- Export JSON + Java. No PNG, no Container codegen, no live runtime hookup

## Non-claims

This app is not a Flutter engine, not a constraint solver, and not an Aero
rewrite. A later slice can open a generated screen in Worldline and require
`spec.matchesStructure(runtime.ui().nodes())`.
