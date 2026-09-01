<!-- worldline-map-schema=1 -->
<!-- boundary=b173-bedrock-subsystem-conformance-cycle -->
<!-- nonclaims=natural-generation-distribution,blast-resistance,piston-mobility,creative-or-command-availability,native-render -->
<!-- frozen-trace=92c66dfc942102809b5319da6632cb9dd22f382c12dd1db9d7472e2b5bac2e34 -->

# Beta 1.7.3 bedrock subsystem conformance

`BedrockSubsystemFixture` treats block `7` as an unbreakable lifecycle rather than merely an
opaque full cube. A supplied `7:0` item follows the native ItemBlock placement route, creates one
`7:0` cell, and consumes the one-item stack.

The native player-relative strength gate returns zero. Consequently the server break algorithm
does not remove the cell and cannot enter its harvest/drop branch; the legal break context has an
empty drop surface. Native chunk serialization preserves `7:0`.

Bedrock has an exact full-cube collision box, opacity 255, emission zero, no random-tick scheduler
enrollment, a stable direct tick callback, and stable stone and lever neighbor notifications.
Registry presence is already covered by the universal registry claim, so this milestone promotes
the nine previously unknown claims.

The map does not claim natural generation distribution, explosion resistance, piston mobility,
creative or command availability, or native rendering.

Frozen aggregate signal:
`family=bedrock-subsystem,subject=7,claims=9,domain=7:0,item-placement=7x1>0,break=strength-0+stable,drop=none,persistence=chunk-nbt,collision=full,light=255:0,ticks=scheduled-F+callback-stable,neighbors=stable,oracle=MATCH`.

Qualified semantic signature: `92c66dfc942102809b5319da6632cb9dd22f382c12dd1db9d7472e2b5bac2e34`.
