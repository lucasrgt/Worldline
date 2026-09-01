<!-- worldline-map-schema=1 -->
<!-- boundary=b173-piston-subsystem-conformance-cycle -->
<!-- nonclaims=entity-shove,push-limit,quasi-connectivity,bud-order,client-rendering,sound-pitch -->
<!-- frozen-trace=c34a64fb2c039ec058d06c9b87f7ce5dac63cd155052b556c82b99c7380c124e -->

# Beta 1.7.3 piston subsystem conformance

`PistonSubsystemFixture` treats sticky piston `29`, piston `33`, piston head `34`, and moving
piston `36` as one causal subsystem. The official piston event path reaches directions `0..5`
for normal and sticky variants, exposing base, moving-tile, and settled-head metadata domains
`0..5 + 8..13`. Internal head and moving blocks have no direct item route: gameplay piston
events materialize them and their native destruction delegates the correct base or stored-block
drop.

Native chunk serialization round-trips both a settled head and an active moving block with its
`TileEntityPiston` stored block, metadata, direction, and extending flag. Collision probes bind
the base to one full collision box, the head to plate plus rod, and the moving state to one
translated stored-block box. All four registered blocks have zero opacity, zero emission, and
no random tick enrollment.

The same run holds idle base/head states, advances the moving tile to its stored head in three
native tile-entity steps, powers and depowers both base types through neighbor updates, invalidates a head
whose base disappears, and proves a moving state retains its tile entity across unrelated
neighbor notification.

The map does not claim entity shove geometry, push limits, quasi-connectivity, BUD ordering,
client rendering, or sound pitch; those remain independently qualified boundaries.

Frozen aggregate signal:
`family=piston-subsystem,subjects=4,claims=28,domains=0..5+8..13,materialization=normal+sticky,drop=head+moving,persistence=chunk-nbt,collision=1+2+1,light=zero,ticks=event-driven,neighbors=extend+retract+invalidate,oracle=MATCH`.

Qualified semantic signature: `c34a64fb2c039ec058d06c9b87f7ce5dac63cd155052b556c82b99c7380c124e`.
