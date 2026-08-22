# Game UI Actions Differential Map

## Claim

Worldline exposes vanilla inventory viewport and slot geometry, semantic
drag/drop, and secondary click with the same observable result as an
independent official Minecraft Beta 1.7.3 client-JAR oracle.

## Scenario

Both sides create the same fresh headless world and place four stone items in
hotbar slot 0. They open the player inventory, record viewport and container
slot 36 geometry, move the complete stack from slot 36 to slot 37 through the
public drag operation, then secondary-click slot 37 and empty slot 38. The
final visible inventory contains two stone in slot 37 and one in slot 38; the
remaining cursor item is outside this claim.

Two fresh subject JVMs and two fresh official-oracle JVMs must produce the
same trace and frozen signature. The first qualification intentionally starts
with `expected.signature=PENDING`; the observed signature must be reviewed,
frozen, and reproduced before this acceptance smoke can pass.

## Exact boundary

The subject uses `GameUi`, lazy slot locators, `GameUiContract`,
`GameUiLayout`, `dragTo`, and `rightClick` after adapter-private fixture setup.
The oracle compiles directly against the hash-verified official JAR and
calculates raw mouse coordinates independently from obfuscated GUI and slot
fields. Official class names are not imported by Worldline product modules.

## Non-claims

This fixture covers the vanilla player inventory only. It does not claim
continuous mouse motion, creative inventory, arbitrary container rules,
keyboard focus, Butter widgets, native framebuffer capture, or Aero rendering.
