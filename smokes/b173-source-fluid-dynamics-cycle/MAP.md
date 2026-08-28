<!-- worldline-map-schema=1 -->
<!-- boundary=b173-source-fluid-dynamics-cycle -->
<!-- nonclaims=flowing-block-ids,state-domain,collision-shape,light-behavior,break-transition,drop-matrix,native-render -->
<!-- frozen-trace=47035bfa79e8d45c1c620377e0f7ff9429a8d0b7c131c3d0ef9052de5b902b1b -->

# Beta 1.7.3 source-fluid gated dynamics

Two public TestKit rows place still water `9:0` and still lava `11:0` from
their inventory items into separate official-server worlds. Each source is
enclosed by a gameplay-built stone channel. The fixture observes the source
and closed stone gate before the player causally breaks that gate, then waits
for the released horizontal fluid state and performs a clean save plus fresh
login.

This boundary claims gameplay placement and save/reload for the two source
subjects. It claims tick-policy and neighbor-response because propagation is
bounded behind the intact gate and begins only after the observed
stone-to-air transition. Flowing registry IDs `8` and `10`, arbitrary metadata
domains, breaking and drop semantics, collision, lighting, and rendering stay
outside scope. Exact stabilized flow states come only from the official
candidate observation.

Frozen aggregate signal:
`provider=b1.7.3-server-fluid-dynamics,family=source-fluid-dynamics,rows=2,passed=2,claims=8,gate=stone-to-air-to-flow,reload=FRESH_LOGINx2,evidence=b97aa062e3c6734fa1a1c253c5541311d07786fa15f0179c81d893eafda53825,isolation=2-fresh-worlds`.

Qualified semantic signature:
`47035bfa79e8d45c1c620377e0f7ff9429a8d0b7c131c3d0ef9052de5b902b1b`.
