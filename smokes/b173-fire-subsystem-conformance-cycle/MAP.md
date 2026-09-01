<!-- worldline-map-schema=1 -->
<!-- boundary=b173-fire-subsystem-conformance-cycle -->
<!-- nonclaims=stochastic-spread-distribution,tnt-ignition,rain-extinction,portal-creation,native-render -->
<!-- frozen-trace=35252ba59f92c8784bad3e2b729367cb5fc0f49e30e4c00192f77130ba03a971 -->

# Beta 1.7.3 fire subsystem conformance

`FireSubsystemFixture` maps block `51` as its native support-dependent fire class. A supplied
`51:0` item follows the ItemBlock placement route onto netherrack, creates one `51:0` cell,
and consumes the one-item stack.

Zero hardness yields infinite player-relative strength. The server harvest sequence removes the
cell and produces no item entity. Deterministic maximum Random inputs advance the native update
callback across every age from metadata 0 through 15, and native chunk serialization preserves
`51:15`.

Fire has no collision box, is noncollidable, has opacity 0 and emission 15, is enrolled for random
ticks, and reports tick rate 40. Netherrack-supported fire remains stable under ordinary neighbor
notifications; fire over a normal stone cube is removed causally when that support disappears.

The Functional Census profile is enriched to
`fire,luminous,random-tick,special-collision,support-dependent,tick-driven`. Tick-policy remains
owned by `m606-fire-spread-wood-set`, while native-render remains owned by
`b173-native-special-world-render-cycle`; this milestone promotes only eight previously unknown
claims. It does not claim stochastic spread distributions, TNT ignition, rain extinction, portal
creation, or native rendering.

Frozen aggregate signal:
`family=fire-subsystem,subject=51,claims=8,domain=51:0..15,item-placement=51x1>0,break=infinite+removed,drop=none,persistence=chunk-nbt,collision=none,light=0:15,ticks=random-T+age-0..15+rate-40,neighbors=supported+support-loss,oracle=MATCH`.

Qualified semantic signature: `35252ba59f92c8784bad3e2b729367cb5fc0f49e30e4c00192f77130ba03a971`.
