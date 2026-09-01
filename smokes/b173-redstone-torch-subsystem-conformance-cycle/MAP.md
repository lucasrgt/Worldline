<!-- worldline-map-schema=1 -->
<!-- boundary=b173-redstone-torch-subsystem-conformance-cycle -->
<!-- nonclaims=wire-strength,consumer-propagation,client-particles,sound-pitch -->
<!-- frozen-trace=4e566a6a43d01eddd3b108b5b0bf777516a930c5ca4613b4f638922b9bc90ce1 -->

# Beta 1.7.3 redstone torch subsystem conformance

`RedstoneTorchSubsystemFixture` treats idle torch `75` and active torch `76` as one inverter.
Item `76` materializes the active form on all five support faces; powering the supporting block
materializes the hidden idle form without changing metadata. Idle destruction returns one active
torch item, and native chunk serialization preserves both states.

Neither state has collision. Both are transparent to block light; idle emits zero and active emits
seven. Both are random-tick enrolled and neighbor updates schedule inversion after two ticks.

The same run proves the native eight-off-transition burnout threshold within the hundred-tick
history, holds the torch idle while that history is live, recovers after history expiration plus
the scheduled delay, and drops the torch when its support disappears.

The map does not claim wire strength, downstream consumer propagation, client particles, or sound
pitch; those remain independently qualified boundaries.

Frozen aggregate signal:
`family=redstone-torch-subsystem,subjects=2,claims=13,domains=75+76:1..5,materialization=item76+signal,drop=76,persistence=chunk-nbt,collision=none+none,light=0+7,ticks=2+burnout,neighbors=invert+support,oracle=MATCH`.

Qualified semantic signature: `4e566a6a43d01eddd3b108b5b0bf777516a930c5ca4613b4f638922b9bc90ce1`.
