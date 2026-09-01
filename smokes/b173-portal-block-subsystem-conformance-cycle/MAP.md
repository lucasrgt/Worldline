<!-- worldline-map-schema=1 -->
<!-- boundary=b173-portal-block-subsystem-conformance-cycle -->
<!-- nonclaims=dimension-travel,portal-cooldown,coordinate-scaling,exit-search,client-particles,native-render,pigman-spawn-probability -->
<!-- frozen-trace=cd3b0d0c468f293cee48a82a4461eca23103ca95ccdaca187a3264023e467fd3 -->

# Beta 1.7.3 portal block subsystem conformance

`PortalBlockSubsystemFixture` treats portal block `90` as a frame-dependent lifecycle rather
than an isolated registry entry. Native materialization fills six `90:0` cells in canonical
four-by-five obsidian frames aligned on either horizontal axis.

Breaking one portal cell removes it without a drop. Native chunk serialization preserves all six
cells. Portal cells have no collision box, opacity zero, emission eleven, are not enrolled in the
random-tick scheduler, and remain stable when their native tick callback is invoked directly.
Removing one obsidian frame member makes the complete six-cell portal
interior collapse through native neighbor handling.

Registry presence is observed through native construction but is already covered by the universal
registry census claim, so this milestone promotes only the nine previously unknown claims.

The map does not claim cross-dimensional travel, portal cooldown, coordinate scaling, exit portal
search or construction, client particles, native rendering, or the probability of pigman spawning.

Frozen aggregate signal:
`family=portal-block-subsystem,subject=90,claims=9,domain=90:0@X+Z,materialization=frame-6+6,drop=none,persistence=chunk-nbt-6,collision=none,light=11,ticks=scheduled-F+callback-stable,neighbors=frame-collapse,oracle=MATCH`.

Qualified semantic signature: `cd3b0d0c468f293cee48a82a4461eca23103ca95ccdaca187a3264023e467fd3`.
