# BetaVault Item Reference Extension Map

This is a parallel controlled-runtime extension. It does not consume a numbered
Worldline milestone and does not change vanilla behavior when the fixture mod is
absent.

## Qualified boundary

- The neutral `itemref` module validates BetaVault-compatible canonical logical
  references without depending on BetaVault.
- A controlled StationAPI mixin adds one nullable logical reference to
  `ItemStack`.
- NBT read/write, `copy`, and `split` preserve the reference.
- Logical cells with identical vanilla ID/count/damage but distinct references
  do not compare as the same item.
- Packet 104, Packet 103, Packet 102, and Packet 15 codecs carry a strict
  opt-in reference suffix when both controlled endpoints load the extension.
- The runtime smoke proves Packet 104 across a real dedicated server/client
  boundary and a full server restart.
- The host record and mutation are the real BetaEnergistics integration, and
  the save-bound object store is the real pinned BetaVault implementation.

## Controlled reconciliation

StationAPI's automatic legacy slot delta knows only vanilla ID/count/damage.
The adapter therefore updates the screen handler's tracked snapshot while
`skipPacketSlotUpdates` is enabled, then sends one authoritative Packet 104
snapshot containing the logical references. This is an explicit controlled
boundary, not a vanilla claim.

## Exact runtime oracle

The create phase allocates one BetaEnergistics storage cell, inserts exactly
100 iron items, places its reference on a physical stone stand-in, and proves
copy, split, equality isolation, screen-handler visibility, and client receipt.
After clean save and stop, the reload phase reads the same player ItemStack NBT,
resolves the same reference, reads exactly 100 iron, and resynchronizes it to a
fresh graphical client.

## Nonclaims

- Packet 5 equipment and Packet 21 dropped-item scalar transports are not yet
  extended.
- This fixture does not retire the legacy BetaEnergistics damage registry.
- Compatibility with a vanilla or extension-absent peer is not claimed; the
  suffix is enabled only in a controlled runtime where both endpoints match.
- Packet 102 and Packet 15 have codec coverage in the extension but are not yet
  exercised by this smoke.
