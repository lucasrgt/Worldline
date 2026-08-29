<!-- worldline-map-schema=1 -->
<!-- boundary=b173-repeater-subsystem-conformance-cycle -->
<!-- nonclaims=short-pulse-filtering,torch-burnout,wire-strength,client-rendering,sound-events -->
<!-- frozen-trace=2b4d6f69cc7311f905e24eb766b78f1567cf9da3ddd4a390486624d6ee10870a -->

# Beta 1.7.3 repeater subsystem conformance

`RepeaterSubsystemFixture` treats idle repeater `93` and active repeater `94` as one timed
signal component. Item `356` materializes the idle block; an input-side signal materializes
the active block while preserving all sixteen orientation and delay metadata values. Active
destruction returns one repeater item, and native chunk serialization preserves both extreme
idle and active states.

Both blocks expose one full-footprint collision box at one eighth of a block high. They are
transparent to block light; idle state emits zero while active state emits nine. Neither is
random-tick enrolled.

The same run proves the complete two-, four-, six-, and eight-tick delay ladder for power and
release in all four directions, holds powered and unpowered states through a twenty-tick
telemetered observation window, and removes an active repeater with its item drop when support
disappears.

The map does not claim short-pulse filtering, torch burnout, wire-strength propagation, client
rendering, or sound events; those remain independently qualified boundaries.

Frozen aggregate signal:
`family=repeater-subsystem,subjects=2,claims=14,domains=93+94:0..15,materialization=item356+signal,drop=356,persistence=chunk-nbt,collision=1/8+1/8,light=0+9,ticks=2+4+6+8,neighbors=power+release+support,oracle=MATCH`.

Qualified semantic signature: `2b4d6f69cc7311f905e24eb766b78f1567cf9da3ddd4a390486624d6ee10870a`.
