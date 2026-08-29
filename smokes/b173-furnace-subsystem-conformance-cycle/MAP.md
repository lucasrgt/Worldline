<!-- worldline-map-schema=1 -->
<!-- boundary=b173-furnace-subsystem-conformance-cycle -->
<!-- nonclaims=container-protocol,client-texture,particles,sound -->
<!-- frozen-trace=81a14731a028188bc83b9a9b7c4637bb939c932e77fae97c31573830a8be100d -->

# Beta 1.7.3 furnace subsystem conformance

`FurnaceSubsystemFixture` treats idle furnace `61` and lit furnace `62` as one stateful tile
subsystem. It proves orientations `2..5`, coal ignition, the `61 -> 62 -> 61` transition, a full
two-hundred-tick sand-to-glass smelt, exact burn progress, and stable irrelevant-neighbor updates.

The lifecycle arm breaks an active furnace and observes the furnace item plus all three inventory
slots. Native chunk NBT reload preserves block `62:5`, nonzero burn and cook progress, and input,
fuel, and output stacks. Both blocks retain full collision; the lit form emits thirteen.

Container packet behavior, client texture selection, particles, and sound remain independent
boundaries.

Frozen aggregate signal:
`family=furnace-subsystem,subjects=2,claims=10,domains=61+62:2..5,materialization=item61+smelt,drop=61+contents,persistence=chunk-nbt-progress,collision=full+full,light=0+13,ticks=tile-200,neighbors=stable+orientation,oracle=MATCH`.

Qualified semantic signature: `81a14731a028188bc83b9a9b7c4637bb939c932e77fae97c31573830a8be100d`.
