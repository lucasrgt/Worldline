<!-- worldline-map-schema=1 -->
<!-- boundary=b173-bed-subsystem-conformance-cycle -->
<!-- nonclaims=gameplay-placement,save-reload,native-render,sleep-activation,spawn-selection,nether-explosion -->
<!-- frozen-trace=9fafcdf57291cb7dd2660a1468035f2d9f40f9b73f599889db777f4600b4a740 -->

# Beta 1.7.3 bed subsystem conformance

`BedSubsystemFixture` maps block `26` as a native two-cell directional structure. Direction bits
`0..3` belong to foot halves, bit `8` identifies head halves, and native occupancy toggling adds
bit `4` to heads, yielding the reachable head domain `8..15`.

Both halves have finite player-relative break strength and transition to air. Native harvesting is
asymmetric: a foot produces exactly one bed item `355`, while a head produces nothing. Each half
uses a full horizontal collision footprint with height `9/16`; the block is nonopaque, is not a
normal cube, and reports light opacity `0` with emission `0`.

Bed is not enrolled for scheduled block ticks, and inherited callbacks preserve complete foot and
head states. Neighbor handling is relational: a complete pair remains stable, an orphan foot
removes itself and drops one bed item, and an orphan head removes itself silently.

The Functional Census profile is enriched to
`bed,directional,multi-block,special-collision,stateful-metadata,support-dependent`. Placement and
save-reload remain owned by `m431-remaining-bed-orient-set`, while native rendering remains owned
by `b173-native-special-world-render-cycle`; this milestone promotes only seven unknown claims.
It does not claim sleeping, spawn selection, occupied-player arbitration, or Nether explosions.

Frozen aggregate signal:
`family=bed-subsystem,subject=26,claims=7,domain=foot0..3+head8..15,break=both-removed,drop=foot355x1+head-none,collision=full-x-9/16,light=0:0,ticks=scheduled-F+callback-stable,neighbors=paired-stable+orphans-cleaned,oracle=MATCH`.

Qualified semantic signature: `9fafcdf57291cb7dd2660a1468035f2d9f40f9b73f599889db777f4600b4a740`.
