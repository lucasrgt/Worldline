# M62 Workbench Window

M62 adds a typed read-only workbench boundary while preserving the exact
protocol-14 distinction between the Packet100 descriptor and the Packet104
combined view. Packet100 reports type 1, title `Crafting`, and nine container
slots. Those nine are the 3x3 matrix only; the combined container also includes
result slot 0, so the player inventory begins at offset 10 and the full view has
46 slots.

`RemoteWindowDescriptor.containerSlots()` remains the exact wire-declared value
9. `playerTailOffset()` exposes 10 and `totalSlots()` exposes 46. The same
derived layout preserves the already-qualified chest 27/63 and furnace 3/39
shapes. Packet104 tail reconciliation and later Packet103 updates use that one
offset, preventing workbench matrix slot 9 from being confused with personal
slot 9.

The official qualification places workbench block 58, seeds one stone in
personal slot 36, and activates the block with an empty selected hand. The
matching combined view contains empty result/matrix slots 0-9 and the exact
stone at combined slot 37. Explicit close is allowed only while the cursor,
result, and matrix are empty, then M58's accepted window-0 action proves server
restoration.

## Boundaries

M62 claims typed workbench open, immutable read, exact declared/combined layout,
and safe empty-grid close. It does not claim matrix writes, recipes, derived
results, ingredient consumption, right/shift clicks, nonempty-grid close
semantics, block removal, or server-forced close handling.
