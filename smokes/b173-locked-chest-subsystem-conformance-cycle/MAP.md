<!-- worldline-map-schema=1 -->
<!-- boundary=b173-locked-chest-subsystem-conformance-cycle -->
<!-- nonclaims=natural-generation-distribution,inventory-container-semantics,tile-entity-lifecycle,random-tick-delay-distribution,native-render -->
<!-- frozen-trace=eeab74a9f384d7557e7f38fa181fcfc8a7f579205a245f495f8ba16d89f81374 -->

# Beta 1.7.3 locked-chest subsystem conformance

`LockedChestSubsystemFixture` treats block `95` as its actual native class rather than as a chest
variant. A supplied `95:0` item follows the native ItemBlock placement route, creates one `95:0`
cell, and consumes the one-item stack.

Zero hardness yields infinite player-relative strength. The native server harvest sequence removes
the cell immediately and produces one `95:0` item entity. Native chunk serialization preserves an
unticked `95:0` cell.

Locked chest has an exact full-cube collision box, opacity 255, emission 15, random-tick
enrollment, a direct tick callback that replaces itself with air, and stable stone and lever
neighbor notifications. Registry presence is already covered by the universal registry claim, so
this milestone promotes the nine previously unknown claims.

The Census profile is corrected from `container,tile-entity` to
`locked-chest,luminous,random-tick,simple-solid`: `BlockLockedChest` extends `Block`, not
`BlockContainer`, and creates no tile entity. This map does not claim natural generation,
inventory/container behavior, tile-entity lifecycle, random-tick delay distribution, or the
already-qualified native render.

Frozen aggregate signal:
`family=locked-chest-subsystem,subject=95,claims=9,domain=95:0,item-placement=95x1>0,break=infinite+removed,drop=95x1,persistence=chunk-nbt,collision=full,light=255:15,ticks=random-T+callback-remove,neighbors=stable,oracle=MATCH`.

Qualified semantic signature: `eeab74a9f384d7557e7f38fa181fcfc8a7f579205a245f495f8ba16d89f81374`.
