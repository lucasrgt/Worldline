<!-- worldline-map-schema=1 -->
<!-- boundary=b173-iron-door-subsystem-conformance-cycle -->
<!-- nonclaims=gameplay-placement,save-reload,native-render,manual-activation,redstone-power-open-close,cross-chunk-power -->
<!-- frozen-trace=2614fe16b13374a50f90bbb989d8c3919a1c5b421b87b52902a5eb8a717d0bcf -->

# Beta 1.7.3 iron-door subsystem conformance

`IronDoorSubsystemFixture` maps block `71` as a native two-cell directional structure. Lower
metadata occupies `0..7`, upper metadata occupies `8..15`, direction uses the low two bits, and
bit `4` records the closed/open state on both halves.

Both halves have finite player-relative break strength and transition to air. Native harvesting is
asymmetric: a lower half produces exactly one iron-door item `330`, while an upper half produces
nothing. Closed metadata zero uses a three-sixteenths X collision plane; toggling the native open
bit rotates it to a three-sixteenths Z plane. The block is nonopaque, is not a normal cube, and
reports light opacity `0` with emission `0`.

Iron doors are not enrolled for scheduled block ticks, and inherited callbacks preserve complete
lower and upper states. Neighbor handling is structural: a supported complete pair remains stable,
an orphan lower removes itself and drops one iron-door item, an orphan upper removes itself
silently, and losing floor support removes both halves with one lower-half drop.

The Functional Census profile is enriched to
`directional,door,multi-block,redstone-component,special-collision,stateful-metadata,support-dependent`.
Placement and save-reload remain owned by `m241-iron-door-place`, native rendering remains owned
by `b173-native-special-world-render-cycle`, and powered open/close behavior remains owned by the
existing redstone and cross-chunk cycles. This milestone promotes only seven unknown claims.

Frozen aggregate signal:
`family=iron-door-subsystem,subject=71,claims=7,domain=lower0..7+upper8..15,break=both-removed,drop=lower330x1+upper-none,collision=closed-x-3/16+open-z-3/16,light=0:0,ticks=scheduled-F+callback-stable,neighbors=paired-stable+orphans+support-cleaned,oracle=MATCH`.

Qualified semantic signature: `2614fe16b13374a50f90bbb989d8c3919a1c5b421b87b52902a5eb8a717d0bcf`.
